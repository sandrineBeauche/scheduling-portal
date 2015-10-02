package org.ow2.proactive_grid_cloud_portal.fixture;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.ow2.proactive_grid_cloud_portal.scheduler.client.Task;

public class TaskList extends ArrayList<Task> implements StreamingOutput {

	@Override
	public void write(OutputStream output) throws IOException,
			WebApplicationException {
		try {
			JSONArray json = JSONFactory.toJSON(this);
			DataOutputStream dos = new DataOutputStream(output);
			dos.writeUTF(json.toString());
		} catch (JSONException e) {
			throw new IOException(e);
		}
    	

	}

}
