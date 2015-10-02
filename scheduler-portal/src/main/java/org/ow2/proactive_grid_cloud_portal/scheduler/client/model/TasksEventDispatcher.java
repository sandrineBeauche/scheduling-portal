package org.ow2.proactive_grid_cloud_portal.scheduler.client.model;

import org.ow2.proactive_grid_cloud_portal.scheduler.client.SchedulerListeners.TagSuggestionListener;
import org.ow2.proactive_grid_cloud_portal.scheduler.client.SchedulerListeners.TasksUpdatedListener;

public interface TasksEventDispatcher {

	void addTasksUpdatedListener(TasksUpdatedListener listener);
	
	void addTagSuggestionListener(TagSuggestionListener listener);
}
