package io.subutai.health;


import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;


public interface HealthRestService
{
    @GET
    @Path( "/ready" )
    Response isReady();
}
