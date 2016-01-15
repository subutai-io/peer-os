package io.subutai.core.environment.rest.ui;


import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;


public interface RestService
{
    /** Templates **************************************************** */
    @GET
    @Path( "templates" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response listTemplates();


    /** Blueprints *************************************************** */

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
    Response saveBlueprint( @FormParam( "blueprint_json" ) String content );

    @DELETE
    @Path( "blueprints/{blueprintId}" )
    Response deleteBlueprint( @PathParam( "blueprintId" ) UUID blueprintId );


    /** Domain **************************************************** */

    @GET
    @Path( "domains" )
    @Produces( { MediaType.TEXT_PLAIN } )
    Response getDefaultDomainName();


    /** Environments **************************************************** */

    @GET
    @Produces( { MediaType.APPLICATION_JSON } )
    Response listEnvironments();

    @PUT
    @Path( "{environmentId}/revoke" )
    Response accessStatus( @PathParam( "environmentId" ) String environmentId);

    @POST
    @Path( "requisites" )
    Response setupRequisites( @FormParam( "blueprint_json" ) String blueprintJson );

    @POST
    @Path( "build" )
    Response startEnvironmentBuild( @FormParam( "environmentId" ) String environmentId,
                                    @FormParam( "signedMessage" ) String signedMessage );

    //    @POST
    //    Response createEnvironment( @FormParam( "blueprint_json" ) String blueprintJson );

    @POST
    @Path( "grow" )
    Response growEnvironment( @FormParam( "environmentId" ) String environmentId,
                              @FormParam( "blueprint_json" ) String blueprintJson );

    @DELETE
    @Path( "{environmentId}" )
    Response destroyEnvironment( @PathParam( "environmentId" ) String environmentId );


    /** Environments SSH keys **************************************************** */

    @POST
    @Path( "keys" )
    Response setSshKey( @FormParam( "environmentId" ) String environmentId, @FormParam( "key" ) String key );


    @DELETE
    @Path( "{environmentId}/keys" )
    Response removeSshKey( @PathParam( "environmentId" ) String environmentId );


    /** Environment domains **************************************************** */

    @GET
    @Path( "{environmentId}/domain" )
    public Response getEnvironmentDomain( @PathParam( "environmentId" ) String environmentId );


    @GET
    @Path( "/domains/strategies" )
    Response listDomainLoadBalanceStrategies();


    @POST
    @Consumes( MediaType.MULTIPART_FORM_DATA )
    @Path( "/domains" )
    Response addEnvironmentDomain( @Multipart( "environmentId" ) String environmentId,
                                   @Multipart( "hostName" ) String hostName,
                                   @Multipart( "strategy" ) String strategyJson,
                                   @Multipart( value = "file" ) Attachment attr );


    @DELETE
    @Path( "{environmentId}/domains" )
    Response removeEnvironmentDomain( @PathParam( "environmentId" ) String environmentId );


    @GET
    @Path( "{environmentId}/containers/{containerId}/domain" )
    Response isContainerDomain( @PathParam( "environmentId" ) String environmentId,
                                @PathParam( "containerId" ) String containerId );


    @PUT
    @Path( "{environmentId}/containers/{containerId}/domain" )
    Response setContainerDomain( @PathParam( "environmentId" ) String environmentId,
                                 @PathParam( "containerId" ) String containerId );


    /** Containers **************************************************** */

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


    /** Container types **************************************************** */

    @GET
    @Path( "containers/types" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response listContainerTypes();


    /** Container quota **************************************************** */

    @GET
    @Path( "containers/{containerId}/quota" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response getContainerQuota( @PathParam( "containerId" ) String containerId );

    @POST
    @Path( "containers/{containerId}/quota" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response setContainerQuota( @PathParam( "containerId" ) String containerId, @FormParam( "cpu" ) int cpu,
                                @FormParam( "ram" ) int ram, @FormParam( "disk_home" ) Double diskHome,
                                @FormParam( "disk_var" ) Double diskVar, @FormParam( "disk_root" ) Double diskRoot,
                                @FormParam( "disk_opt" ) Double diskOpt );

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


    /** Peers strategy **************************************************** */

    @GET
    @Path( "strategies" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response listPlacementStrategies();


    /** Peers **************************************************** */

    @GET
    @Path( "peers" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response getPeers();


    /** Tags **************************************************** */

    @POST
    @Path( "{environmentId}/containers/{containerId}/tags" )
    Response addTags( @PathParam( "environmentId" ) String environmentId,
                      @PathParam( "containerId" ) String containerId, @FormParam( "tags" ) String tags );

    @DELETE
    @Path( "{environmentId}/containers/{containerId}/tags/{tag}" )
    Response removeTag( @PathParam( "environmentId" ) String environmentId,
                        @PathParam( "containerId" ) String containerId, @PathParam( "tag" ) String tag );

    @GET
    @Path( "{environmentId}/containers/{containerId}/ssh" )
    @Produces( { MediaType.TEXT_PLAIN } )
    Response setupContainerSsh( @PathParam( "environmentId" ) String environmentId,
                                @PathParam( "containerId" ) String containerId );


    /** Share **************************************************** */

    @GET
    @Path( "shared/users/{objectId}" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response getSharedUsers( @PathParam( "objectId" ) String objectId );


    @POST
    @Path( "share" )
    Response shareEnvironment( @FormParam( "users" ) String users, @FormParam( "environmentId" ) String environmentId );
}
