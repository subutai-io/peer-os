package org.safehaus.subutai.rest.templateregistry;


import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


public interface RestService {

	@GET
	@Path ("get_template/{templateName}")
	@Produces ({MediaType.APPLICATION_JSON})
	public String getTemplate(@PathParam ("templateName") String templateName);

	@GET
	@Path ("register_template")
	public Response registerTemplate(@QueryParam ("config") String configFilePath,
	                                 @QueryParam ("packages") String packagesFilePath);

	@GET
	@Path ("unregister_template/{templateName}")
	public Response unregisterTemplate(@PathParam ("templateName") String templateName);

	@GET
	@Path ("get_template/{templateName}/{lxcArch}")
	@Produces ({MediaType.APPLICATION_JSON})
	public String getTemplate(@PathParam ("templateName") String templateName,
	                          @PathParam ("lxcArch") String lxcArch);

	@GET
	@Path ("get_parent_template/{childTemplateName}")
	@Produces ({MediaType.APPLICATION_JSON})
	public String getParentTemplate(@PathParam ("childTemplateName") String childTemplateName);

	@GET
	@Path ("get_parent_template/{childTemplateName}/{lxcArch}")
	@Produces ({MediaType.APPLICATION_JSON})
	public String getParentTemplate(@PathParam ("childTemplateName") String childTemplateName,
	                                @PathParam ("lxcArch") String lxcArch);

	@GET
	@Path ("get_parent_templates/{childTemplateName}")
	@Produces ({MediaType.APPLICATION_JSON})
	public String getParentTemplates(@PathParam ("childTemplateName") String childTemplateName);

	@GET
	@Path ("get_parent_templates/{childTemplateName}/{lxcArch}")
	@Produces ({MediaType.APPLICATION_JSON})
	public String getParentTemplates(@PathParam ("childTemplateName") String childTemplateName,
	                                 @PathParam ("lxcArch") String lxcArch);

	@GET
	@Path ("get_child_templates/{parentTemplateName}")
	@Produces ({MediaType.APPLICATION_JSON})
	public String getChildTemplates(@PathParam ("parentTemplateName") String parentTemplateName);

	@GET
	@Path ("get_child_templates/{parentTemplateName}/{lxcArch}")
	@Produces ({MediaType.APPLICATION_JSON})
	public String getChildTemplates(@PathParam ("parentTemplateName") String parentTemplateName,
	                                @PathParam ("lxcArch") String lxcArch);

	@GET
	@Path ("get_template_tree")
	@Produces ({MediaType.APPLICATION_JSON})
	public String getTemplateTree();

	@GET
	@Path ("list_templates")
	@Produces ({MediaType.APPLICATION_JSON})
	public String listTemplates();


	@GET
	@Path ("list_templates/{lxcArch}")
	@Produces ({MediaType.APPLICATION_JSON})
	public String listTemplates(@PathParam ("lxcArch") String lxcArch);


	@GET
	@Path ("list_templates_plain")
	@Produces ({MediaType.TEXT_PLAIN})
	public String listTemplatesPlain();

	@GET
	@Path ("list_templates_plain/{lxcArch}")
	@Produces ({MediaType.TEXT_PLAIN})
	public String listTemplatesPlain(@PathParam ("lxcArch") String lxcArch);
}