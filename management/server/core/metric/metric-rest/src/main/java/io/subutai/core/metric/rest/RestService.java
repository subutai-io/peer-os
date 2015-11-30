package io.subutai.core.metric.rest;


import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.subutai.common.metric.ResourceAlert;


/**
 * Rest for Monitor
 */
public interface RestService
{


    @GET
    @Path( "metrics/resource-hosts" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getResourceHostsMetrics();

    @GET
    @Path( "metrics/containers-hosts/{environmentId}" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getContainerHostsMetrics( @PathParam( "environmentId" ) String id );

    @POST
    @Path( "alert" )
    @Consumes( MediaType.APPLICATION_JSON )
    public Response alert( ResourceAlert alert );
}
