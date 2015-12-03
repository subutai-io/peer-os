 
package io.subutai.core.kurjun.rest.template;

 
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
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
 *
 */
public interface RestTemplateManager
{
    // TODO: copied from TemplateServlet. Put to some common place 
    static final String MD5_PARAM = "md5";
    static final String NAME_PARAM = "name";
    static final String VERSION_PARAM = "version";
    static final String PACKAGE_FILE_PART_NAME = "package";
    static final String TYPE_PARAM = "type";
    static final String IS_KURJUN_CLIENT_PARAM = "kc";
    static final String RESPONSE_TYPE_MD5 = "md5";

    @GET
    @Path( "{repository}/get" )
    @Produces( MediaType.TEXT_PLAIN )
    Response getTemplate( @PathParam( "repository" ) String repository,
                          @QueryParam( MD5_PARAM ) String md5,
                          @QueryParam( NAME_PARAM ) String name,
                          @QueryParam( VERSION_PARAM ) String version,
                          @QueryParam( TYPE_PARAM ) String type,
                          @QueryParam( IS_KURJUN_CLIENT_PARAM ) boolean isKurjunClient
    );
    
    
    @GET
    @Path( "{repository}/info" )
    @Produces( MediaType.APPLICATION_JSON )
    Response getTemplateInfo( @PathParam( "repository" ) String repository,
                          @QueryParam( MD5_PARAM ) String md5,
                          @QueryParam( NAME_PARAM ) String name,
                          @QueryParam( VERSION_PARAM ) String version,
                          @QueryParam( IS_KURJUN_CLIENT_PARAM ) boolean isKurjunClient
    );
    
    @GET
    @Path( "{repository}/list" )
    @Produces( MediaType.APPLICATION_JSON )
    Response getTemplateList( @PathParam( "repository") String repository,
                              @QueryParam( IS_KURJUN_CLIENT_PARAM ) boolean isKurjunClient );


    @POST
    @Path( "upload/{repository}" )
    @Produces( MediaType.TEXT_PLAIN )
    @Consumes( MediaType.MULTIPART_FORM_DATA )
    Response uploadTemplate( @PathParam( "repository" ) String repository,
                             @Multipart( PACKAGE_FILE_PART_NAME ) Attachment attachment
    );


    @DELETE
    @Path( "{repository}" )
    @Produces( MediaType.TEXT_PLAIN )
    Response deleteTemplates( @PathParam( "repository" ) String repository,
                              @QueryParam( MD5_PARAM ) String md5
    );
}
