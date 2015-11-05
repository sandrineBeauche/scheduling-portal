/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive_grid_cloud_portal.scheduler.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ow2.proactive_grid_cloud_portal.common.client.Controller;
import org.ow2.proactive_grid_cloud_portal.common.client.LoadingMessage;
import org.ow2.proactive_grid_cloud_portal.common.client.LoginPage;
import org.ow2.proactive_grid_cloud_portal.common.client.Settings;
import org.ow2.proactive_grid_cloud_portal.common.client.json.JSONUtils;
import org.ow2.proactive_grid_cloud_portal.common.client.model.LogModel;
import org.ow2.proactive_grid_cloud_portal.common.client.model.LoginModel;
import org.ow2.proactive_grid_cloud_portal.common.shared.Config;
import org.ow2.proactive_grid_cloud_portal.scheduler.client.ServerLogsView.ShowLogsCallback;
import org.ow2.proactive_grid_cloud_portal.scheduler.client.controller.ExecutionsController;
import org.ow2.proactive_grid_cloud_portal.scheduler.client.controller.TasksController;
import org.ow2.proactive_grid_cloud_portal.scheduler.shared.SchedulerConfig;

import com.google.gwt.core.client.GWT.UncaughtExceptionHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONException;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.layout.Layout;
import com.smartgwt.client.widgets.layout.SectionStackSection;
import com.smartgwt.client.widgets.layout.VLayout;


/**
 * Logic that interacts between the remote Scheduler and the local Model
 * <p>
 * The Controller can be accessed statically by the client to ensure
 * coherent modification of the Model data:
 * <ul><li>views submit actions to the Controller,
 * <li>the Controller performs the actions,
 * <li>the Controller updates new Data to the Model,
 * <li>the view displays what it reads from the Model.
 * </code>
 *
 * @author mschnoor
 */
public class SchedulerController extends Controller implements UncaughtExceptionHandler {

    static final String LOCAL_SESSION_COOKIE = "pa.sched.local_session";

    private static final int AUTO_LOGIN_TIMER_PERIOD_IN_MS = 1000;

    @Override
    public String getLoginSettingKey() {
        return LOGIN_SETTING;
    }

    @Override
    public String getLogo350Url() {
        return SchedulerImagesUnbundled.LOGO_350;
    }

    /** if this is different than LOCAL_SESSION cookie, we need to disconnect */
    private String localSessionNum;

    /** scheduler server for async rpc calls */
    private SchedulerServiceAsync scheduler = null;

    /** writable view of the model */
    private SchedulerModelImpl model = null;

    

    /** login page when not logged in, or null */
    private LoginPage loginView = null;
    /** main page when logged in, or null */
    private SchedulerPage schedulerView = null;

    /** contains all pending getTaskOutput requests, taskId as key */
    private Map<String, Request> taskOutputRequests = null;
    
    /** periodically updates the local job view */
    private Timer schedulerTimerUpdate = null;
    
 // incremented each time #fetchJobs is called
    private int timerUpdate = 0;

    /** ids of jobs we should auto fetch */
    private Set<String> liveOutputJobs = null;
    /** periodically fetches live output */
    private Timer liveOutputUpdater = null;

    /** frequency of user info fetch */
    private int userFetchTick = active_tick;
    /** frequency of stats info fetch */
    private int statsFetchTick = active_tick;

    /** do not fetch visualization info when false */
    private boolean visuFetchEnabled = false;

    

    private static final int active_tick = 3;
    private static final int lazy_tick = 20;

    private Timer autoLoginTimer;


    protected TasksController tasksController;


    protected ExecutionsController executionController;


    /**
     * Default constructor
     *
     * @param scheduler server endpoint for async remote calls
     */
    public SchedulerController(SchedulerServiceAsync scheduler) {
        this.scheduler = scheduler;
        this.model = new SchedulerModelImpl();
        this.taskOutputRequests = new HashMap<String, Request>();
        this.liveOutputJobs = new HashSet<String>();

        init();
    }

    /**
     * Entry point: creates the GUI
     */
    private void init() {
        final String session = Settings.get().getSetting(SESSION_SETTING);

        if (session != null) {
            LoadingMessage loadingMessage = new LoadingMessage();
            loadingMessage.draw();
            tryLogin(session, loadingMessage);
        } else {
            this.loginView = new LoginPage(this, null);
            tryToLoginIfLoggedInRm();
        }
    }

