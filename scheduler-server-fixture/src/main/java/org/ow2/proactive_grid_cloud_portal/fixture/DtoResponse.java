package org.ow2.proactive_grid_cloud_portal.fixture;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;

@XmlRootElement
public class DtoResponse implements StreamingOutput{

    private String message;
    private DateTime time;

    public DtoResponse() {}

    public String getMessage() { return message;}

    public void setMessage(String message) { this.message = message; }

    public DateTime getTime() { return time; }

    public void setTime(DateTime time) { this.time = time; }
    
    @Override
    public void write(OutputStream output) throws IOException,
    		WebApplicationException {
    	JSONObject json = new JSONObject();
		try {
			json.put("tell", this.getMessage());
			DataOutputStream dos = new DataOutputStream(output);
			dos.writeUTF(json.toString());
		} catch (JSONException e) {
			throw new IOException(e);
		}
    	
    }
}