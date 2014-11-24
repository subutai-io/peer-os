package org.safehaus.subutai.core.hostregistry.rest;


import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


public interface RestService
{

    @GET
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getHosts();
}