    private void tryLogin(final String session, final VLayout loadingMessage) {
        this.scheduler.getSchedulerStatus(session, new AsyncCallback<String>() {
            public void onSuccess(String result) {
                if (loadingMessage != null) {
                    loadingMessage.destroy();
                }
                login(session, Settings.get().getSetting(LOGIN_SETTING));
                LogModel.getInstance().logMessage("Rebound session " + session);
            }

            public void onFailure(Throwable caught) {
                if (loadingMessage != null) {
                    loadingMessage.destroy();
                }
                Settings.get().clearSetting(SESSION_SETTING);
                SchedulerController.this.loginView = new LoginPage(SchedulerController.this, null);
                tryToLoginIfLoggedInRm();
            }
        });
    }

    private void tryToLoginIfLoggedInRm() {
        autoLoginTimer = new Timer() {
            @Override
            public void run() {
                String session = Settings.get().getSetting(SESSION_SETTING);
                if (session != null) {
                    tryLogin(session, null);
                }
            }
        };
        autoLoginTimer.scheduleRepeating(AUTO_LOGIN_TIMER_PERIOD_IN_MS);
    }

    private void stopTryingLoginIfLoggerInRm() {
        if (autoLoginTimer != null) {
            autoLoginTimer.cancel();
        }
    }

    /**
     * @return the Model used by this Controller
     */
    public SchedulerModel getModel() {
        return this.model;
    }

    /**
     * Use the event dispatcher to register listeners for specific events
     * 
     * @return the current event dispatcher
     */
    @Override
    public SchedulerEventDispatcher getEventDispatcher() {
        return this.model;
    }




    public SchedulerServiceAsync getScheduler() {
        return scheduler;
    }

    @Override
    public void login(final String sessionId, final String login) {
        stopTryingLoginIfLoggerInRm();
        scheduler.getVersion(new AsyncCallback<String>() {
            public void onSuccess(String result) {
                JSONObject obj = JSONParser.parseStrict(result).isObject();
                String schedVer = obj.get("scheduler").isString().stringValue();
                String restVer = obj.get("rest").isString().stringValue();
                Config.get().set(SchedulerConfig.SCHED_VERSION, schedVer);
                Config.get().set(SchedulerConfig.REST_VERSION, restVer);

                __login(sessionId, login);
            }

            public void onFailure(Throwable caught) {
                String msg = JSONUtils.getJsonErrorMessage(caught);
                LogModel.getInstance().logImportantMessage("Failed to get REST server version: " + msg);
            }
        });

    }

    private void __login(String sessionId, String login) {
        LoginModel loginModel = LoginModel.getInstance();
        loginModel.setLoggedIn(true);
        loginModel.setLogin(login);
        loginModel.setSessionId(sessionId);

        if (loginView != null)
            SchedulerController.this.loginView.destroy();
        this.loginView = null;
        this.schedulerView = new SchedulerPage(this);

        this.executionController.getJobsController().fetchJobs(true);
        
        this.startTimer();

        String lstr = "";
        if (login != null) {
            lstr += " as " + login;
        }

        Settings.get().setSetting(SESSION_SETTING, sessionId);
        if (login != null) {
            Settings.get().setSetting(LOGIN_SETTING, login);
        } else {
            Settings.get().clearSetting(LOGIN_SETTING);
        }

        // this cookie is reset to a random int on every login:
        // if another session in another tab has a different localSessionNUm
        // than the one in the domain cookie, then we exit
        this.localSessionNum = "" + System.currentTimeMillis() + "_" + Random.nextInt();
        Cookies.setCookie(LOCAL_SESSION_COOKIE, this.localSessionNum);

        LogModel.getInstance().logMessage("Connected to " + SchedulerConfig.get().getRestUrl() + lstr + " (sessionId=" +
                loginModel.getSessionId() + ")");
    }

    /**
     * Disconnect from the Scheduler
     * <p>
     * Maintains coherent state in the views between
     * LoginView and SchedulerView, depending the connection status
     *
     * @throws IllegalStateException not connected
     */
    public void logout() {
        if (!LoginModel.getInstance().isLoggedIn())
            throw new IllegalStateException("Not connected");

        Settings.get().clearSetting(SESSION_SETTING);

        scheduler.logout(LoginModel.getInstance().getSessionId(), new AsyncCallback<Void>() {

            public void onFailure(Throwable caught) {
            }

            public void onSuccess(Void result) {
            }

        });

        // do not wait for the callback, stop the thread immediately
        // or it may try to update stuff while disconnected
        teardown(null);
        tryToLoginIfLoggedInRm();
    }
    
    
    
    public Layout buildTaskView(){
        this.tasksController = new TasksController(this);
        return this.tasksController.buildView();
    }
    
    
    public SectionStackSection buildExecutionsView(){
        this.executionController = new ExecutionsController(this);
        return this.executionController.buildView();
    }
    
   
    
    void setVisuFetchEnabled(boolean b) {
        this.visuFetchEnabled = b;
    }

