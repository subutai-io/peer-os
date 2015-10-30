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
import java.util.UUID;


public interface RestService
{

    @GET
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response listEnvironments();

    @GET
    @Path( "domain" )
    @Produces( { MediaType.TEXT_PLAIN } )
    public Response getDefaultDomainName();

    // blueprint

    @GET
    @Path( "blueprint" )
    @Produces( { MediaType.TEXT_PLAIN } )
    public Response getBlueprints();

    @POST
    @Path( "blueprint" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response saveBlueprint( @FormParam( "blueprint_json" ) String content);

    @DELETE
    @Path( "blueprint/{blueprintId}" )
    public Response deleteBlueprint( @PathParam( "blueprintId" ) UUID blueprintId );

    // container

    @GET
    @Path( "container/{containerId}" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getContainerEnvironmentId( @PathParam( "containerId" ) String containerId );

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

    // environment

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


    // Peers
    @GET
    @Path( "peers" )
    public Response getPeers();


    //Quota
    @GET
    @Path( "container/{containerId}/quota" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response getContainerQuota( @PathParam( "containerId" ) String containerId );

    @POST
    @Path( "container/{containerId}/quota" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response setContainerQuota( @PathParam( "containerId" ) String containerId,
                                @FormParam( "cpu" ) int cpu,
                                @FormParam( "ram" ) int ram,
                                @FormParam( "disk_home" ) String diskHome,
                                @FormParam( "disk_var" ) String diskVar,
                                @FormParam( "disk_root" ) String diskRoot,
                                @FormParam( "disk_opt" ) String diskOpt);

    @GET
    @Path( "container/{containerId}/quota/ram" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response getRamQuota( @PathParam( "containerId" ) String containerId );

    @POST
    @Path( "container/{containerId}/quota/ram" )
    Response setRamQuota( @PathParam( "containerId" ) String containerId, @FormParam( "ram" ) int ram );

    @GET
    @Path( "container/{containerId}/quota/cpu" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response getCpuQuota( @PathParam( "containerId" ) String containerId );

    @POST
    @Path( "container/{containerId}/quota/cpu" )
    Response setCpuQuota( @PathParam( "containerId" ) String containerId, @FormParam( "cpu" ) int cpu );

    @GET
    @Path( "container/{containerId}/quota/disk/{diskPartition}" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response getDiskQuota( @PathParam( "containerId" ) String containerId,
                           @PathParam( "diskPartition" ) String diskPartition );

    @POST
    @Path( "container/{containerId}/quota/disk" )
    Response setDiskQuota( @PathParam( "containerId" ) String containerId, @FormParam( "diskQuota" ) String diskQuota );
}