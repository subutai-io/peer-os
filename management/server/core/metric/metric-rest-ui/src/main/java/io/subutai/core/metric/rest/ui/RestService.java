package io.subutai.core.metric.rest.ui;


import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


public interface RestService
{
    @GET
    @Path( "{environment_id}/{host_id}/{interval}" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response getMetrics( @PathParam( "environment_id" ) String environmentId, @PathParam( "host_id" ) String hostId, @PathParam( "interval" ) int interval );
}
