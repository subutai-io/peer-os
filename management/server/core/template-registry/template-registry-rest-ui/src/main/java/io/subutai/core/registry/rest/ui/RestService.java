package io.subutai.core.registry.rest.ui;


import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;


public interface RestService
{

    @POST
    @Path( "templates" )
    public Response registerTemplate( @FormParam( "config" ) String configFilePath,
                                      @FormParam( "packages" ) String packagesFilePath,
                                      @FormParam( "md5sum" ) String md5sum );

    @GET
    @Path( "changedFiles/{template1}/{template2}" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getChangedFiles( @PathParam( "template1" ) String template1, @PathParam( "template2" ) String template2 );

//    @GET
//    @Path( "file_diff/{template1}/{template2}/{file_id}" )
//    @Produces( { MediaType.APPLICATION_JSON } )
//    public Response getFileDiff( @PathParam( "template1" ) String template1, @PathParam( "template2" ) String template2, @PathParam( "file_id" ) String fileId );

    @POST
    @Path( "templates/import" )
    @Consumes( { MediaType.MULTIPART_FORM_DATA } )
    public Response importTemplate( @Multipart( "file" ) Attachment in, @Multipart( "config_dir" ) String configDir );


    @GET
    @Path( "templates" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response listTemplates();


    @GET
    @Path( "templates/arch/{lxcArch}" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response listTemplates( @PathParam( "lxcArch" ) String lxcArch );


    @GET
    @Path( "templates/plain-list" )
    @Produces( { MediaType.TEXT_PLAIN } )
    public Response listTemplatesPlain();

    @GET
    @Path( "templates/arch/{lxcArch}/plain-list" )
    @Produces( { MediaType.TEXT_PLAIN } )
    public Response listTemplatesPlain( @PathParam( "lxcArch" ) String lxcArch );


    @GET
    @Path( "templates/{templateName}" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getTemplate( @PathParam( "templateName" ) String templateName );


    @GET
    @Path( "templates/{templateName}/{templateVersion}" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getTemplate( @PathParam( "templateName" ) String templateName,
                                 @PathParam( "templateVersion" ) String templateVersion );


    @DELETE
    @Path( "templates/{templateName}" )
    public Response unregisterTemplate( @PathParam( "templateName" ) String templateName );


    @DELETE
    @Path( "templates/{templateName}/{templateVersion}" )
    public Response unregisterTemplate( @PathParam( "templateName" ) String templateName,
                                        @PathParam( "templateVersion" ) String templateVersion );


    @DELETE
    @Path( "templates/{templateName}/{templateVersion}/remove" )
    public Response removeTemplate( @PathParam( "templateName" ) String templateName,
                                    @PathParam( "templateVersion" ) String templateVersion );

    @GET
    @Path( "templates/{templateName}/{templateVersion}/arch/{lxcArch}" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getTemplate( @PathParam( "templateName" ) String templateName,
                                 @PathParam( "templateVersion" ) String templateVersion,
                                 @PathParam( "lxcArch" ) String lxcArch );


    @GET
    @Path( "templates/{childTemplateName}/parent" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getParentTemplate( @PathParam( "childTemplateName" ) String childTemplateName );


    @GET
    @Path( "templates/{childTemplateName}/{templateVersion}/parent" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getParentTemplate( @PathParam( "childTemplateName" ) String childTemplateName,
                                       @PathParam( "templateVersion" ) String templateVersion );

    @GET
    @Path( "templates/{childTemplateName}/{templateVersion}/arch/{lxcArch}/parent" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getParentTemplate( @PathParam( "childTemplateName" ) String childTemplateName,
                                       @PathParam( "templateVersion" ) String templateVersion,
                                       @PathParam( "lxcArch" ) String lxcArch );


    @GET
    @Path( "templates/{childTemplateName}/parents" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getParentTemplates( @PathParam( "childTemplateName" ) String childTemplateName );


    @GET
    @Path( "templates/{childTemplateName}/{templateVersion}/parents" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getParentTemplates( @PathParam( "childTemplateName" ) String childTemplateName,
                                        @PathParam( "templateVersion" ) String templateVersion );

    @GET
    @Path( "templates/{childTemplateName}/{templateVersion}/arch/{lxcArch}/parents" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getParentTemplates( @PathParam( "childTemplateName" ) String childTemplateName,
                                        @PathParam( "templateVersion" ) String templateVersion,
                                        @PathParam( "lxcArch" ) String lxcArch );


    @GET
    @Path( "templates/{parentTemplateName}/children" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getChildTemplates( @PathParam( "parentTemplateName" ) String parentTemplateName );


    @GET
    @Path( "templates/{parentTemplateName}/{templateVersion}/children" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getChildTemplates( @PathParam( "parentTemplateName" ) String parentTemplateName,
                                       @PathParam( "templateVersion" ) String templateVersion );

    @GET
    @Path( "templates/{parentTemplateName}/{templateVersion}/arch/{lxcArch}/children" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getChildTemplates( @PathParam( "parentTemplateName" ) String parentTemplateName,
                                       @PathParam( "templateVersion" ) String templateVersion,
                                       @PathParam( "lxcArch" ) String lxcArch );


    @GET
    @Path( "templates/{templateName}/{templateVersion}/is-used-on-fai" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response isTemplateInUse( @PathParam( "templateName" ) String templateName,
                                     @PathParam( "templateVersion" ) String templateVersion );


    @PUT
    @Path( "templates/{templateName}/{templateVersion}/fai/{faiHostname}/is-used/{isInUse}" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response setTemplateInUse( @PathParam( "faiHostname" ) String faiHostname,
                                      @PathParam( "templateName" ) String templateName,
                                      @PathParam( "templateVersion" ) String templateVersion,
                                      @PathParam( "isInUse" ) String isInUse );
}