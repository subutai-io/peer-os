package io.subutai.core.environment.rest.ui;


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


    /** Domain **************************************************** */

    @GET
    @Path( "domains" )
    @Produces( { MediaType.TEXT_PLAIN } )
    Response getDefaultDomainName();


    /** Environments **************************************************** */

    @GET
    @Produces( { MediaType.APPLICATION_JSON } )
    Response listEnvironments();

    @POST
    @Path( "build" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response build(@FormParam( "name" ) String name, @FormParam( "topology" ) String topologyJson );


    @POST
    @Path( "build/advanced" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response buildAdvanced( @FormParam( "name" ) String name, @FormParam( "topology" ) String topologyJson );


    @POST
    @Produces( { MediaType.APPLICATION_JSON } )
    @Path( "{environmentId}/modify" )
    Response modify(@PathParam( "environmentId" ) String environmentId,
                    @FormParam( "topology" ) String topologyJson,
                    @FormParam( "removedContainers" ) String removedContainers );

    @POST
    @Produces( { MediaType.APPLICATION_JSON } )
    @Path( "{environmentId}/modify/advanced" )
    Response modifyAdvanced(@PathParam( "environmentId" ) String environmentId,
                            @FormParam( "topology" ) String topologyJson,
                            @FormParam( "removedContainers" ) String removedContainers );


    @DELETE
    @Path( "{environmentId}" )
    Response destroyEnvironment( @PathParam( "environmentId" ) String environmentId );


    /** Environments SSH keys **************************************************** */

    @GET
    @Path( "{environmentId}/keys" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response getSshKeys(@PathParam( "environmentId" ) String environmentId );


    @POST
    @Path( "{environmentId}/keys" )
    Response addSshKey( @PathParam( "environmentId" ) String environmentId, @FormParam( "key" ) String key );


    @DELETE
    @Path( "{environmentId}/keys" )
    Response removeSshKey( @PathParam( "environmentId" ) String environmentId, @QueryParam( "key" ) String key );


    /** Environment domains **************************************************** */

    @GET
    @Path( "{environmentId}/domain" )
    Response getEnvironmentDomain( @PathParam( "environmentId" ) String environmentId );


    @GET
    @Path( "/domains/strategies" )
    Response listDomainLoadBalanceStrategies();


    @POST
    @Consumes( MediaType.MULTIPART_FORM_DATA )
    @Path( "{environmentId}/domains" )
    Response addEnvironmentDomain( @PathParam( "environmentId" ) String environmentId,
                                   @Multipart( "hostName" ) String hostName,
                                   @Multipart( "strategy" ) String strategyJson,
                                   @Multipart( value = "file" ) Attachment attr );


    @DELETE
    @Path( "{environmentId}/domains" )
    Response removeEnvironmentDomain( @PathParam( "environmentId" ) String environmentId );


    @GET
    @Path( "{environmentId}/containers/{containerId}/domain" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response isContainerDomain( @PathParam( "environmentId" ) String environmentId,
                                @PathParam( "containerId" ) String containerId );


    @PUT
    @Path( "{environmentId}/containers/{containerId}/domain" )
    Response setContainerDomain( @PathParam( "environmentId" ) String environmentId,
                                 @PathParam( "containerId" ) String containerId );


    /** Containers **************************************************** */

    @DELETE
    @Path( "containers/{containerId}" )
    Response destroyContainer( @PathParam( "containerId" ) String containerId );

    @GET
    @Path( "containers/{containerId}/state" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response getContainerState( @PathParam( "containerId" ) String containerId );

    @PUT
    @Path( "containers/{containerId}/start" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response startContainer( @PathParam( "containerId" ) String containerId );

    @PUT
    @Path( "containers/{containerId}/stop" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response stopContainer( @PathParam( "containerId" ) String containerId );


    /** Container types **************************************************** */

    @GET
    @Path( "containers/types" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response listContainerTypes();


    @GET
    @Path( "containers/types/info" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response listContainerTypesInfo();


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
    @Path( "{environmentId}/share" )
    Response share(@FormParam( "users" ) String users, @PathParam( "environmentId" ) String environmentId );
}
