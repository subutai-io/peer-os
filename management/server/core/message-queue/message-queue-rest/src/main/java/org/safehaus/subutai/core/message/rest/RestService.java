package org.safehaus.subutai.core.message.rest;


import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;


/**
 * Rest for Queue
 */
public interface RestService
{


    @POST
    @Path("message")
    public Response processMessage( @FormParam( "envelope" ) String envelope );
}
