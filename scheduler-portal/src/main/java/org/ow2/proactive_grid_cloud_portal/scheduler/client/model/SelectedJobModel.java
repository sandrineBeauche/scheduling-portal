package org.ow2.proactive_grid_cloud_portal.scheduler.client.model;

import org.ow2.proactive_grid_cloud_portal.scheduler.client.Job;

public interface SelectedJobModel {

	public Job getSelectedJob();
	
	
	public void setSelectedJob(Job selectedJob);
}
