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
    public Response buildLocalEnvironment( @QueryParam( "blueprint" ) String blueprint );

    @GET
    @Path( "container/environmentId" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getContainerEnvironmentId( @QueryParam( "containerId" ) String containerId );

    @GET
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getEnvironment( @QueryParam( "environmentId" ) String environmentId );

    @DELETE
    public Response destroyEnvironment( @QueryParam( "environmentId" ) String environmentId );

    @DELETE
    @Path( "container" )
    public Response destroyContainer( @QueryParam( "containerId" ) String containerId );

    @POST
    @Path( "nodeGroup" )
    public Response addNodeGroup( @QueryParam( "environmentId" ) String environmentId,
                                  @QueryParam( "nodeGroup" ) String nodeGroup );

    @GET
    @Path( "container/state" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getContainerState( @QueryParam( "containerId" ) String containerId );
}