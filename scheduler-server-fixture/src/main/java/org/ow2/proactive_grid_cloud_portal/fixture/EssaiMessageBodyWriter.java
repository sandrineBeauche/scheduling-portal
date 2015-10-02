package org.ow2.proactive_grid_cloud_portal.fixture;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;


@Provider
@Produces("application/json")
public class EssaiMessageBodyWriter implements MessageBodyWriter<DtoResponse> {

	@Override
	public boolean isWriteable(Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType) {
		return type == DtoResponse.class;
	}
	
	
	@Override
	public void writeTo(DtoResponse t, Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, Object> httpHeaders,
			OutputStream entityStream) throws IOException,
			WebApplicationException {
		
		JSONObject json = new JSONObject();
		try {
			json.put("tell", t.getMessage());
			DataOutputStream dos = new DataOutputStream(entityStream);
			dos.writeUTF(json.toString());
		} catch (JSONException e) {
			throw new IOException(e);
		}
		
	}
	
	@Override
	public long getSize(DtoResponse t, Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType) {
		return 0;
	}
}
