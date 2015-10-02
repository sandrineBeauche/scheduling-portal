package org.ow2.proactive_grid_cloud_portal.scheduler.client.controller;

import org.ow2.proactive_grid_cloud_portal.scheduler.client.model.PaginatedModel;
import org.ow2.proactive_grid_cloud_portal.scheduler.shared.SchedulerConfig;

public class JobsPaginationController extends PaginationController {

	public JobsPaginationController(PaginatedModel paginatedModel) {
		super(paginatedModel, SchedulerConfig.JOBS_PAGE_SIZE);
	}

	@Override
	public void fetch() {
		// TODO Auto-generated method stub

	}

}
