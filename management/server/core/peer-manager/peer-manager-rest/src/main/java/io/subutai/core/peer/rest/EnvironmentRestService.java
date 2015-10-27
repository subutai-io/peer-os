package io.subutai.core.peer.rest;


import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.subutai.common.host.ContainerHostState;
import io.subutai.common.peer.ContainerId;


public interface EnvironmentRestService
{
    @POST
    @Path( "{environmentId}/container/start" )
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    void startContainer( ContainerId containerId );

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
}