package org.ow2.proactive_grid_cloud_portal.scheduler.client.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.ow2.proactive_grid_cloud_portal.scheduler.client.Job;
import org.ow2.proactive_grid_cloud_portal.scheduler.client.JobOutput;
import org.ow2.proactive_grid_cloud_portal.scheduler.client.JobStatus;
import org.ow2.proactive_grid_cloud_portal.scheduler.client.SchedulerListeners.JobOutputListener;
import org.ow2.proactive_grid_cloud_portal.scheduler.client.SchedulerListeners.JobSelectedListener;
import org.ow2.proactive_grid_cloud_portal.scheduler.client.SchedulerListeners.JobsUpdatedListener;
import org.ow2.proactive_grid_cloud_portal.scheduler.client.SchedulerListeners.VisualizationListener;
import org.ow2.proactive_grid_cloud_portal.scheduler.client.Task;
import org.ow2.proactive_grid_cloud_portal.scheduler.shared.JobVisuMap;

public class JobsModel implements PaginatedModel, JobsEventDispatcher{

	
	
	private LinkedHashMap<Integer, Job> jobs = null;
    private long jobsRev = -1;
    private Job selectedJob = null;
    
    private boolean fetchMyJobsOnly = false;
    private boolean fetchPending = true;
    private boolean fetchRunning = true;
    private boolean fetchFinished = true;
    
    private HashMap<Integer, JobOutput> output = null;
    private HashSet<String> isLiveOutput = null;
    
    private HashMap<String, StringBuffer> liveOutput = null;
    
    private Map<String, JobVisuMap> visuMap = null;
    
    private Map<String, String> imagePath = null;
    
    private Map<String, String> htmlMap = null;
    
    private ArrayList<JobsUpdatedListener> jobsUpdatedListeners = null;
    private ArrayList<JobSelectedListener> jobSelectedListeners = null;
    private ArrayList<JobOutputListener> jobOutputListeners = null;
    private ArrayList<VisualizationListener> visuListeners = null;
    
    
    protected SchedulerModel schedulerModel;
	
    
    public JobsModel(SchedulerModel schedulerModel) {
    	this.jobsUpdatedListeners = new ArrayList<JobsUpdatedListener>();
        this.jobSelectedListeners = new ArrayList<JobSelectedListener>();
        this.jobOutputListeners = new ArrayList<JobOutputListener>();
        this.visuListeners = new ArrayList<VisualizationListener>();
        this.output = new HashMap<Integer, JobOutput>();
        this.isLiveOutput = new HashSet<String>();
        this.liveOutput = new HashMap<String, StringBuffer>();
        this.visuMap = new HashMap<String, JobVisuMap>();
        this.imagePath = new HashMap<String, String>();
        this.htmlMap = new HashMap<String, String>();
        this.schedulerModel = schedulerModel;
	}
    
	@Override
	public void empty() {
		setJobs(null, -1);
	}
	
	
	
	public LinkedHashMap<Integer, Job> getJobs() {
        return this.jobs;
    }
	
	
	/**
     * Modifies the local joblist
     * triggers {@link JobsUpdatedListener#jobsUpdated(java.util.Map)}},
     * or {@link JobsUpdatedListener#jobsUpdating()} if <code>jobs</code> was null
     * 
     * @param jobs a jobset, or null
     * @param rev the revision of this jobset
     */
    public void setJobs(LinkedHashMap<Integer, Job> jobs, long rev) {
        this.jobs = jobs;
        this.jobsRev = rev;
        boolean empty = false;

        if (jobs == null) {
            empty = true;
            this.jobs = new LinkedHashMap<Integer, Job>();
        }

        for (JobsUpdatedListener listener : this.jobsUpdatedListeners) {
            listener.jobsUpdated(this.jobs);
            if (empty)
                listener.jobsUpdating();
        }
    }

    public void jobSubmitted(Job j) {
        for (JobsUpdatedListener listener : this.jobsUpdatedListeners) {
            listener.jobSubmitted(j);
        }
    }

    public void jobsUpdating() {
        for (JobsUpdatedListener listener : this.jobsUpdatedListeners) {
            listener.jobsUpdating();
        }
    }

    /**
     * Modifies the Job selection,
     * triggers a JobSelected event
     *
     */
    public void selectJob(int jobId) {
        Job j = null;
        // find the job
        for (Job it : this.jobs.values()) {
            if (it.getId() == jobId) {
                j = it;
            }
        }
        boolean selChanged = this.selectedJob == null || !this.selectedJob.equals(j);
        this.selectedJob = j;

        ((TasksModelImpl) this.schedulerModel.getTasksModel()).resetTagSuggestions();
        
        // notify job selection listeners
        for (JobSelectedListener listener : this.jobSelectedListeners) {
            if (j == null)
                listener.jobUnselected();
            else
                listener.jobSelected(j);
        }

        // tasks list will change, notify tasks listeners
        ((TasksModelImpl) this.schedulerModel.getTasksModel()).notifyTaskUpdating(selChanged, j);
    }
    
    
    
    public Job getSelectedJob() {
        return this.selectedJob;
    }

   
    public Job getJob(int jobId) {
        for (Job j : this.jobs.values()) {
            if (j.getId() == jobId) {
                return j;
            }
        }
        return null;
    }

    
    public long getJobsRevision() {
        return this.jobsRev;
    }
    
    
    
