package org.ow2.proactive_grid_cloud_portal.fixture;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.ow2.proactive_grid_cloud_portal.scheduler.client.Task;

public class JSONFactory {

	public static JSONObject toJSON(Task task) throws JSONException{
		JSONObject result = new JSONObject();
		result.put("name", task.getName());
		JSONObject taskInfoResult = new JSONObject();
		taskInfoResult.put("executionHostName", task.getHostName());
		
		JSONObject taskIdResult = new JSONObject();
		taskIdResult.put("id", task.getId());
		taskInfoResult.put("taskId", taskIdResult);
		
		taskInfoResult.put("taskStatus", task.getStatus().toString());
		taskInfoResult.put("startTime", task.getStartTime());
		taskInfoResult.put("finishedTime", task.getFinishTime());
		taskInfoResult.put("executionDuration", task.getExecutionTime());
		
		result.put("taskInfo", taskIdResult);
		
		result.put("description", task.getDescription());
		result.put("tag", task.getTag());
		result.put("maxNumberOfExecution", task.getMaxNumberOfExec());
		result.put("numberOfExecutionLeft", task.getNumberOfExecLeft());
		result.put("numberOfExecutionOnFailureLeft", task.getNumberOfExecOnFailureLeft());
		result.put("maxNumberOfExecutionOnFailure", task.getMaxNumberOfExecOnFailure());
		
		JSONObject parallel = new JSONObject();
		parallel.put("nodesNumber", task.getNodeCount());
		result.put("parallelEnvironment", parallel);
		
		
		return result;
	}
	
	
	public static JSONArray toJSON(TaskList tasks) throws JSONException {
		JSONArray result = new JSONArray();
		for(Task task: tasks){
			result.put(toJSON(task));
		}
		return result;
	}
	
	
}
