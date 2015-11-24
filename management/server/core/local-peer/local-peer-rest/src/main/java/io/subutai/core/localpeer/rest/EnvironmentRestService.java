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
import io.subutai.common.quota.CpuQuota;
import io.subutai.common.quota.DiskPartition;
import io.subutai.common.quota.DiskQuota;
import io.subutai.common.quota.RamQuota;


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


    @POST
    @Path( "{environmentId}/container/state" )
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( { MediaType.APPLICATION_JSON } )
    ContainerHostState getContainerState( ContainerId containerId );

    @GET
    @Path( "{environmentId}/container/{containerId}/usage/{pid}" )
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( { MediaType.APPLICATION_JSON } )
    ProcessResourceUsage getProcessResourceUsage( @PathParam( "containerId" ) ContainerId containerId,
                                                  @PathParam( "pid" ) int pid );

    @GET
    @Path( "{environmentId}/container/{containerId}/quota/ram/available" )
    @Produces( MediaType.APPLICATION_JSON )
    Response getAvailableRamQuota( @PathParam( "containerId" ) ContainerId containerId );

    @GET
    @Path( "{environmentId}/container/{containerId}/quota/cpu/available" )
    @Produces( MediaType.APPLICATION_JSON )
    Response getAvailableCpuQuota( @PathParam( "containerId" ) ContainerId containerId );

    @GET
    @Path( "{environmentId}/container/{containerId}/quota/disk/{partition}/available" )
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    Response getAvailableDiskQuota( @PathParam( "containerId" ) ContainerId containerId,
                                    @PathParam( "partition" ) DiskPartition diskPartition );

    @GET
    @Path( "{environmentId}/container/{containerId}/quota/ram" )
    @Produces( MediaType.APPLICATION_JSON )
    Response getRamQuota( @PathParam( "containerId" ) ContainerId containerId );

    @POST
    @Path( "{environmentId}/container/{containerId}/quota/ram" )
    Response setRamQuota( @PathParam( "containerId" ) ContainerId containerId, RamQuota ramQuota );


    @GET
    @Path( "{environmentId}/container/{containerId}/quota/cpu" )
    @Produces( MediaType.APPLICATION_JSON )
    Response getCpuQuota( @PathParam( "containerId" ) ContainerId containerId );

    @POST
    @Path( "{environmentId}/container/{containerId}/quota/cpu" )
    Response setCpuQuota( @PathParam( "containerId" ) ContainerId containerId, CpuQuota cpuQuota );

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
    @Path( "{environmentId}/container/{containerId}/quota/disk/{partition}" )
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    Response getDiskQuota( @PathParam( "containerId" ) ContainerId containerId,
                           @PathParam( "partition" ) DiskPartition diskPartition );

    @POST
    @Path( "{environmentId}/container/{containerId}/quota/disk" )
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    Response setDiskQuota( @PathParam( "containerId" ) ContainerId containerId, DiskQuota diskQuota );
}