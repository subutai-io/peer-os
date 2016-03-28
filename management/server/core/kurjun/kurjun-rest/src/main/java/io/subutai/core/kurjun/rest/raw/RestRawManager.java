package io.subutai.core.kurjun.rest.raw;


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


public interface RestRawManager
{
    String MD5_PARAM = "md5";
    String NAME_PARAM = "name";
    String FILE_PART_NAME = "file";
    String IS_KURJUN_CLIENT_PARAM = "kc";
    String REPOSITORY = "repository";

    @GET
    @Path( "get" )
    @Produces( MediaType.APPLICATION_OCTET_STREAM )
    Response download( @QueryParam( REPOSITORY ) String repository, @QueryParam( MD5_PARAM ) String md5,
                      @QueryParam( NAME_PARAM ) String name,
                      @QueryParam( IS_KURJUN_CLIENT_PARAM ) boolean isKurjunClient );

    @GET
    @Path( "info" )
    @Produces( MediaType.APPLICATION_JSON )
    Response info( @QueryParam( REPOSITORY ) String repository, @QueryParam( MD5_PARAM ) String md5,
                          @QueryParam( NAME_PARAM ) String name,
                          @QueryParam( IS_KURJUN_CLIENT_PARAM ) boolean isKurjunClient );

    @GET
    @Path( "list" )
    @Produces( MediaType.APPLICATION_JSON )
    Response list( @QueryParam( REPOSITORY ) String repository );

    @POST
    @Path( "upload" )
    @Produces( MediaType.TEXT_PLAIN )
    @Consumes( MediaType.MULTIPART_FORM_DATA )
    Response upload( @QueryParam( REPOSITORY ) String repository,
                         @Multipart( FILE_PART_NAME ) Attachment attachment );

    @DELETE
    @Path( "delete" )
    @Produces( MediaType.TEXT_PLAIN )
    Response delete( @QueryParam( REPOSITORY ) String repository, @QueryParam( MD5_PARAM ) String md5 );

    @GET
    @Path( "md5" )
    @Produces( MediaType.TEXT_PLAIN )
    Response md5( @QueryParam( REPOSITORY ) String repository );
}
