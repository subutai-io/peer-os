package org.safehaus.subutai.core.environment.rest;


import javax.jws.WebService;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;


@WebService(serviceName = "peerServices")
public interface RestService
{
    @POST
    @Path("/environment")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.TEXT_PLAIN)
    public String buildNodeGroup( String peer );
}