    public void visuFetch(final String jobId) {

        // fetch visu info
        if (jobId != null && this.visuFetchEnabled) {
            String curHtml = model.getJobHtml(jobId);
            if (curHtml != null) {
                // exists already, resetting it will trigger listeners
                model.setJobHtml(jobId, curHtml);
            } else {
                final long t = System.currentTimeMillis();
                this.scheduler.getJobHtml(LoginModel.getInstance().getSessionId(), jobId, new AsyncCallback<String>() {
                    public void onSuccess(String result) {
                        model.setJobHtml(jobId, result);
                        LogModel.getInstance().logMessage("Fetched html for job " + jobId + " in " +
                                (System.currentTimeMillis() - t) + " ms");
                    }

                    public void onFailure(Throwable caught) {
                        String msg = "Failed to fetch html for job " + jobId;
                        String json = JSONUtils.getJsonErrorMessage(caught);
                        if (json != null)
                            msg += " : " + json;

                        LogModel.getInstance().logImportantMessage(msg);

                        // trying to load image
                        String curPath = model.getJobImagePath(jobId);
                        if (curPath != null) {
                            // exists already, resetting it will trigger listeners
                            model.setJobImagePath(jobId, curPath);
                        } else {
                            final long t = System.currentTimeMillis();
                            scheduler.getJobImage(LoginModel.getInstance().getSessionId(), jobId, new AsyncCallback<String>() {
                                public void onSuccess(String result) {
                                    model.setJobImagePath(jobId, result);
                                    LogModel.getInstance().logMessage("Fetched image for job " + jobId + " in " +
                                            (System.currentTimeMillis() - t) + " ms");
                                }

                                public void onFailure(Throwable caught) {
                                    String msg = "Failed to fetch image for job " + jobId;
                                    String json = JSONUtils.getJsonErrorMessage(caught);
                                    if (json != null)
                                        msg += " : " + json;

                                    LogModel.getInstance().logImportantMessage(msg);
                                    model.visuUnavailable(jobId);
                                }
                            });
                        }
                    }
                });
            }


        }
    }

    
    /**
     * Attempt to start the scheduler, might fail depending the server state/rights
     */
    public void startScheduler() {
        this.scheduler.startScheduler(LoginModel.getInstance().getSessionId(), new AsyncCallback<Boolean>() {
            public void onFailure(Throwable caught) {
                String msg = JSONUtils.getJsonErrorMessage(caught);
                warn("Could not start Scheduler:\n" + msg);
                LogModel.getInstance().logImportantMessage("Failed to start Scheduler: " + msg);
            }

            public void onSuccess(Boolean result) {
                LogModel.getInstance().logMessage("Scheduler started");
            }
        });
    }

    /**
     * Attempt to stop the scheduler, might fail depending server state/rights
     */
    public void stopScheduler() {
        this.scheduler.stopScheduler(LoginModel.getInstance().getSessionId(), new AsyncCallback<Boolean>() {
            public void onSuccess(Boolean result) {
                LogModel.getInstance().logMessage("Scheduler stopped");
            }

            public void onFailure(Throwable caught) {
                String msg = JSONUtils.getJsonErrorMessage(caught);
                warn("Could not stop Scheduler:\n" + msg);
                LogModel.getInstance().logImportantMessage("Failed to stop Scheduler: " + msg);
            }
        });
    }

    /**
     * Attempt to pause the scheduler, might fail depending server state/rights
     */
    public void pauseScheduler() {
        this.scheduler.pauseScheduler(LoginModel.getInstance().getSessionId(), new AsyncCallback<Boolean>() {
            public void onSuccess(Boolean result) {
                LogModel.getInstance().logMessage("Scheduler paused");
            }

            public void onFailure(Throwable caught) {
                String msg = JSONUtils.getJsonErrorMessage(caught);
                warn("Could not pause Scheduler:\n" + msg);
                LogModel.getInstance().logImportantMessage("Failed to pause Scheduler: " + msg);
            }
        });
    }

    /**
     * Attempt to freeze the scheduler, might fail depending server state/rights
     */
    public void freezeScheduler() {
        this.scheduler.freezeScheduler(LoginModel.getInstance().getSessionId(), new AsyncCallback<Boolean>() {
            public void onSuccess(Boolean result) {
                LogModel.getInstance().logMessage("Scheduler freezed");
            }

            public void onFailure(Throwable caught) {
                String msg = JSONUtils.getJsonErrorMessage(caught);
                warn("Could not freeze Scheduler:\n" + msg);
                LogModel.getInstance().logImportantMessage("Failed to freeze Scheduler: " + msg);
            }
        });
    }

