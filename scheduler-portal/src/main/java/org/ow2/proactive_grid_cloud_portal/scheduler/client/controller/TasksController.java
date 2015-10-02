package org.ow2.proactive_grid_cloud_portal.scheduler.client.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ow2.proactive_grid_cloud_portal.common.client.Controller;
import org.ow2.proactive_grid_cloud_portal.scheduler.client.SchedulerServiceAsync;
import org.ow2.proactive_grid_cloud_portal.scheduler.client.Task;
import org.ow2.proactive_grid_cloud_portal.scheduler.client.model.TasksModelImpl;

import com.google.gwt.http.client.Request;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.SuggestOracle;

public class TasksController {

	private TasksModelImpl model;
	
	private PrefixWordSuggestOracle tagSuggestionOracle;
	
	private SchedulerController schedulerController;
	
	private PaginationController paginationController;
	
	/** do not fetch visualization info when false */
    private boolean visuFetchEnabled = false;
    
    /** contains all pending getTaskOutput requests, taskId as key */
    private Map<String, Request> taskOutputRequests = null;
    /** pending taskUpdate request, or null */
    private Request taskUpdateRequest = null;
	
	/** scheduler server for async rpc calls */
    private SchedulerServiceAsync scheduler = null;
	
	public TasksController(SchedulerController schedulerController){
		this.schedulerController = schedulerController;
		this.tagSuggestionOracle = new PrefixWordSuggestOracle(this.model, this.scheduler);
		this.taskOutputRequests = new HashMap<String, Request>();
	}
	
	
	/**
     * Updates the current task list depending the current job selection in the model 
     */
    private void updateTasks(String tagFilter) {
        if (model.getSelectedJob() == null) {
            this.model.setTasks(new ArrayList<Task>());
        } else {
            final String jobId = "" + model.getSelectedJob().getId();

            AsyncCallback<String> callback = new AsyncCallback<String>() {

                public void onFailure(Throwable caught) {
                    String msg = Controller.getJsonErrorMessage(caught);

                    TasksController.this.model.taskUpdateError(msg);
                    TasksController.this.schedulerController.logImportantMessage("Failed to update tasks for job " +
                        jobId + ": " + msg);
                }

                public void onSuccess(String result) {
                    List<Task> tasks;

                    JSONValue val = parseJSON(result);
                    JSONArray arr = val.isArray();
                    if (arr == null) {
                        error("Expected JSON Array: " + val.toString());
                    }
                    tasks = getTasksFromJson(arr);

                    TasksController.this.model.setTasksDirty(false);
                    TasksController.this.model.setTasks(tasks);
                    // do not model.logMessage() : this is repeated by a timer
                }
            };
            
            int offset = this.paginationController.getOffset();
            int limit = this.paginationController.getRange();
            if(tagFilter.equals("")){
            	this.taskUpdateRequest = this.scheduler.getTasks(model.getSessionId(), jobId, offset, limit, callback);
            }
            else{
            	this.taskUpdateRequest = this.scheduler.getTasksByTag(model.getSessionId(), jobId, tagFilter, offset, limit, callback);
            }
        }
    }
    
    
    public void setTaskTagFilter(String tag){
    	boolean changed = this.model.setCurrentTagFilter(tag);
    	if(changed){
    		this.taskOutputRequests.clear();
    		if (this.taskUpdateRequest != null) {
    			this.taskUpdateRequest.cancel();
    			this.taskUpdateRequest = null;
    		}
    		
    		if(tag != ""){
    			updateTasks(tag);
    		}
    	}
    }
    
    
    SuggestOracle getTagSuggestionOracle(){
    	return this.tagSuggestionOracle;
    }
    
    
    void setVisuFetchEnabled(boolean b) {
        this.visuFetchEnabled = b;
    }

