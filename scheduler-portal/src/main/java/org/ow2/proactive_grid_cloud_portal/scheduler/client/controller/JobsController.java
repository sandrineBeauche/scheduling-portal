package org.ow2.proactive_grid_cloud_portal.scheduler.client.controller;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

import org.ow2.proactive_grid_cloud_portal.common.client.Controller;
import org.ow2.proactive_grid_cloud_portal.common.client.EventDispatcher;
import org.ow2.proactive_grid_cloud_portal.common.client.Model;
import org.ow2.proactive_grid_cloud_portal.common.client.Settings;
import org.ow2.proactive_grid_cloud_portal.scheduler.client.Job;
import org.ow2.proactive_grid_cloud_portal.scheduler.client.SchedulerServiceAsync;
import org.ow2.proactive_grid_cloud_portal.scheduler.client.model.JobsModel;
import org.ow2.proactive_grid_cloud_portal.scheduler.client.model.PaginationModel;
import org.ow2.proactive_grid_cloud_portal.scheduler.client.model.SchedulerModel;
import org.ow2.proactive_grid_cloud_portal.scheduler.shared.SchedulerConfig;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class JobsController extends Controller implements PaginatedController{

	private SchedulerServiceAsync scheduler = null;
	
	private SchedulerController schedulerController;
	
	private PaginationController paginationController;
	
	private JobsModel model = null;
	
	private SchedulerModel schedulerModel = null;
	
	/** periodically updates the local job view */
    private Timer jobsUpdater = null;
    
    /** ids of jobs we should auto fetch */
    private Set<String> liveOutputJobs = null;
    
 // incremented each time #fetchJobs is called
    private int timerUpdate = 0;
	
	
	public JobsController(SchedulerController schedulerController) {
		this.schedulerController = schedulerController;
        this.liveOutputJobs = new HashSet<String>();
	}
	
	@Override
	public void fetch() {
		this.model.empty();
		this.fetchJobs();
	}
	
	/**
     * Fetch the complete JobBag from the server,
     * update the local scheduler revision number,
     * update the model and views
     * fail hard
     */
    public void fetchJobs() {
        final long t1 = System.currentTimeMillis();
        model.jobsUpdating();

        int offset = this.paginationController.getOffset();
        int range = this.paginationController.getRange();

        scheduler.revisionAndjobsinfo(schedulerModel.getSessionId(), offset, range, model.isFetchMyJobsOnly(),
                model.isFetchPendingJobs(), model.isFetchRunningJobs(), model.isFetchFinishedJobs(),
                new AsyncCallback<String>() {

                    public void onFailure(Throwable caught) {
                        if (!schedulerModel.isLoggedIn()) {
                            // might have been disconnected in between
                            return;
                        }
                        int httpErrorCodeFromException = getJsonErrorCode(caught);
                        if (httpErrorCodeFromException == Response.SC_UNAUTHORIZED) {
                            schedulerController.teardown("You have been disconnected from the server.");
                        } else if (httpErrorCodeFromException == Response.SC_FORBIDDEN) {
                            schedulerController.logImportantMessage(
                                    "Failed to fetch jobs because of permission (automatic refresh will be disabled)"
                                            + getJsonErrorMessage(caught));
                            stopTimer();
                            // display empty message in jobs view
                            model.empty();
                        } else {
                            error("Error while fetching jobs:\n" + getJsonErrorMessage(caught));
                        }
                    }

                    public void onSuccess(String result) {
                        long rev;
                        LinkedHashMap<Integer, Job> jobs;

                        JSONValue jsonVal = parseJSON(result);
                        JSONObject jsonInfo = jsonVal.isObject();
                        if (jsonInfo == null) {
                            error("Expected JSON Object: " + result);
                        }

                        String key = jsonInfo.keySet().iterator().next();
                        rev = Long.parseLong(key);

                        JSONArray jsonArr = jsonInfo.get(key).isArray();
                        if (jsonArr == null)
                            error("Expected JSONArray: " + jsonInfo.toString());

                        jobs = getJobsFromJson(jsonArr);

                        // if the selected job has changed and autorefresh is enabled, fetch task details
                        if(SchedulerController.this.model.getTaskAutoRefreshOption()){
                        	Job oldSel = model.getSelectedJob();
                        	if (oldSel != null) {
                        		Job newSel = jobs.get(oldSel.getId());
                        		if (newSel != null && !newSel.isEqual(oldSel)) {
                        			updateTasks(model.getCurrentTagFilter());
                        		}
                        	}
                        }

                        JobsController.this.model.setJobs(jobs, rev);
                        // do not model.logMessage() : this is repeated by a timer

                        int jn = jobs.size();
                        if (jn > 0) {
                            long t = (System.currentTimeMillis() - t1);
                            model.logMessage("<span style='color:gray;'>Fetched " + jn + " jobs in " + t +
                                " ms</span>");
                        }
                    }
                });
    }
    
    
    /**
     * Invalidates the current job list if toggling state,
     * refetch immediately a new job list
     * 
     * @param b true to fetch only jobs submitted by the current user, or false to fetch all jobs
     */
    public void fetchMyJobsOnly(boolean b) {
        if (b == model.isFetchMyJobsOnly())
            return;

        model.fetchMyJobsOnly(b);

        if (b)
            this.schedulerController.logMessage("Fetching only my jobs");
        else
            this.schedulerController.logMessage("Fetching all jobs");

        this.paginationController.resetPage();
    }

    /**
     * Invalidates the current job list if toggling state,
     * refetch immediately a new job list
     * 
     * @param f true to fetch pending jobs
     */
    public void fetchPending(boolean f) {
        if (f == model.isFetchPendingJobs())
            return;

        model.fetchPending(f);

        if (f)
            schedulerController.logMessage("Fetching pending jobs");
        else
            schedulerController.logMessage("Dot not fetch pending jobs");

        this.paginationController.resetPage();
    }

    /**
     * Invalidates the current job list if toggling state,
     * refetch immediately a new job list
     * 
     * @param f true to fetch running jobs
     */
    public void fetchRunning(boolean f) {
        if (f == model.isFetchRunningJobs())
            return;

        model.fetchRunning(f);

        if (f)
            schedulerController.logMessage("Fetching running jobs");
        else
            schedulerController.logMessage("Dot not fetch running jobs");

        this.paginationController.resetPage();
    }
    
    
    /**
     * Invalidates the current job list if toggling state,
     * refetch immediately a new job list
     * 
     * @param f true to fetch finished jobs
     */
    public void fetchFinished(boolean f) {
        if (f == model.isFetchFinishedJobs())
            return;

        model.fetchFinished(f);

        if (f)
            schedulerController.logMessage("Fetching finished jobs");
        else
            schedulerController.logMessage("Dot not fetch finished jobs");

        this.paginationController.resetPage();
    }
    
    
    /**
     * Override user settings, rewrite cookies, refresh corresponding ui elements
     * 
     * @param refreshTime refresh time for update thread in ms
     * @param pageSize number of results per job list page
     * @param liveLogTime refresh time for livelog update thread in ms
     * @param forceRefresh refresh ui even if properties did not change
     */
    public void setUserSettings(String refreshTime, String pageSize, String liveLogTime, boolean forceRefresh) {z
        boolean pageChanged = !pageSize.equals("" + SchedulerConfig.get().getJobsPageSize());
        SchedulerConfig.get().set(SchedulerConfig.JOBS_PAGE_SIZE, pageSize);
        Settings.get().setSetting(SchedulerConfig.JOBS_PAGE_SIZE, pageSize);

        boolean logChanged = !liveLogTime.equals("" + SchedulerConfig.get().getLivelogsRefreshTime());
        SchedulerConfig.get().set(SchedulerConfig.LIVELOGS_REFRESH_TIME, liveLogTime);
        Settings.get().setSetting(SchedulerConfig.LIVELOGS_REFRESH_TIME, liveLogTime);

        if (refreshChanged || forceRefresh) {
            this.stopTimer();
            this.startTimer();
        }
        if (pageChanged || forceRefresh) {
            resetPage();
        }
        if (logChanged || forceRefresh) {
            this.stopLiveTimer();
            this.startLiveTimer();
        }
    }

	@Override
	public Model getModel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EventDispatcher getEventDispatcher() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void login(String sessionId, String login) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getLoginSettingKey() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getLogo350Url() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	/**
     * Job selection has changed, notify the views
     *
     * @param jobId of the new job selection. you can use null to cancel the current selection
     */
    public void selectJob(final String jobId) {
        int id = 0;
        if (jobId != null)
            id = Integer.parseInt(jobId);
        
        this.model.selectJob(id);
        this.schedulerController.getTasksController().selectedJob(jobId);
    }
}
