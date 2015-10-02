package org.ow2.proactive_grid_cloud_portal.fixture;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.ow2.proactive_grid_cloud_portal.scheduler.client.Task;

public class TaskFixture extends Task implements JSONFixture{

	public JSONObject toJSON() throws JSONException{
		JSONObject result = new JSONObject();
		result.put("name", this.getName());
		JSONObject taskInfoResult = new JSONObject();
		taskInfoResult.put("executionHostName", this.getHostName());
		
		JSONObject taskIdResult = new JSONObject();
		taskIdResult.put("id", this.getId());
		taskInfoResult.put("taskId", taskIdResult);
		
		taskInfoResult.put("taskStatus", this.getStatus().toString());
		taskInfoResult.put("startTime", this.getStartTime());
		taskInfoResult.put("finishedTime", this.getFinishTime());
		taskInfoResult.put("executionDuration", this.getExecutionTime());
		
		result.put("taskInfo", taskIdResult);
		
		result.put("description", this.getDescription());
		result.put("tag", this.getTag());
		result.put("maxNumberOfExecution", this.getMaxNumberOfExec());
		result.put("numberOfExecutionLeft", this.getNumberOfExecLeft());
		result.put("numberOfExecutionOnFailureLeft", this.getNumberOfExecOnFailureLeft());
		result.put("maxNumberOfExecutionOnFailure", this.getMaxNumberOfExecOnFailure());
		
		JSONObject parallel = new JSONObject();
		parallel.put("nodesNumber", this.getNodeCount());
		result.put("parallelEnvironment", parallel);
		
		
		return result;
	}
}
