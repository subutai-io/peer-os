package io.subutai.core.environment.rest;


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

import io.subutai.common.environment.Blueprint;
import io.subutai.common.network.DomainLoadBalanceStrategy;
import io.subutai.common.peer.EnvironmentId;


public interface RestService
{

    @GET
    @Produces(  MediaType.APPLICATION_JSON  )
    public Response listEnvironments();

    @GET
    @Path( "domain" )
    @Produces( { MediaType.TEXT_PLAIN } )
    public Response getDefaultDomainName();

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Path( "domain" )
    Response addEnvironmentDomain( @Multipart( "environmentId" ) String environmentId,
                                   @Multipart( "hostName" ) String hostName,
                                   @Multipart( "strategy" ) DomainLoadBalanceStrategy strategy,
                                   @Multipart(value = "file") Attachment attr );

    @GET
    @Path( "container/environmentId" )
    @Produces( { MediaType.TEXT_PLAIN } )
    public Response getContainerEnvironmentId( @QueryParam( "containerId" ) String containerId );


    @GET
    @Path( "{environmentId}" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response viewEnvironment( @PathParam( "environmentId" ) String environmentId );

    @POST
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    public Response createEnvironment( Blueprint blueprint );

    @PUT
    @Path( "{environmentId}" )
    @Consumes( MediaType.APPLICATION_JSON )
    public void growEnvironment( @PathParam( "environmentId" ) String environmentId, final Blueprint blueprint );

    @POST
    @Path( "key" )
    public Response setSshKey( @FormParam( "environmentId" ) String environmentId, @FormParam( "key" ) String key );


    @DELETE
    @Path( "key" )
    public Response removeSshKey( @QueryParam( "environmentId" ) String environmentId );

    @DELETE
    @Path( "{environmentId}" )
    public Response destroyEnvironment( @PathParam( "environmentId" ) String environmentId );

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