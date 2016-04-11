package io.subutai.core.localpeer.rest;


import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.subutai.common.environment.HostAddresses;
import io.subutai.common.environment.SshPublicKeys;
import io.subutai.common.host.ContainerHostState;
import io.subutai.common.host.HostId;
import io.subutai.common.metric.ProcessResourceUsage;
import io.subutai.common.peer.ContainerId;
import io.subutai.common.peer.EnvironmentId;
import io.subutai.common.protocol.ReverseProxyConfig;
import io.subutai.common.quota.ContainerQuota;


public interface EnvironmentRestService
{
    @POST
    @Path( "{environmentId}/container/{id}/start" )
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    void startContainer( @PathParam( "id" ) ContainerId containerId );

    @POST
    @Path( "{environmentId}/container/{id}/stop" )
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( { MediaType.APPLICATION_JSON } )
    void stopContainer( @PathParam( "id" ) ContainerId containerId );

    @POST
    @Path( "{environmentId}/container/{id}/destroy" )
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( { MediaType.APPLICATION_JSON } )
    void destroyContainer( @PathParam( "id" ) ContainerId containerId );


    @GET
    @Path( "{environmentId}/container/{containerId}/state" )
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
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
    @Path( "{environmentId}/container/{containerId}/quota" )
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    Response getQuota( @PathParam( "containerId" ) ContainerId containerId );

    @POST
    @Path( "{environmentId}/container/{containerId}/quota" )
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    Response setQuota( @PathParam( "containerId" ) ContainerId containerId, ContainerQuota containerQuota );

    @GET
    @Path( "{environmentId}/container/{containerId}/rhId" )
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    HostId getResourceHostIdByContainerId( @PathParam( "containerId" ) ContainerId containerId );

    @POST
    @Path( "{environmentId}/containers/sshkeys" )
    @Consumes( MediaType.APPLICATION_JSON )
    Response configureSshInEnvironment( @PathParam( "environmentId" ) EnvironmentId environmentId,
                                        SshPublicKeys sshPublicKeys );

    @PUT
    @Path( "{environmentId}/containers/sshkeys" )
    @Produces( MediaType.APPLICATION_JSON )
    SshPublicKeys generateSshKeysForEnvironment( @PathParam( "environmentId" ) EnvironmentId environmentId );

    @POST
    @Path( "{environmentId}/containers/sshkey/add" )
    void addSshKey( @PathParam( "environmentId" ) EnvironmentId environmentId, String sshPublicKey );

    @POST
    @Path( "{environmentId}/containers/sshkey/remove" )
    void removeSshKey( @PathParam( "environmentId" ) EnvironmentId environmentId, String sshPublicKey );

    @POST
    @Path( "{environmentId}/containers/hosts" )
    @Consumes( MediaType.APPLICATION_JSON )
    Response configureHostsInEnvironment( @PathParam( "environmentId" ) EnvironmentId environmentId,
                                          HostAddresses hostAddresses );

    @POST
    @Path( "{environmentId}/container/{containerId}/reverseProxy" )
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    Response addReverseProxy( ReverseProxyConfig reverseProxyConfig );
}