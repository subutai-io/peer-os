package org.safehaus.subutai.core.env.rest;


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
    public Response createEnvironment( @QueryParam( "topology" ) String topologyJsonString );

    @GET
    @Path( "container/environmentId" )
    @Produces( { MediaType.TEXT_PLAIN } )
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
    @Path( "grow" )
    public Response growEnvironment( @QueryParam( "environmentId" ) String environmentId,
                                     @QueryParam( "topology" ) String topologyJsonString );

    @GET
    @Path( "container/state" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getContainerState( @QueryParam( "containerId" ) String containerId );
}