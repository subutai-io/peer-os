package org.safehaus.subutai.core.environment.rest;


import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


public interface RestService
{

    @POST
    @Path( "build" )
    public Response buildLocalEnvironment( @QueryParam( "blueprint" ) String blueprint );

    @GET
    @Path( "getEnvironmentId" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getContainerEnvironmentId( @QueryParam( "containerId" ) String containerId );

    @GET
    @Path( "getEnvironment" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getEnvironment( @QueryParam( "environmentId" ) String environmentId );

    @DELETE
    @Path( "destroy" )
    public Response destroyEnvironment( @QueryParam( "environmentId" ) String environmentId );

    @DELETE
    @Path( "container/destroy" )
    public Response destroyContainer( @QueryParam( "containerId" ) String containerId );

    @POST
    @Path( "add" )
    public Response addNodeGroup( @QueryParam( "environmentId" ) String environmentId,
                                  @QueryParam( "nodeGroup" ) String nodeGroup );

    @GET
    @Path( "getContainerState" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getContainerState( @QueryParam( "containerId" ) String containerId );
}