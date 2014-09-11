package org.safehaus.subutai.core.dispatcher.rest;


import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


public interface RestService {

    @POST
    @Path("responses")
    @Produces({ MediaType.TEXT_PLAIN })
    public Response processResponses( @FormParam("responses") String responses );

    @POST
    @Path("requests")
    @Produces({ MediaType.TEXT_PLAIN })
    public Response executeRequests( @FormParam("requests") String requests );
}