    /**
     * Attempt to resume the scheduler, might fail depending server state/rights
     */
    public void resumeScheduler() {
        this.scheduler.resumeScheduler(LoginModel.getInstance().getSessionId(), new AsyncCallback<Boolean>() {
            public void onSuccess(Boolean result) {
                LogModel.getInstance().logMessage("Scheduler resumed");
            }

            public void onFailure(Throwable caught) {
                String msg = JSONUtils.getJsonErrorMessage(caught);
                warn("Could not resume Scheduler:\n" + msg);
                LogModel.getInstance().logImportantMessage("Failed to resume Scheduler: " + msg);
            }
        });
    }

    /**
     * Attempt to kill the scheduler, might fail depending server state/rights
     */
    public void killScheduler() {
        this.scheduler.killScheduler(LoginModel.getInstance().getSessionId(), new AsyncCallback<Boolean>() {
            public void onSuccess(Boolean result) {
                LogModel.getInstance().logMessage("Scheduler killed");
            }

            public void onFailure(Throwable caught) {
                String msg = JSONUtils.getJsonErrorMessage(caught);
                warn("Could not kill Scheduler:\n" + msg);
                LogModel.getInstance().logImportantMessage("Failed to kill Scheduler: " + msg);
            }
        });
    }

    /**
     * Auto fetch the output for the currently selected job
     */
    public void getLiveOutput() {
        Job j = this.executionController.getJobsController().getModel().getSelectedJob();
        if (j == null)
            return;

        if (this.model.getTasksModel().getTasks().isEmpty())
            return;

        final String jobId = "" + j.getId();

        if (this.liveOutputJobs.contains(jobId)) {
            model.appendLiveOutput(jobId, "");
            return;
        }

        this.model.setLiveOutput(jobId, true);
        this.liveOutputJobs.add(jobId);

        if (this.liveOutputUpdater == null) {
            this.startLiveTimer();
        }
    }

    public void deleteLiveLogJob() {
        Job j = this.executionController.getJobsController().getModel().getSelectedJob();
        if (j == null)
            return;

        if (this.model.getTasksModel().getTasks().isEmpty())
            return;

        final String jobId = String.valueOf(j.getId());
        liveOutputJobs.remove(jobId);
        model.setLiveOutput(jobId, false);
    }

    /**
     * Start the timer that will periodically fetch live logs
     */
    private void startLiveTimer() {
        this.liveOutputUpdater = new Timer() {

            @Override
            public void run() {
                for (Iterator<String> it = SchedulerController.this.liveOutputJobs.iterator(); it.hasNext();) {
                    final String jobId = it.next();
                    doFetchLiveLog(jobId);

                    int id = Integer.parseInt(jobId);
                    Job j = executionController.getJobsController().getModel().getJobs().get(id);
                    if (j == null || j.isExecuted()) {
                        LogModel.getInstance().logMessage("stop fetching live logs for job " + jobId);
                        it.remove();
                        model.setLiveOutput(jobId, false);
                        model.appendLiveOutput(jobId, "");
                    }
                }
            }
        };
        this.liveOutputUpdater.scheduleRepeating(SchedulerConfig.get().getLivelogsRefreshTime());
    }

    private void doFetchLiveLog(final String jobId) {
        SchedulerController.this.scheduler.getLiveLogJob(LoginModel.getInstance().getSessionId(), jobId,
                new AsyncCallback<String>() {
            public void onSuccess(String result) {
                if (result.length() > 0) {
                    LogModel.getInstance().logMessage("Fetched livelog chunk for job " + jobId + " (" +
                            result.length() + " chars)");
                    model.appendLiveOutput(jobId, result);
                }
            }

            public void onFailure(Throwable caught) {
                String msg = JSONUtils.getJsonErrorMessage(caught);
                LogModel.getInstance().logImportantMessage("Failed to fetch live log for job " + jobId + ": " + msg);
            }
        });
    }

    /**
     * Stop the timer that will periodically fetch live logs
     */
    private void stopLiveTimer() {
        if (this.liveOutputUpdater == null)
            return;

        this.liveOutputUpdater.cancel();
        this.liveOutputUpdater = null;
    }

    /**
     * Fetch the output for the currently selected job
     * store the result (or error msg) in the model
     * @param logMode one of {@link SchedulerServiceAsync#LOG_ALL}, {@link SchedulerServiceAsync#LOG_STDERR},
     * 	 {@link SchedulerServiceAsync#LOG_STDOUT}
     */
    public void getJobOutput(int logMode) {
        if (this.executionController.getJobsController().getModel().getSelectedJob() == null)
            return;

        Job j = this.executionController.getJobsController().getModel().getSelectedJob();
        if (this.model.getTasksModel().getTasks().isEmpty()) {
            // notify the listeners, they will figure out there is no output
            this.model.updateOutput(j.getId());
        }

        for (Task t : this.model.getTasksModel().getTasks()) {
            switch (t.getStatus()) {
            case SKIPPED:
            case PENDING:
            case SUBMITTED:
            case NOT_STARTED:
                break;
            default:
                this.getTaskOutput(j.getId(), t, logMode);
                break;
            }
        }
    }