    public boolean isFetchMyJobsOnly() {
        return fetchMyJobsOnly;
    }

    public void fetchMyJobsOnly(boolean b) {
        this.fetchMyJobsOnly = b;
    }

   
    public boolean isFetchPendingJobs() {
        return this.fetchPending;
    }

    public void fetchPending(boolean f) {
        this.fetchPending = f;
    }

    
    public boolean isFetchRunningJobs() {
        return this.fetchRunning;
    }

    public void fetchRunning(boolean f) {
        this.fetchRunning = f;
    }

   
    public boolean isFetchFinishedJobs() {
        return this.fetchFinished;
    }

    public void fetchFinished(boolean f) {
        this.fetchFinished = f;
    }
    
    
   
    public void addJobsUpdatedListener(JobsUpdatedListener listener) {
        this.jobsUpdatedListeners.add(listener);
    }

    
    public void addJobSelectedListener(JobSelectedListener listener) {
        this.jobSelectedListeners.add(listener);
    }
    
    
    
    public JobOutput getJobOutput(int jobId) {
        return this.output.get(jobId);
    }
    
    
    /**
     * Notify listeners that the output of a given job has changed
     * 
     * @param jobId the job for which the output changed
     */
    public void updateOutput(int jobId) {
        if (this.output.get(jobId) == null) {
            JobOutput jo = new JobOutput(jobId);
            this.output.put(jobId, jo);
        }

        for (JobOutputListener listener : this.jobOutputListeners) {
            listener.jobOutputUpdated(this.output.get(jobId));
        }
    }
    
    

    public void updateJobOutput(int jobId, Task task, List<String> lines){
    	if (this.output.get(jobId) == null) {
            JobOutput jo = new JobOutput(jobId);
            jo.update(task, lines);
            this.output.put(jobId, jo);
        } else {
            this.output.get(jobId).update(task, lines);
        }

        this.updateOutput(jobId);
    }
    
    
    /**
     * Append a job output fragment to the stored live output
     * @param jobId id of the job to which this fragment belongs
     * @param out job output fragment
     */
    void appendLiveOutput(String jobId, String out) {
        String[] expl = ModelUtils.lineByLine(out);
        out = "";
        for (String str : expl) {
        	((TasksModelImpl) this.schedulerModel.getTasksModel()).addRemoteHintIfNecessary(str);
            out += ModelUtils.formatLine(str);
        }

        StringBuffer buf = this.liveOutput.get(jobId);
        if (buf == null) {
            buf = new StringBuffer();
            this.liveOutput.put(jobId, buf);
        }
        buf.append(out);

        for (JobOutputListener list : this.jobOutputListeners) {
            list.liveOutputUpdated(jobId, buf.toString());
        }
    }
    
    
    public boolean isLiveOutput(String jobId) {
        return this.isLiveOutput.contains(jobId);
    }

    /**
     * The output for this job should be fetched live
     * @param jobId id of the job
     * @param isLiveOutput true to live fetch
     */
    void setLiveOutput(String jobId, boolean isLiveOutput) {
        if (isLiveOutput) {
            this.isLiveOutput.add(jobId);
            if(!liveOutput.containsKey(jobId)){
                this.liveOutput.put(jobId, new StringBuffer());
            }
        } else {
            this.isLiveOutput.remove(jobId);
        }
    }
    
    
    public String getLiveOutput(String jobId) {
        StringBuffer buf = this.liveOutput.get(jobId);
        if (buf == null) {
            return "";
        } else {
            return buf.toString();
        }
    }
    
    /*
     * (non-Javadoc)
     * @see org.ow2.proactive_grid_cloud_portal.client.EventDispatcher#addJobOutputListener(org.ow2.proactive_grid_cloud_portal.client.Listeners.JobOutputListener)
     */
    public void addJobOutputListener(JobOutputListener listener) {
        this.jobOutputListeners.add(listener);
    }
    
    
    public void addVisualizationListener(VisualizationListener listener) {
        this.visuListeners.add(listener);
    }
    
    public void visuUnavailable(String jobId) {
        for (VisualizationListener list : visuListeners) {
            list.visualizationUnavailable(jobId);
        }
    }
    
    public JobVisuMap getJobVisuMap(String jobId) {
        return this.visuMap.get(jobId);
    }

    void setJobVisuMap(String jobId, JobVisuMap map) {
        this.visuMap.put(jobId, map);

        for (VisualizationListener list : this.visuListeners) {
            list.mapUpdated(jobId, map);
        }
    }
    
    public void setJobImagePath(String jobId, String path){
    	this.imagePath.put(jobId, path);
    	for (VisualizationListener list : this.visuListeners) {
            list.imageUpdated(jobId, path);
        }
    }
    
    public String getJobImagePath(String jobId) {
        return this.imagePath.get(jobId);
    }
    
    
    public String getJobHtml(String jobId) {
        return this.htmlMap.get(jobId);
    }

    
    public void setJobHtml(String jobId, String curHtml) {
        this.htmlMap.put(jobId, curHtml);

        for (VisualizationListener list : this.visuListeners) {
            list.htmlUpdated(jobId, curHtml);
        }
    }
    
    
    public JobStatus getJobStatus(int jobId) {
        for (Job j : this.jobs.values()) {
            if (jobId == j.getId())
                return j.getStatus();
        }
        return null;
    }
    
}
