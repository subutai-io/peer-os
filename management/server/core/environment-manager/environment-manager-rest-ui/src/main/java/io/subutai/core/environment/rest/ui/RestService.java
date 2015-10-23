package io.subutai.core.environment.rest.ui;


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

    @POST
    @Path( "blueprint" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response saveBlueprint( @FormParam( "blueprint_json" ) String content);

    @GET
    @Path( "blueprint" )
    @Produces( { MediaType.TEXT_PLAIN } )
    public Response getBlueprints();

    @GET
    @Path( "container/{containerId}" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getContainerEnvironmentId( @PathParam( "containerId" ) String containerId );


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
    @Path( "{environmentId}/keys" )
    public Response removeSshKey( @PathParam( "environmentId" ) String environmentId );

    @DELETE
    @Path( "{environmentId}" )
    public Response destroyEnvironment( @PathParam( "environmentId" ) String environmentId );

    @DELETE
    @Path( "container/{containerId}" )
    public Response destroyContainer( @PathParam( "containerId" ) String containerId );

    @GET
    @Path( "container/{containerId}/state" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getContainerState( @PathParam( "containerId" ) String containerId );

    @POST
    @Path( "container/{containerId}/start" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response startContainer( @PathParam( "containerId" ) String containerId );

    @POST
    @Path( "container/{containerId}/stop" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response stopContainer( @PathParam( "containerId" ) String containerId );
}