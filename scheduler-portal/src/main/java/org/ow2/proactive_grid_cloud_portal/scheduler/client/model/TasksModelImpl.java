package org.ow2.proactive_grid_cloud_portal.scheduler.client.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import org.apache.commons.collections4.trie.PatriciaTrie;
import org.ow2.proactive_grid_cloud_portal.scheduler.client.Job;
import org.ow2.proactive_grid_cloud_portal.scheduler.client.JobStatus;
import org.ow2.proactive_grid_cloud_portal.scheduler.client.SchedulerListeners.RemoteHintListener;
import org.ow2.proactive_grid_cloud_portal.scheduler.client.SchedulerListeners.TagSuggestionListener;
import org.ow2.proactive_grid_cloud_portal.scheduler.client.SchedulerListeners.TasksUpdatedListener;
import org.ow2.proactive_grid_cloud_portal.scheduler.client.Task;
import org.ow2.proactive_grid_cloud_portal.scheduler.client.controller.PrefixWordSuggestOracle.TagSuggestion;
import org.ow2.proactive_grid_cloud_portal.scheduler.client.model.SchedulerModel.RemoteHint;

public class TasksModelImpl implements PaginatedModel, SelectedJobModel, TasksEventDispatcher, TaskModel{

	
	private static final String PA_REMOTE_CONNECTION = "PA_REMOTE_CONNECTION";
	
	protected Job selectedJob;
	
	private ArrayList<TagSuggestionListener> tagSuggestionListeners = null;
	
	private String tasksTagFilter = "";
	
    private PatriciaTrie<String> availableTags = null;
    
    private List<Task> selectedTasks = null;
    
    private boolean tasksDirty = false;
    
    private boolean taskAutoRefreshOption = false;
    
    private List<RemoteHint> remoteHints = null;
    
    
    
    
    private ArrayList<TasksUpdatedListener> tasksUpdatedListeners = null;
    
    private ArrayList<RemoteHintListener> remoteHintListeners = null;
    
    
    protected SchedulerModel schedulerModel;
	
	public TasksModelImpl(SchedulerModel schedulerModel) {
		this.tagSuggestionListeners = new ArrayList<TagSuggestionListener>();
		this.tasksUpdatedListeners = new ArrayList<TasksUpdatedListener>();
		this.remoteHintListeners = new ArrayList<RemoteHintListener>();
		
		this.availableTags = new PatriciaTrie<String>();
		this.remoteHints = new ArrayList<RemoteHint>();
		this.schedulerModel = schedulerModel;
		
	}
	
	
	@Override
	public void empty() {
		// TODO Auto-generated method stub
		
	}
	
	
	@Override
	public void fetch() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public Job getSelectedJob() {
		return this.selectedJob;
	}
	
	
	@Override
	public void setSelectedJob(Job selectedJob) {
		this.selectedJob = selectedJob;
	}
	
	
	public void resetTagSuggestions(){
		this.availableTags.clear();
	}
	
	
	public void setTagSuggestions(Collection<String> tags) {
    	for(String currentTag: tags){
    		int index = currentTag.indexOf("<index>");
    		if(index >= 0){
    			this.availableTags.put(currentTag, currentTag.substring(0, index));
    		}
    		else{
    			this.availableTags.put(currentTag, currentTag);
    		}
    	}
    	
    	for(TagSuggestionListener currentListener: this.tagSuggestionListeners){
    		currentListener.tagSuggestionListUpdated();
    	}
    }
	
	
	/* (non-Javadoc)
	 * @see org.ow2.proactive_grid_cloud_portal.scheduler.client.model.TaskModel#getAvailableTags(java.lang.String)
	 */
	@Override
	public Collection<TagSuggestion> getAvailableTags(String query) {
    	SortedMap<String, String> mapSuggestions = this.availableTags.prefixMap(query);
    	ArrayList<TagSuggestion> suggestions = new ArrayList<TagSuggestion>(20);
    	Iterator<Map.Entry<String, String>> it = mapSuggestions.entrySet().iterator();
    	for(int i = 0; i < 20 && it.hasNext(); i++){
    		Map.Entry<String, String> current = it.next();
    		TagSuggestion suggestion = new TagSuggestion(current.getValue(), current.getKey());
    		suggestions.add(suggestion);
    	}
    	
    	return suggestions;
    }
	
	
	/**
     * Modifies the tasks set
     * triggers TasksUpdated event
     * 
     * @param tasks the new TaskSet
     */
    public void setTasks(List<Task> tasks) {
        this.selectedTasks = tasks;
        for (TasksUpdatedListener list : this.tasksUpdatedListeners) {
            list.tasksUpdated(tasks);
        }
    }
    