    /**
     * Fetch the output for a single task,
     * store the result (or error message) in the model
     * 
     * @param jobId id of the job containing this task
     * @param task task for which the output should be fetched
     * @param logMode one of {@link SchedulerServiceAsync#LOG_ALL}, {@link SchedulerServiceAsync#LOG_STDERR},
     * 	 {@link SchedulerServiceAsync#LOG_STDOUT}
     */
    public void getTaskOutput(final int jobId, final Task task, final int logMode) {
        this.model.setLiveOutput("" + jobId, false);
        this.liveOutputJobs.remove("" + jobId);
        if (this.liveOutputJobs.isEmpty()) {
            stopLiveTimer();
        }

        Request req = this.scheduler.getTaskOutput(LoginModel.getInstance().getSessionId(), "" + jobId, task.getName(), logMode,
                new AsyncCallback<String>() {
            public void onFailure(Throwable caught) {
                String msg = JSONUtils.getJsonErrorMessage(caught);
                // might be an exception
                try {
                    JSONObject json = parseJSON(caught.getMessage()).isObject();
                    if (json.containsKey("stackTrace")) {
                        msg = json.get("stackTrace").isString().stringValue();
                        msg = msg.replace("\t", "&nbsp;&nbsp;&nbsp;&nbsp;");
                        msg = msg.replace("\n", "<br>");
                    }
                } catch (Throwable t) {
                    // not json
                }
                SchedulerController.this.model.setTaskOutput(jobId, task,
                        "[" + task.getName() + "] <span style='color:red;'>" + msg + "</span>");
                LogModel.getInstance().logMessage("Failed to get output for task " +
                        task.getName() + " in job " + jobId /* + ": " + msg */);

                taskOutputRequests.remove("" + task.getId());
            }

            public void onSuccess(String result) {
                SchedulerController.this.model.setTaskOutput(jobId, task, result);
                LogModel.getInstance().logMessage("Successfully fetched output for task " +
                        task.getName() + " in job " + jobId);

                taskOutputRequests.remove("" + task.getId());
            }
        });
        this.taskOutputRequests.put("" + task.getId(), req);
    }

    /**
     * Fetch server logs for a single task
     * 
     * @param jobId id of the job containing this task
     * @param taskname task for which the output should be fetched
     * @param logs one of {@link SchedulerServiceAsync#LOG_ALL}, {@link SchedulerServiceAsync#LOG_STDERR},
     *   {@link SchedulerServiceAsync#LOG_STDOUT}
     */
    public void getTaskServerLogs(final int jobId, final String taskname, final ShowLogsCallback logs) {
        Request req = this.scheduler.getTaskServerLogs(LoginModel.getInstance().getSessionId(), jobId, taskname,
                new AsyncCallback<String>() {
            public void onFailure(Throwable caught) {
                String msg = JSONUtils.getJsonErrorMessage(caught);
                // might be an exception
                try {
                    JSONObject json = parseJSON(caught.getMessage()).isObject();
                    if (json.containsKey("stackTrace")) {
                        msg = json.get("stackTrace").isString().stringValue();
                        msg = msg.replace("\t", "&nbsp;&nbsp;&nbsp;&nbsp;");
                        msg = msg.replace("\n", "<br>");
                    }
                } catch (Throwable t) {
                    // not json
                }
                LogModel.getInstance().logMessage("Failed to get server logs for task " +
                        taskname + " in job " + jobId /* + ": " + msg */);
            }

            public void onSuccess(String result) {
                LogModel.getInstance().logMessage("Successfully fetched server logs for task " + taskname +
                        " in job " + jobId);
                logs.show(result);
            }
        });
    }

