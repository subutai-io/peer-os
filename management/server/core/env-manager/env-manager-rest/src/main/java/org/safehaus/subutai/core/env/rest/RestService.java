package org.safehaus.subutai.core.env.rest;


import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


public interface RestService
{

    @GET
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response listEnvironments();

    @GET
    @Path( "domain" )
    @Produces( { MediaType.TEXT_PLAIN } )
    public Response getDefaultDomainName();

    @POST
    public Response createEnvironment( @QueryParam( "topology" ) String topologyJsonString );


    @GET
    @Path( "{environmentId}" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response viewEnvironment( @PathParam( "environmentId" ) String environmentId );

    @DELETE
    public Response destroyEnvironment( @QueryParam( "environmentId" ) String environmentId );

    @DELETE
    @Path( "container" )
    public Response destroyContainer( @QueryParam( "containerId" ) String containerId );

    @POST
    @Path( "grow" )
    public Response growEnvironment( @QueryParam( "environmentId" ) String environmentId,
                                     @QueryParam( "topology" ) String topologyJsonString );

    @PUT
    @Path( "key" )
    public Response setSshKey( @QueryParam( "environmentId" ) String environmentId, @QueryParam( "key" ) String key );

    @DELETE
    @Path( "key" )
    public Response removeSshKey( @QueryParam( "environmentId" ) String environmentId );


    @GET
    @Path( "container/environmentId" )
    @Produces( { MediaType.TEXT_PLAIN } )
    public Response getContainerEnvironmentId( @QueryParam( "containerId" ) String containerId );

    @GET
    @Path( "container/state" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getContainerState( @QueryParam( "containerId" ) String containerId );
}