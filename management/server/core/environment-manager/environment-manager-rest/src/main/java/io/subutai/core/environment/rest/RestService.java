package io.subutai.core.environment.rest;


import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
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

    @GET
    @Path( "container/environmentId" )
    @Produces( { MediaType.TEXT_PLAIN } )
    public Response getContainerEnvironmentId( @QueryParam( "containerId" ) String containerId );


    @GET
    @Path( "{environmentId}" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response viewEnvironment( @PathParam( "environmentId" ) String environmentId );

    @POST
    public Response createEnvironment( @FormParam( "name" ) String environmentName,
                                       @FormParam( "topology" ) String topologyJsonString,
                                       @FormParam( "subnet" ) String subnetCidr, @FormParam( "key" ) String sshKey );

    @POST
    @Path( "grow" )
    public Response growEnvironment( @FormParam( "environmentId" ) String environmentId,
                                     @FormParam( "topology" ) String topologyJsonString );

    @POST
    @Path( "key" )
    public Response setSshKey( @FormParam( "environmentId" ) String environmentId, @FormParam( "key" ) String key );


    @DELETE
    @Path( "key" )
    public Response removeSshKey( @QueryParam( "environmentId" ) String environmentId );

    @DELETE
    public Response destroyEnvironment( @QueryParam( "environmentId" ) String environmentId );

    @DELETE
    @Path( "container" )
    public Response destroyContainer( @QueryParam( "containerId" ) String containerId );

    @GET
    @Path( "container/state" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getContainerState( @QueryParam( "containerId" ) String containerId );

    @POST
    @Path( "container/start" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response startContainer( @QueryParam( "containerId" ) String containerId );

    @POST
    @Path( "container/stop" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response stopContainer( @QueryParam( "containerId" ) String containerId );
}