    /**
     * Fetch server logs for a single job
     * 
     * @param jobId id of the job containing this task
     * @param logs one of {@link SchedulerServiceAsync#LOG_ALL}, {@link SchedulerServiceAsync#LOG_STDERR},
     *   {@link SchedulerServiceAsync#LOG_STDOUT}
     */
    public void getJobServerLogs(final int jobId, final ShowLogsCallback logs) {
        Request req = this.scheduler.getJobServerLogs(LoginModel.getInstance().getSessionId(), jobId,
                new AsyncCallback<String>() {
            public void onFailure(Throwable caught) {
                String msg = JSONUtils.getJsonErrorMessage(caught);
                // might be an exception
                try {
                    JSONObject json = parseJSON(caught.getMessage()).isObject();
                    if (json.containsKey("stackTrace")) {
                        msg = json.get("stackTrace").isString().stringValue();
                        msg = msg.replace("\t", "&nbsp;&nbsp;&nbsp;&nbsp;");
                        msg = msg.replace("\n", "<br>");
                    }
                } catch (Throwable t) {
                    // not json
                }
                LogModel.getInstance().logMessage("Failed to get server logs for a job " +
                        jobId);
            }

            public void onSuccess(String result) {
                LogModel.getInstance().logMessage("Successfully fetched server logs for job " + jobId);
                logs.show(result);
            }
        });
    }


    public void restartTimer(){
        this.stopTimer();
        this.startTimer();
    }
    
    
    public void restartLiveTimer(){
        this.stopLiveTimer();
        this.startLiveTimer();
    }
    

    /**
     * Starts the Timer that will periodically fetch the current scheduler state
     * from the server end and update the local view
     */
    private void startTimer() {
        if (this.schedulerTimerUpdate != null)
            throw new IllegalStateException("There's already a Timer");

        this.schedulerTimerUpdate = new Timer() {

            @Override
            public void run() {

                if (!localSessionNum.equals(Cookies.getCookie(LOCAL_SESSION_COOKIE))) {
                    teardown("Duplicate session detected!<br>"
                            + "Another tab or window in this browser is accessing this page.");
                }

                SchedulerController.this.updateSchedulerStatus();

                executionController.executionStateRevision();

                if (timerUpdate % userFetchTick == 0) {
                    final long t1 = System.currentTimeMillis();

                    scheduler.getSchedulerUsers(LoginModel.getInstance().getSessionId(), new AsyncCallback<String>() {
                        public void onSuccess(String result) {
                            List<SchedulerUser> users;

                            JSONValue val = parseJSON(result);
                            JSONArray arr = val.isArray();
                            if (arr == null) {
                                error("Expected JSON Array: " + val.toString());
                            }
                            users = getUsersFromJson(arr);
                            model.setSchedulerUsers(users);

                            long t = (System.currentTimeMillis() - t1);
                            LogModel.getInstance().logMessage("<span style='color:gray;'>Fetched " + users.size() +
                                    " users in " + t + " ms</span>");
                        }

                        public void onFailure(Throwable caught) {
                            if (!LoginModel.getInstance().isLoggedIn())
                                return;

                            error("Failed to fetch scheduler users:<br>" + JSONUtils.getJsonErrorMessage(caught));
                        }
                    });
                }

                if (timerUpdate % statsFetchTick == 0) {
                    final long t1 = System.currentTimeMillis();

                    scheduler.getStatistics(LoginModel.getInstance().getSessionId(), new AsyncCallback<String>() {
                        public void onFailure(Throwable caught) {
                            String msg = JSONUtils.getJsonErrorMessage(caught);
                            if (!LoginModel.getInstance().isLoggedIn())
                                return;
                            error("Failed to fetch scheduler stats:<br>" + msg);
                        }

                        public void onSuccess(String result) {
                            HashMap<String, String> stats = new HashMap<String, String>();

                            JSONObject json = parseJSON(result).isObject();
                            if (json == null)
                                error("Expected JSON Object: " + result);

                            stats.put("JobSubmittingPeriod", json.get("JobSubmittingPeriod").isString()
                                    .stringValue());
                            stats.put("FormattedJobSubmittingPeriod", json
                                    .get("FormattedJobSubmittingPeriod").isString().stringValue());
                            stats.put("MeanJobPendingTime", json.get("MeanJobPendingTime").isString()
                                    .stringValue());
                            stats.put("ConnectedUsersCount", json.get("ConnectedUsersCount").isString()
                                    .stringValue());
                            stats.put("FinishedTasksCount", json.get("FinishedTasksCount").isString()
                                    .stringValue());
                            stats.put("RunningJobsCount", json.get("RunningJobsCount").isString()
                                    .stringValue());
                            stats.put("RunningTasksCount", json.get("RunningTasksCount").isString()
                                    .stringValue());
                            stats.put("FormattedMeanJobPendingTime", json.get("FormattedMeanJobPendingTime")
                                    .isString().stringValue());
                            stats.put("MeanJobExecutionTime", json.get("MeanJobExecutionTime").isString()
                                    .stringValue());
                            stats.put("PendingTasksCount", json.get("PendingTasksCount").isString()
                                    .stringValue());
                            stats.put("FinishedJobsCount", json.get("FinishedJobsCount").isString()
                                    .stringValue());
                            stats.put("TotalTasksCount", json.get("TotalTasksCount").isString().stringValue());
                            stats.put("FormattedMeanJobExecutionTime",
                                    json.get("FormattedMeanJobExecutionTime").isString().stringValue());
                            stats.put("TotalJobsCount", json.get("TotalJobsCount").isString().stringValue());
                            stats.put("PendingJobsCount", json.get("PendingJobsCount").isString()
                                    .stringValue());

                            model.setSchedulerStatistics(stats);

                            long t = (System.currentTimeMillis() - t1);
                            LogModel.getInstance().logMessage("<span style='color:gray;'>Fetched sched stats: " +
                                    result.length() + " chars in " + t + " ms</span>");
                        }
                    });

                    final long t2 = System.currentTimeMillis();

                    scheduler.getStatisticsOnMyAccount(LoginModel.getInstance().getSessionId(), new AsyncCallback<String>() {
                        public void onFailure(Throwable caught) {
                            if (!LoginModel.getInstance().isLoggedIn())
                                return;
                            error("Failed to fetch account stats:<br>" + JSONUtils.getJsonErrorMessage(caught));
                        }

                        public void onSuccess(String result) {
                            HashMap<String, String> stats = new HashMap<String, String>();

                            JSONObject json = parseJSON(result).isObject();
                            if (json == null)
                                error("Expected JSON Object: " + result);

                            stats.put("TotalTaskCount", json.get("TotalTaskCount").isString().stringValue());
                            stats.put("TotalJobDuration", json.get("TotalJobDuration").isString()
                                    .stringValue());
                            stats.put("TotalJobCount", json.get("TotalJobCount").isString().stringValue());
                            stats.put("TotalTaskDuration", json.get("TotalTaskDuration").isString()
                                    .stringValue());

                            model.setAccountStatistics(stats);

                            long t = (System.currentTimeMillis() - t2);
                            LogModel.getInstance().logMessage("<span style='color:gray;'>Fetched account stats: " +
                                    result.length() + " chars in " + t + " ms</span>");
                        }
                    });
                }
                timerUpdate++;
            }
        };
        this.schedulerTimerUpdate.scheduleRepeating(SchedulerConfig.get().getClientRefreshTime());
    }

    

    

