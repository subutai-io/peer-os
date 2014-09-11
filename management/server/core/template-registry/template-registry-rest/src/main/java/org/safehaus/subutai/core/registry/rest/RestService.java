package org.safehaus.subutai.core.registry.rest;


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


public interface RestService {

    @GET
    @Path("templates/{templateName}")
    @Produces({ MediaType.APPLICATION_JSON })
    public String getTemplate( @PathParam("templateName") String templateName );

    @POST
    @Path("templates")
    public Response registerTemplate( @FormParam("config") String configFilePath,
                                      @FormParam("packages") String packagesFilePath,
                                      @FormParam("md5sum") String md5sum );

    @DELETE
    @Path("templates/{templateName}")
    public Response unregisterTemplate( @PathParam("templateName") String templateName );

    @GET
    @Path("templates/{templateName}/arch/{lxcArch}")
    @Produces({ MediaType.APPLICATION_JSON })
    public String getTemplate( @PathParam("templateName") String templateName, @PathParam("lxcArch") String lxcArch );

    @GET
    @Path("templates/{childTemplateName}/parent")
    @Produces({ MediaType.APPLICATION_JSON })
    public String getParentTemplate( @PathParam("childTemplateName") String childTemplateName );

    @GET
    @Path("templates/{childTemplateName}/arch/{lxcArch}/parent")
    @Produces({ MediaType.APPLICATION_JSON })
    public String getParentTemplate( @PathParam("childTemplateName") String childTemplateName,
                                     @PathParam("lxcArch") String lxcArch );

    @GET
    @Path("templates/{childTemplateName}/parents")
    @Produces({ MediaType.APPLICATION_JSON })
    public String getParentTemplates( @PathParam("childTemplateName") String childTemplateName );

    @GET
    @Path("templates/{childTemplateName}/arch/{lxcArch}/parents")
    @Produces({ MediaType.APPLICATION_JSON })
    public String getParentTemplates( @PathParam("childTemplateName") String childTemplateName,
                                      @PathParam("lxcArch") String lxcArch );

    @GET
    @Path("templates/{parentTemplateName}/children")
    @Produces({ MediaType.APPLICATION_JSON })
    public String getChildTemplates( @PathParam("parentTemplateName") String parentTemplateName );

    @GET
    @Path("templates/{parentTemplateName}/arch/{lxcArch}/children")
    @Produces({ MediaType.APPLICATION_JSON })
    public String getChildTemplates( @PathParam("parentTemplateName") String parentTemplateName,
                                     @PathParam("lxcArch") String lxcArch );

    @GET
    @Path("templates/tree")
    @Produces({ MediaType.APPLICATION_JSON })
    public String getTemplateTree();

    @GET
    @Path("templates/{templateName}/is-used-on-fai")
    @Produces({ MediaType.APPLICATION_JSON })
    public String isTemplateInUse( @PathParam("templateName") String templateName );

    @PUT
    @Path("templates/{templateName}/fai/{faiHostname}/is-used/{isInUse}")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response setTemplateInUse( @PathParam("faiHostname") String faiHostname,
                                      @PathParam("templateName") String templateName,
                                      @PathParam("isInUse") String isInUse );

    @GET
    @Path("templates")
    @Produces({ MediaType.APPLICATION_JSON })
    public String listTemplates();


    @GET
    @Path("templates/arch/{lxcArch}")
    @Produces({ MediaType.APPLICATION_JSON })
    public String listTemplates( @PathParam("lxcArch") String lxcArch );


    @GET
    @Path("templates/plain-list")
    @Produces({ MediaType.TEXT_PLAIN })
    public String listTemplatesPlain();

    @GET
    @Path("templates/arch/{lxcArch}/plain-list")
    @Produces({ MediaType.TEXT_PLAIN })
    public String listTemplatesPlain( @PathParam("lxcArch") String lxcArch );
}