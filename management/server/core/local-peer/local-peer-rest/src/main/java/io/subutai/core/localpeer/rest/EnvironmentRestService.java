package io.subutai.core.localpeer.rest;


import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.subutai.common.host.ContainerHostState;
import io.subutai.common.metric.ProcessResourceUsage;
import io.subutai.common.peer.ContainerId;
import io.subutai.common.resource.ResourceType;
import io.subutai.common.resource.ResourceValue;


public interface EnvironmentRestService
{
    @GET
    @Path( "{environmentId}/container/{id}/start" )
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    void startContainer( @PathParam( "id" ) ContainerId containerId );

    @POST
    @Path( "{environmentId}/container/stop" )
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( { MediaType.APPLICATION_JSON } )
    void stopContainer( ContainerId containerId );

    @POST
    @Path( "{environmentId}/container/destroy" )
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( { MediaType.APPLICATION_JSON } )
    void destroyContainer( ContainerId containerId );


    @GET
    @Path( "{environmentId}/container/{containerId}/state" )
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( { MediaType.APPLICATION_JSON } )
    ContainerHostState getContainerState( @PathParam( "containerId" ) ContainerId containerId );

    @GET
    @Path( "{environmentId}/container/{containerId}/usage/{pid}" )
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( { MediaType.APPLICATION_JSON } )
    ProcessResourceUsage getProcessResourceUsage( @PathParam( "containerId" ) ContainerId containerId,
                                                  @PathParam( "pid" ) int pid );

    @GET
    @Path( "{environmentId}/container/{containerId}/quota/cpuset" )
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    Response getCpuSet( @PathParam( "containerId" ) ContainerId containerId );

    @POST
    @Path( "{environmentId}/container/{containerId}/quota/cpuset" )
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    Response setCpuSet( @PathParam( "containerId" ) ContainerId containerId, Set<Integer> cpuSet );

    @GET
    @Path( "{environmentId}/container/{containerId}/quota/{resourceType}" )
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    Response getQuota( @PathParam( "containerId" ) ContainerId containerId,
                       @PathParam( "resourceType" ) ResourceType resourceType );

    @POST
    @Path( "{environmentId}/container/{containerId}/quota/{resourceType}" )
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    Response setQuota( @PathParam( "containerId" ) ContainerId containerId,
                       @PathParam( "resourceType" ) ResourceType resourceType, ResourceValue resourceValue );

    @GET
    @Path( "{environmentId}/container/{containerId}/quota/{resourceType}/available" )
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    Response getAvailableQuota( @PathParam( "containerId" ) ContainerId containerId,
                                @PathParam( "resourceType" ) ResourceType resourceType );
}