    /**
     * Parse the raw JSON array describing the users list, return a Java representation
     * @param jsonarray JSONArray containing all users
     * @return the currently connected users
     * @throws JSONException JSON parsing failed
     */
    private List<SchedulerUser> getUsersFromJson(JSONArray jsonarray) throws JSONException {
        ArrayList<SchedulerUser> users = new ArrayList<SchedulerUser>();

        for (int i = 0; i < jsonarray.size(); i++) {
            JSONObject jsonUser = jsonarray.get(i).isObject();
            users.add(SchedulerUser.parseJson(jsonUser));
        }

        return users;
    }

    /**
     * Fetch scheduler status, update the model,
     * fail hard on error
     */
    private void updateSchedulerStatus() {
        scheduler.getSchedulerStatus(LoginModel.getInstance().getSessionId(), new AsyncCallback<String>() {

            public void onFailure(Throwable caught) {
                if (!LoginModel.getInstance().isLoggedIn()) {
                    // might have been disconnected in between
                    return;
                }
                String msg = JSONUtils.getJsonErrorMessage(caught);
                error("Error while fetching status:\n" + caught.getClass().getName() + " " + msg);
                LogModel.getInstance().logImportantMessage("Error while fetching status: " + msg);
            }

            public void onSuccess(String result) {
                JSONValue val = parseJSON(result);
                String sval = val.isString().stringValue();
                SchedulerStatus stat = SchedulerStatus.valueOf(sval);
                SchedulerController.this.model.setSchedulerStatus(stat);

                if (result.equals(SchedulerStatus.SHUTTING_DOWN)) {
                    error("The Scheduler has been shut down, exiting");
                } else if (result.equals(SchedulerStatus.KILLED)) {
                    error("The Scheduler has been killed, exiting");
                }
                // do not model.logMessage() : this is repeated by a timer
            }

        });
    }

    
    /**
     * @param b true fetch users info less often
     */
    protected void setLazyUserFetch(boolean b) {
        this.userFetchTick = (b) ? lazy_tick : active_tick;
    }

    /**
     * @param b true fetch stats info less often
     */
    protected void setLazyStatsFetch(boolean b) {
        this.statsFetchTick = (b) ? lazy_tick : active_tick;
    }

    /**
     * Issue an error message to the user and exit the schedulerView
     *
     * @param reason error message to display
     */
    private void error(String reason) {
        LogModel.getInstance().logCriticalMessage(reason);
    }

    private void warn(String reason) {
        SC.warn(reason);
    }