    /**
     * Notify task updated listeners that updating failed
     * 
     * @param message the error message
     */
    public void taskUpdateError(String message) {
        this.selectedTasks = new ArrayList<Task>();
        for (TasksUpdatedListener list : this.tasksUpdatedListeners) {
            list.tasksUpdatedFailure(message);
        }
    }

    
    /* (non-Javadoc)
	 * @see org.ow2.proactive_grid_cloud_portal.scheduler.client.model.TaskModel#getTasks()
	 */
    @Override
	public List<Task> getTasks() {
        return this.selectedTasks;
    }

    
    /* (non-Javadoc)
	 * @see org.ow2.proactive_grid_cloud_portal.scheduler.client.model.TaskModel#isTasksDirty()
	 */
    @Override
	public boolean isTasksDirty() {
        return this.tasksDirty;
    }

    public void setTasksDirty(boolean b) {
        this.tasksDirty = b;
    }
    
    
    /**
     * Set the current tag used to filter the list of tasks.
     * @param tag the tag used to filter the list of tasks.
     * @return true if the tag value has changed, false otherwise.
     */
    public boolean setCurrentTagFilter(String tag){
    	boolean result = (!this.tasksTagFilter.equals(tag));
    	this.tasksTagFilter = tag;
    	if(result){
    		for (TasksUpdatedListener list : this.tasksUpdatedListeners) {
                list.tasksUpdating(true);
            }
    	}
    	return result;
    }
    
   
    /* (non-Javadoc)
	 * @see org.ow2.proactive_grid_cloud_portal.scheduler.client.model.TaskModel#getCurrentTagFilter()
	 */
    @Override
	public String getCurrentTagFilter() {
    	return this.tasksTagFilter;
    }
    
    
    
    public void addTasksUpdatedListener(TasksUpdatedListener listener) {
        this.tasksUpdatedListeners.add(listener);
    }
    
    
    
    public void addTagSuggestionListener(TagSuggestionListener listener) {
    	this.tagSuggestionListeners.add(listener);
    }
    
    
    public boolean getTaskAutoRefreshOption() {
    	return this.taskAutoRefreshOption;
    }
    
    
    public void setTaskAutoRefreshOption(boolean value) {
    	this.taskAutoRefreshOption = value;
    }
    
    
    public void notifyTaskUpdating(boolean selChanged, Job selectedJob){
    	for (TasksUpdatedListener list : this.tasksUpdatedListeners) {
            if (selectedJob == null)
                list.tasksUpdated(new ArrayList<Task>());
            else
                list.tasksUpdating(selChanged);
        }
    }
    
    @Override
    public SchedulerModel getSchedulerModel() {
    	// TODO Auto-generated method stub
    	return null;
    }
    
    
    /**
     * Set the output for a given task in a given job
     * 
     * notify listeners
     * 
     */
    void setTaskOutput(int jobId, Task task, String output) {
        JobStatus stat = this.schedulerModel.getJobsModel().getJobStatus(jobId);
        if (stat == null) {
            throw new IllegalStateException("Trying to set output for a task in job " + jobId +
                " for which there is no local representation");
        }

        List<String> lines = new ArrayList<String>();

        for (String line : ModelUtils.lineByLine(output)) {
            addRemoteHintIfNecessary(line);
            line = ModelUtils.formatLine(line);

            if (!line.trim().isEmpty()) {
                lines.add(line);
            }
        }

        
        this.schedulerModel.getJobsModel().updateJobOutput(jobId, task, lines);
    }
    
    
    public void addRemoteHintIfNecessary(String line) {
        if (line.contains(PA_REMOTE_CONNECTION)) {
            this.addRemoteHint(line);
        }
    }

    /**
     * Add a remote hint
     * will notify listeners if it is well formed
     * 
     * @param remoteHint a string containing PA_REMOTE_CONNECTION
     */
    public void addRemoteHint(String remoteHint) {
        String[] expl = remoteHint.split(PA_REMOTE_CONNECTION);
        if (expl.length < 2)
            return;

        expl = expl[1].split(";");
        if (expl.length < 4)
            return;

        RemoteHint rh = new RemoteHint();
        rh.taskId = expl[1];
        rh.type = expl[2];
        rh.argument = expl[3];

        this.remoteHints.add(rh);

        for (RemoteHintListener rhl : this.remoteHintListeners) {
            rhl.remoteHintRead(rh);
        }
    }
    
    public List<RemoteHint> getRemoteHints() {
        return this.remoteHints;
    }
    
    
    
    public void addRemoteHintListener(RemoteHintListener listener) {
        this.remoteHintListeners.add(listener);
    }
    
}
