package org.ow2.proactive_grid_cloud_portal.fixture;

import java.io.InputStream;

import org.jboss.resteasy.client.ClientResponse;
import javax.ws.rs.core.Response;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.ow2.proactive_grid_cloud_portal.scheduler.server.RestClient;

public class SchedulerFuxture implements RestClient {

	@Override
	public ClientResponse<Void> disconnect(String sessionId) {
		return null;
	}

	@Override
	public ClientResponse<InputStream> jobs(String sessionId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClientResponse<String> jobs(String sessionId,
			MultipartInput multipart) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClientResponse<String> submitFlat(String sessionId,
			String commandFileContent, String jobName,
			String selectionScriptContent, String selectionScriptExtension) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClientResponse<InputStream> removeJob(String sessionId, String jobId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClientResponse<InputStream> pauseJob(String sessionId, String jobId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClientResponse<InputStream> resumeJob(String sessionId, String jobId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClientResponse<InputStream> killJob(String sessionId, String jobId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClientResponse<InputStream> killTask(String sessionId, String jobId,
			String taskName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClientResponse<InputStream> preemptTask(String sessionId,
			String jobId, String taskName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClientResponse<InputStream> restartTask(String sessionId,
			String jobId, String taskName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClientResponse<InputStream> getJobTaskStates(String sessionId,
			String jobId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClientResponse<InputStream> getJobTaskStatesByTag(String sessionId,
			String jobId, String tag) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClientResponse<InputStream> getJobTaskTagsPrefix(String sessionId,
			String jobId, String prefix) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClientResponse<InputStream> job(String sessionId, String jobId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClientResponse<InputStream> schedulerChangeJobPriorityByName(
			String sessionId, String jobId, String priorityName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClientResponse<InputStream> pauseScheduler(String sessionId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClientResponse<InputStream> resumeScheduler(String sessionId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClientResponse<InputStream> freezeScheduler(String sessionId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClientResponse<InputStream> killScheduler(String sessionId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClientResponse<InputStream> startScheduler(String sessionId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClientResponse<InputStream> stopScheduler(String sessionId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClientResponse<InputStream> getJobTasksIds(String sessionId,
			String jobId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClientResponse<String> tasklog(String sessionId, String jobId,
			String taskId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClientResponse<String> taskStdout(String sessionId, String jobId,
			String taskId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClientResponse<String> taskStderr(String sessionId, String jobId,
			String taskId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClientResponse<String> getLiveLogJob(String sessionId, String jobId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClientResponse<String> getLiveLogJobAvailable(String sessionId,
			String jobId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClientResponse<InputStream> deleteLiveLogJob(String sessionId,
			String jobId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClientResponse<String> taskServerLogs(String sessionId,
			String jobId, String taskId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClientResponse<String> jobServerLogs(String sessionId, String jobId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClientResponse<InputStream> taskresult(String sessionId,
			String jobId, String taskId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClientResponse<String> getStatistics(String sessionId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClientResponse<String> getStatisticsOnMyAccount(String sessionId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClientResponse<String> schedulerStateRevision(String sessionId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClientResponse<InputStream> revisionAndjobsinfo(String sessionId,
			int index, int range, boolean myJobs, boolean pending,
			boolean running, boolean finished) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClientResponse<InputStream> getJobHtml(String sessionId, String jobId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClientResponse<InputStream> getSchedulerUsers(String sessionId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClientResponse<InputStream> getSchedulerUsersWithJobs(
			String sessionId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClientResponse<String> schedulerStatus(String sessionId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClientResponse<InputStream> getVersion() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClientResponse<InputStream> getUsageOnMyAccount(String sessionId,
			String startDate, String endDate) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClientResponse<InputStream> getUsageOnAccount(String sessionId,
			String user, String startDate, String endDate) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClientResponse<Void> putThirdPartyCredential(String sessionId,
			String key, String value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClientResponse<Void> removeThirdPartyCredential(String sessionId,
			String key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClientResponse<InputStream> thirdPartyCredentialsKeySet(
			String sessionId) {
		// TODO Auto-generated method stub
		return null;
	}

}