    /**
     * Leave the scheduler view
     * Does not disconnect from the server
     */
    public void teardown(String message) {
        this.stopTimer();
        this.stopLiveTimer();
        this.model = new SchedulerModelImpl();

        SchedulerController.this.schedulerView.destroy();
        SchedulerController.this.schedulerView = null;

        this.loginView = new LoginPage(this, message);
    }

    /**
     * Stops the Timer that updates the local view of the remote scheduler
     */
    public void stopTimer() {
        if (this.schedulerTimerUpdate == null)
            return;

        this.schedulerTimerUpdate.cancel();
        this.schedulerTimerUpdate = null;
    }

    public void onUncaughtException(Throwable e) {
        e.printStackTrace();
    }

    public void getUsage(String user, Date startDate, Date endDate) {
        scheduler.getUsage(LoginModel.getInstance().getSessionId(), user, startDate, endDate, new AsyncCallback<List<JobUsage>>() {
            @Override
            public void onFailure(Throwable caught) {
                String msg = JSONUtils.getJsonErrorMessage(caught);
                LogModel.getInstance().logImportantMessage("Failed to fetch usage data " + ": " + msg);
            }

            @Override
            public void onSuccess(List<JobUsage> jobUsages) {
                model.setUsage(jobUsages);
                LogModel.getInstance().logMessage("Successfully fetched usage for " + jobUsages.size()+ " jobs");

            }
        });
    }

    public void getUsersWithJobs() {
        final long t1 = System.currentTimeMillis();

        scheduler.getSchedulerUsersWithJobs(LoginModel.getInstance().getSessionId(), new AsyncCallback<String>() {
            public void onSuccess(String result) {
                List<SchedulerUser> users;

                JSONValue val = parseJSON(result);
                JSONArray arr = val.isArray();
                if (arr == null) {
                    error("Expected JSON Array: " + val.toString());
                }
                users = getUsersFromJson(arr);
                model.setSchedulerUsersWithJobs(users);

                long t = (System.currentTimeMillis() - t1);
                LogModel.getInstance().logMessage("<span style='color:gray;'>Fetched " + users.size() +
                        " users with jobs in " + t + " ms</span>");
            }

            public void onFailure(Throwable caught) {
                if (!LoginModel.getInstance().isLoggedIn())
                    return;

                LogModel.getInstance().logMessage("Failed to fetch scheduler users with jobs:<br>" + JSONUtils.getJsonErrorMessage(caught));
            }
        });
    }

    public void putThirdPartyCredential(final String key, String value) {
        scheduler.putThirdPartyCredential(LoginModel.getInstance().getSessionId(), key, value, new AsyncCallback<Void>() {

            @Override
            public void onFailure(Throwable caught) {
                String msg = JSONUtils.getJsonErrorMessage(caught);
                LogModel.getInstance().logImportantMessage("Error while saving third-party credential: " + msg);
            }

            @Override
            public void onSuccess(Void result) {
                LogModel.getInstance().logMessage("Successfully saved third-party credential for " + key + ".");
                refreshThirdPartyCredentialsKeys();
            }
        });
    }

    public void refreshThirdPartyCredentialsKeys() {
        scheduler.thirdPartyCredentialKeySet(LoginModel.getInstance().getSessionId(), new AsyncCallback<Set<String>>() {
            @Override
            public void onFailure(Throwable caught) {
                String msg = JSONUtils.getJsonErrorMessage(caught);
                LogModel.getInstance().logImportantMessage("Error while getting third-party credentials: " + msg);
            }

            @Override
            public void onSuccess(Set<String> result) {
                model.setThirdPartyCredentialsKeys(result);
            }
        });
    }

    public void removeThirdPartyCredential(final String key) {
        scheduler.removeThirdPartyCredential(LoginModel.getInstance().getSessionId(), key, new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                String msg = JSONUtils.getJsonErrorMessage(caught);
                LogModel.getInstance().logImportantMessage("Error while deleting third-party credential: " + msg);
            }

            @Override
            public void onSuccess(Void result) {
                LogModel.getInstance().logMessage("Successfully deleted third-party credential for " + key + ".");
                refreshThirdPartyCredentialsKeys();
            }
        });
    }



    public void resetPendingTasksRequests(){
        this.taskOutputRequests.clear();
        this.tasksController.resetPendingTasksRequests();
    }

    public TasksController getTasksController() {
        return tasksController;
    }

    public void setTasksController(TasksController tasksController) {
        this.tasksController = tasksController;
    }
    
    
    public void cancelOutputRequests(){
        for (Request req : this.taskOutputRequests.values()) {
            req.cancel();
        }
    }

    public ExecutionsController getExecutionController() {
        return executionController;
    }
    
}