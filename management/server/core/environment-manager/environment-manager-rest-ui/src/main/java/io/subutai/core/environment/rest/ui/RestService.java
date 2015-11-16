package io.subutai.core.environment.rest.ui;


import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.UUID;


public interface RestService
{
    /** Templates *****************************************************/
    @GET
    @Path( "templates" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response listTemplates();


    /** Blueprints ****************************************************/
    @GET
    @Path( "blueprints" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response getBlueprints();

    @GET
    @Path( "blueprints/{blueprintId}" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response getBlueprint( @PathParam( "blueprintId" ) UUID blueprintId );

    @POST
    @Path( "blueprints" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response saveBlueprint( @FormParam( "blueprint_json" ) String content);

    @DELETE
    @Path( "blueprints/{blueprintId}" )
    Response deleteBlueprint( @PathParam( "blueprintId" ) UUID blueprintId );


    /** Domain *****************************************************/
    @GET
    @Path( "domains" )
    @Produces( { MediaType.TEXT_PLAIN } )
    Response getDefaultDomainName();


    /** Environments *****************************************************/
    @GET
    @Produces( { MediaType.APPLICATION_JSON } )
    Response listEnvironments();


    @POST
    Response createEnvironment( @FormParam( "blueprint_json" ) String blueprintJson );

    @POST
    @Path( "grow" )
    Response growEnvironment( @FormParam( "blueprint_json" ) String blueprintJson );

    @DELETE
    @Path( "{environmentId}" )
    Response destroyEnvironment( @PathParam( "environmentId" ) String environmentId );


    /** Environments SSH keys *****************************************************/
    @POST
    @Path( "keys" )
    Response setSshKey( @FormParam( "environmentId" ) String environmentId, @FormParam( "key" ) String key );


    @DELETE
    @Path( "{environmentId}/keys" )
    Response removeSshKey( @PathParam( "environmentId" ) String environmentId );


    /** Containers keys *****************************************************/
    @GET
    @Path( "containers/{containerId}" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response getContainerEnvironmentId( @PathParam( "containerId" ) String containerId );

    @DELETE
    @Path( "containers/{containerId}" )
    Response destroyContainer( @PathParam( "containerId" ) String containerId );

    @GET
    @Path( "containers/{containerId}/state" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response getContainerState( @PathParam( "containerId" ) String containerId );

    @POST
    @Path( "containers/{containerId}/start" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response startContainer( @PathParam( "containerId" ) String containerId );

    @POST
    @Path( "containers/{containerId}/stop" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response stopContainer( @PathParam( "containerId" ) String containerId );


    /** Container types *****************************************************/
    @GET
    @Path( "containers/types" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response listContainerTypes();


    /** Container quota *****************************************************/
    @GET
    @Path( "containers/{containerId}/quota" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response getContainerQuota( @PathParam( "containerId" ) String containerId );

    @POST
    @Path( "containers/{containerId}/quota" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response setContainerQuota( @PathParam( "containerId" ) String containerId,
                                @FormParam( "cpu" ) int cpu,
                                @FormParam( "ram" ) int ram,
                                @FormParam( "disk_home" ) Double diskHome,
                                @FormParam( "disk_var" ) Double diskVar,
                                @FormParam( "disk_root" ) Double diskRoot,
                                @FormParam( "disk_opt" ) Double diskOpt);

    @GET
    @Path( "containers/{containerId}/quota/ram" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response getRamQuota( @PathParam( "containerId" ) String containerId );

    @POST
    @Path( "containers/{containerId}/quota/ram" )
    Response setRamQuota( @PathParam( "containerId" ) String containerId, @FormParam( "ram" ) int ram );

    @GET
    @Path( "containers/{containerId}/quota/cpu" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response getCpuQuota( @PathParam( "containerId" ) String containerId );

    @POST
    @Path( "containers/{containerId}/quota/cpu" )
    Response setCpuQuota( @PathParam( "containerId" ) String containerId, @FormParam( "cpu" ) int cpu );

    @GET
    @Path( "containers/{containerId}/quota/disk/{diskPartition}" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response getDiskQuota( @PathParam( "containerId" ) String containerId,
                           @PathParam( "diskPartition" ) String diskPartition );

    @POST
    @Path( "containers/{containerId}/quota/disk" )
    Response setDiskQuota( @PathParam( "containerId" ) String containerId, @FormParam( "diskQuota" ) String diskQuota );

    /** Peers strategy *****************************************************/

    @GET
    @Path( "strategies" )
    Response listPlacementStrategies();

    /** Peers *****************************************************/
    @GET
    @Path( "peers" )
    Response getPeers();

}