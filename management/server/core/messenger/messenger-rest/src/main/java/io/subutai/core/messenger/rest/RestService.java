package io.subutai.core.messenger.rest;


import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;


/**
 * Messenger REST API
 */
public interface RestService
{


    @POST
    @Path("message")
    public Response processMessage( @FormParam("envelope") String envelope );
}
