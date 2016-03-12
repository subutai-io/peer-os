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
    static final String MD5_PARAM = "md5";
    static final String NAME_PARAM = "name";
    static final String FILE_PART_NAME = "file";
    static final String IS_KURJUN_CLIENT_PARAM = "kc";


    @GET
    @Path( "get" )
    @Produces( MediaType.TEXT_PLAIN )
    Response getFile(
            @QueryParam( MD5_PARAM ) String md5,
            @QueryParam( NAME_PARAM ) String name,
            @QueryParam( IS_KURJUN_CLIENT_PARAM ) boolean isKurjunClient
    );


    @GET
    @Path( "info" )
    @Produces( MediaType.APPLICATION_JSON )
    Response getFileInfo(
            @QueryParam( MD5_PARAM ) String md5,
            @QueryParam( NAME_PARAM ) String name,
            @QueryParam( IS_KURJUN_CLIENT_PARAM ) boolean isKurjunClient
    );


    @GET
    @Path( "list" )
    @Produces( MediaType.APPLICATION_JSON )
    Response getFileList( @QueryParam( IS_KURJUN_CLIENT_PARAM ) boolean isKurjunClient );


    @POST
    @Path( "upload" )
    @Produces( MediaType.TEXT_PLAIN )
    @Consumes( MediaType.MULTIPART_FORM_DATA )
    Response uploadFile( @Multipart( FILE_PART_NAME ) Attachment attachment );


    @DELETE
    @Path( "delete" )
    @Produces( MediaType.TEXT_PLAIN )
    Response deleteFile( @QueryParam( MD5_PARAM ) String md5
    );

}
