package io.subutai.core.kurjun.rest.template;


import javax.ws.rs.Consumes;
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

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;


/**
 * HTTP service for templates repository. Built using CXF to be compatible with Subutai.
 */
public interface RestTemplateManager
{
    // TODO: copied from TemplateServlet. Put to some common place 
    String ID_PARAM = "id";
    String NAME_PARAM = "name";
    String VERSION_PARAM = "version";
    String PACKAGE_FILE_PART_NAME = "package";
    String TYPE_PARAM = "type";
    String IS_KURJUN_CLIENT_PARAM = "kc";
    String RESPONSE_TYPE_ID = "id";

    @GET
    @Path( "repositories" )
    @Produces( MediaType.APPLICATION_JSON )
    Response getRepositories();


    @GET
    @Path( "shared-info" )
    @Produces( MediaType.APPLICATION_JSON )
    Response getSharedTemplateInfos( @QueryParam( ID_PARAM ) String id );


    @GET
    @Path( "{repository}/can-upload" )
    @Produces( MediaType.APPLICATION_OCTET_STREAM)
    Response checkUploadAllowed( @PathParam( "repository" ) String repository );


    @GET
    @Path( "{repository}/get" )
    @Produces( MediaType.APPLICATION_OCTET_STREAM)
    Response getTemplate( @PathParam( "repository" ) String repository, @QueryParam( ID_PARAM ) String id,
                          @QueryParam( NAME_PARAM ) String name, @QueryParam( VERSION_PARAM ) String version,
                          @QueryParam( TYPE_PARAM ) String type,
                          @QueryParam( IS_KURJUN_CLIENT_PARAM ) boolean isKurjunClient );


    @GET
    @Path( "{repository}/info" )
    @Produces( MediaType.APPLICATION_JSON )
    Response getTemplateInfo( @PathParam( "repository" ) String repository, @QueryParam( ID_PARAM ) String id,
                              @QueryParam( NAME_PARAM ) String name, @QueryParam( VERSION_PARAM ) String version,
                              @QueryParam( IS_KURJUN_CLIENT_PARAM ) boolean isKurjunClient );

    @GET
    @Path( "{repository}/list" )
    @Produces( MediaType.APPLICATION_JSON )
    Response getTemplateList( @PathParam( "repository" ) String repository,
                              @QueryParam( IS_KURJUN_CLIENT_PARAM ) boolean isKurjunClient );


    @GET
    @Path( "{repository}/template-list" )
    @Produces( MediaType.APPLICATION_JSON )
    Response getTemplateListSimple( @PathParam( "repository" ) String repository );


    @POST
    @Path( "upload/{repository}" )
    @Produces( MediaType.TEXT_PLAIN )
    @Consumes( MediaType.MULTIPART_FORM_DATA )
    Response uploadTemplate( @PathParam( "repository" ) String repository,
                             @Multipart( PACKAGE_FILE_PART_NAME ) Attachment attachment );


    @DELETE
    @Path( "{repository}" )
    @Produces( MediaType.TEXT_PLAIN )
    Response deleteTemplate( @PathParam( "repository" ) String repository, @QueryParam( ID_PARAM ) String id );


    @PUT
    @Path( "share/{targetUserName}" )
    @Produces( MediaType.TEXT_PLAIN )
    Response shareTemplate( @PathParam( "targetUserName" ) String targetUserName, @QueryParam( ID_PARAM ) String id );


    @DELETE
    @Path( "share/{targetUserName}" )
    @Produces( MediaType.TEXT_PLAIN )
    Response unshareTemplate( @PathParam( "targetUserName" ) String targetUserName, @QueryParam( ID_PARAM ) String id );
}
