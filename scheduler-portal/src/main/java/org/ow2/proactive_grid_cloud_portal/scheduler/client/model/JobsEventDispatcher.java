package org.ow2.proactive_grid_cloud_portal.scheduler.client.model;

import org.ow2.proactive_grid_cloud_portal.scheduler.client.SchedulerListeners.JobSelectedListener;
import org.ow2.proactive_grid_cloud_portal.scheduler.client.SchedulerListeners.JobsUpdatedListener;

public interface JobsEventDispatcher {

	void addJobsUpdatedListener(JobsUpdatedListener listener);

    void addJobSelectedListener(JobSelectedListener listener);

}
