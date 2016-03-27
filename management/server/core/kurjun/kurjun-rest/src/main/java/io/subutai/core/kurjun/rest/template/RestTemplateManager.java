package io.subutai.core.kurjun.rest.template;


import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
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

    String ID_PARAM = "id";
    String NAME_PARAM = "name";
    String VERSION_PARAM = "version";
    String PACKAGE_FILE_PART_NAME = "package";
    String TYPE_PARAM = "type";
    String IS_KURJUN_CLIENT_PARAM = "kc";
    String RESPONSE_TYPE_ID = "id";


    @GET
    @Path( "get" )
    @Produces( MediaType.APPLICATION_OCTET_STREAM )
    Response getTemplate( @QueryParam( "repository" ) String repository, @QueryParam( ID_PARAM ) String id,
                          @QueryParam( NAME_PARAM ) String name, @QueryParam( VERSION_PARAM ) String version,
                          @QueryParam( TYPE_PARAM ) String type,
                          @QueryParam( IS_KURJUN_CLIENT_PARAM ) boolean isKurjunClient );


    @GET
    @Path( "info" )
    @Produces( MediaType.APPLICATION_JSON )
    Response getTemplateInfo( @QueryParam( "repository" ) String repository, @QueryParam( ID_PARAM ) String id,
                              @QueryParam( NAME_PARAM ) String name, @QueryParam( VERSION_PARAM ) String version,
                              @QueryParam( "type") String type,
                              @QueryParam( IS_KURJUN_CLIENT_PARAM ) boolean isKurjunClient );

    @GET
    @Path( "list" )
    @Produces( MediaType.APPLICATION_JSON )
    Response getTemplateList( @QueryParam( "repository" ) String repository,
                              @QueryParam( IS_KURJUN_CLIENT_PARAM ) boolean isKurjunClient );

    @POST
    @Path( "upload" )
    @Produces( MediaType.TEXT_PLAIN )
    @Consumes( MediaType.MULTIPART_FORM_DATA )
    Response uploadTemplate( @QueryParam( "repository" ) String repository,
                             @Multipart( PACKAGE_FILE_PART_NAME ) Attachment attachment );

    @DELETE
    @Path( "delete" )
    @Produces( MediaType.TEXT_PLAIN )
    Response deleteTemplate( @QueryParam( "repository" ) String repository, @QueryParam( ID_PARAM ) String id );

}
