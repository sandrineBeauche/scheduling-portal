package org.ow2.proactive_grid_cloud_portal.fixture;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/sample")
public class SimpleService {
    private final static Logger log = LoggerFactory.getLogger(SimpleService.class);

    @GET
    @Path("/say/{msg}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPortDataSet(@PathParam("msg") String message) {
        List<DtoResponse> response = new ArrayList<DtoResponse>();
        DtoResponse response1 = new DtoResponse();
        try {
            
            response1.setMessage(message + "1");
            response1.setTime(DateTime.now());
            
            DtoResponse response2 = new DtoResponse();
            response2.setMessage(message + "2");
            response2.setTime(DateTime.now());
            
            response.add(response1);
            response.add(response2);
        } catch (Exception e) {
            log.error("internal error: {}", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        return Response.ok(response).build();
    }
}