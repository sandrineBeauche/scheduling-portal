package org.ow2.proactive_grid_cloud_portal.scheduler.client.model;

import java.util.Collection;
import java.util.List;

import org.ow2.proactive_grid_cloud_portal.scheduler.client.Task;
import org.ow2.proactive_grid_cloud_portal.scheduler.client.controller.PrefixWordSuggestOracle.TagSuggestion;

public interface TaskModel {

	public Collection<TagSuggestion> getAvailableTags(String query);

	public List<Task> getTasks();

	public boolean isTasksDirty();

	public String getCurrentTagFilter();
	
	public SchedulerModel getSchedulerModel(); 
	
	public void addRemoteHintIfNecessary(String line);

}