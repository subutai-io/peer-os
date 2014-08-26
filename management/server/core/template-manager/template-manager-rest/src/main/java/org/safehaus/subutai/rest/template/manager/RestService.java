package org.safehaus.subutai.rest.template.manager;

import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;

public interface RestService {

	@GET
	@Path ("management_hostname")
	@Produces ({MediaType.TEXT_PLAIN})
	public String getManagementHostName();

	@PUT
	@Path ("management_hostname/{hostname}")
	public void setManagementHostName(@PathParam ("hostname") String hostname);

	@POST
	@Path ("import")
	@Consumes ({MediaType.MULTIPART_FORM_DATA})
	@Produces ({MediaType.TEXT_PLAIN})
	public Response importTemplate(@Multipart ("file") InputStream in,
	                               @Multipart ("config_dir") String configDir);

	@GET
	@Path ("export/{template}")
	@Produces ({MediaType.APPLICATION_OCTET_STREAM})
	public Response exportTemplate(@PathParam ("template") String templateName);

}
