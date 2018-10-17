package io.subutai.core.localpeer.rest;


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
import io.subutai.common.host.ContainerHostState;
import io.subutai.common.host.HostId;
import io.subutai.common.host.Quota;
import io.subutai.common.peer.ContainerId;
import io.subutai.common.peer.EnvironmentId;
import io.subutai.common.protocol.CustomProxyConfig;
import io.subutai.common.security.SshEncryptionType;
import io.subutai.common.security.SshKeys;
import io.subutai.bazaar.share.quota.ContainerQuota;


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

    @POST
    @Path( "{environmentId}/container/{id}/hostname" )
    void setContainerHostname( @PathParam( "id" ) ContainerId containerId, String hostname );


    @GET
    @Path( "{environmentId}/container/{containerId}/state" )
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    ContainerHostState getContainerState( @PathParam( "containerId" ) ContainerId containerId );

    @GET
    @Path( "{environmentId}/container/{containerId}/quota/raw" )
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    Quota getRawQuota( @PathParam( "containerId" ) ContainerId containerId );


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

/*
    @POST
    @Path( "{environmentId}/container/{containerId}/size" )
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    Response setContainerSize( @PathParam( "containerId" ) ContainerId containerId, ContainerSize containerSize );
*/

    @GET
    @Path( "{environmentId}/container/{containerId}/rhId" )
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    HostId getResourceHostIdByContainerId( @PathParam( "containerId" ) ContainerId containerId );

    @POST
    @Path( "{environmentId}/containers/sshkeys" )
    @Consumes( MediaType.APPLICATION_JSON )
    Response configureSshInEnvironment( @PathParam( "environmentId" ) EnvironmentId environmentId, SshKeys sshKeys );

    @GET
    @Path( "{environmentId}/templatesprogress" )
    @Produces( MediaType.APPLICATION_JSON )
    Response getTemplateDownloadProgress( @PathParam( "environmentId" ) EnvironmentId environmentId );

    @PUT
    @Path( "{environmentId}/containers/sshkeys/{encType}" )
    @Produces( MediaType.APPLICATION_JSON )
    SshKeys generateSshKeysForEnvironment( @PathParam( "environmentId" ) EnvironmentId environmentId,
                                           @PathParam( "encType" ) SshEncryptionType sshKeyType );


    @GET
    @Path( "{environmentId}/container/{containerId}/sshkeys" )
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    SshKeys getContainerAuthorizedKeys( @PathParam( "containerId" ) ContainerId containerId );

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

    @GET
    @Path( "{environmentId}/sshkeys/{encType}" )
    @Consumes( MediaType.APPLICATION_JSON )
    Response getSshKeys( @PathParam( "environmentId" ) EnvironmentId environmentId,
                         @PathParam( "encType" ) SshEncryptionType encryptionType );

    @POST
    @Path( "{environmentId}/sshkeys/{encType}" )
    @Consumes( MediaType.APPLICATION_JSON )
    Response createSshKey( @PathParam( "environmentId" ) EnvironmentId environmentId,
                           @PathParam( "encType" ) SshEncryptionType encryptionType, String containerId );


    @POST
    @Path( "{environmentId}/containers/etchosts/{oldHostname}/{newHostname}" )
    void updateEtcHostsWithNewContainerHostname( @PathParam( "environmentId" ) EnvironmentId environmentId,
                                                 @PathParam( "oldHostname" ) String oldHostname,
                                                 @PathParam( "newHostname" ) String newHostname );

    @POST
    @Path( "{environmentId}/containers/authorizedkeys/{encType}/{oldHostname}/{newHostname}" )
    void updateAuthorizedKeysWithNewContainerHostname( @PathParam( "environmentId" ) EnvironmentId environmentId,
                                                       @PathParam( "encType" ) SshEncryptionType sshEncryptionType,
                                                       @PathParam( "oldHostname" ) String oldHostname,
                                                       @PathParam( "newHostname" ) String newHostname );


    @POST
    @Path( "{environmentId}/container/{containerId}/customProxy/add" )
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    Response addCustomProxy( CustomProxyConfig proxyConfig );

    @POST
    @Path( "{environmentId}/container/{containerId}/customProxy/remove" )
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    Response removeCustomProxy( CustomProxyConfig proxyConfig );

    @POST
    @Path( "{environmentId}/peers/{peerId}/exclude" )
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    Response excludePeerFromEnvironment( @PathParam( "environmentId" ) String environmentId,
                                         @PathParam( "peerId" ) String peerId );

    @POST
    @Path( "{environmentId}/containers/{containerId}/exclude" )
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    Response excludeContainerFromEnvironment( @PathParam( "environmentId" ) String environmentId,
                                              @PathParam( "containerId" ) String containerId );

    @POST
    @Path( "{environmentId}/containers/{containerId}/hostname/{hostname}" )
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    Response updateContainerHostname( @PathParam( "environmentId" ) String environmentId,
                                      @PathParam( "containerId" ) String containerId,
                                      @PathParam( "hostname" ) String hostname );

    @POST
    @Path( "{environmentId}/info/{containerId}" )
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    Response placeEnvironmentInfoByContainerId( @PathParam( "environmentId" ) String environmentId,
                                                @PathParam( "containerId" ) String containerId );
}