    void visuFetch(final String jobId) {

        // fetch visu info
        if (jobId != null) {
            String curHtml = model.getJobHtml(jobId);
            if (curHtml != null) {
                // exists already, resetting it will trigger listeners
                model.setJobHtml(jobId, curHtml);
            } else {
                final long t = System.currentTimeMillis();
                this.scheduler.getJobHtml(model.getSessionId(), jobId, new AsyncCallback<String>() {
                    public void onSuccess(String result) {
                        model.setJobHtml(jobId, result);
                        model.logMessage("Fetched html for job " + jobId + " in " +
                                (System.currentTimeMillis() - t) + " ms");
                    }

                    public void onFailure(Throwable caught) {
                        String msg = "Failed to fetch html for job " + jobId;
                        String json = getJsonErrorMessage(caught);
                        if (json != null)
                            msg += " : " + json;

                        model.logImportantMessage(msg);

                        // trying to load image
                        String curPath = model.getJobImagePath(jobId);
                        if (curPath != null) {
                            // exists already, resetting it will trigger listeners
                            model.setJobImagePath(jobId, curPath);
                        } else {
                            final long t = System.currentTimeMillis();
                            scheduler.getJobImage(model.getSessionId(), jobId, new AsyncCallback<String>() {
                                public void onSuccess(String result) {
                                    model.setJobImagePath(jobId, result);
                                    model.logMessage("Fetched image for job " + jobId + " in " +
                                            (System.currentTimeMillis() - t) + " ms");
                                }

                                public void onFailure(Throwable caught) {
                                    String msg = "Failed to fetch image for job " + jobId;
                                    String json = getJsonErrorMessage(caught);
                                    if (json != null)
                                        msg += " : " + json;

                                    model.logImportantMessage(msg);
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
     * Fetch the previous task list page
     * invalidate the current page, set the jobs views in indeterminate mode
     */
    public void previousTaskPage() {
        int curPage = model.getTaskPage();
        if (curPage == 0)
            return;
        model.setTaskPage(curPage - 1);
        this.updateTasks(model.getCurrentTagFilter());
    }
    
    
    public void setTaskAutoRefreshOption(boolean value){
    	this.model.setTaskAutoRefreshOption(value);
    }
    
    
    
    /**
     * Kill a task within a job 
     * @param jobId job id
     * @param taskName task name
     */
    public void killTask(final Integer jobId, final String taskName) {

        this.scheduler.killTask(model.getSessionId(), jobId, taskName, new AsyncCallback<Boolean>() {
            @Override
            public void onFailure(Throwable caught) {
                caught.printStackTrace();

                String msg = getJsonErrorMessage(caught);
                model.logImportantMessage("Failed to kill task: " + msg);
            }

            @Override
            public void onSuccess(Boolean result) {
                model.logMessage("Successfully killed task " + taskName + " in job " + jobId);
            }
        });
    }

    /**
     * Restart a task within a job 
     * @param jobId job id
     * @param taskName task name
     */
    public void restartTask(final Integer jobId, final String taskName) {
        this.scheduler.restartTask(model.getSessionId(), jobId, taskName, new AsyncCallback<Boolean>() {
            @Override
            public void onFailure(Throwable caught) {

                caught.printStackTrace();
                String msg = getJsonErrorMessage(caught);
                model.logImportantMessage("Failed to restart task: " + msg);
            }

            @Override
            public void onSuccess(Boolean result) {
                model.logMessage("Successfully restarted task " + taskName + " in job " + jobId);
            }
        });
    }

    /**
     * Preempt a task within a job 
     * @param jobId job id
     * @param taskName task name
     */
    public void preemptTask(final Integer jobId, final String taskName) {
        this.scheduler.preemptTask(model.getSessionId(), jobId, taskName, new AsyncCallback<Boolean>() {
            @Override
            public void onFailure(Throwable caught) {
                String msg = getJsonErrorMessage(caught);
                model.logImportantMessage("Failed to preempt task: " + msg);
            }

            @Override
            public void onSuccess(Boolean result) {
                model.logMessage("Successfully preempted task " + taskName + " in job " + jobId);
            }
        });
    }
    
    
    public void selectedJob(String jobId){
    	if (model.getSelectedJob() == null || id != model.getSelectedJob().getId()) {
            for (Request req : this.taskOutputRequests.values()) {
                req.cancel();
            }
            this.taskOutputRequests.clear();
            if (this.taskUpdateRequest != null) {
                this.taskUpdateRequest.cancel();
                this.taskUpdateRequest = null;
            }
        }
    	
    	this.model.setTasksDirty(true);
        this.updateTasks("");

        if (visuFetchEnabled) {
            visuFetch(jobId);
        }
    }
}
