package org.safehaus.subutai.core.metric.rest;


import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


/**
 * Rest for Monitor
 */
public interface RestService
{


    @GET
    @Path( "metrics/resource-hosts" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getResourceHostMetrics();

    @GET
    @Path( "metrics/containers-hosts/{environmentId}" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getContainerHostMetrics( @PathParam( "environmentId" ) String uuid );

    @POST
    @Path( "alert" )
    public Response alertThresholdExcess( @FormParam( "metric" ) String alertMetric );
}
