package org.safehaus.subutai.rest.template.manager;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public interface RestService {

    @GET
    @Path("management_hostname")
    @Produces({MediaType.TEXT_PLAIN})
    public String getManagementHostName();

    @PUT
    @Path("management_hostname/{hostname}")
    public void setManagementHostName(@PathParam("hostname") String hostname);

    @POST
    @Path("import")
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED})
    @Produces({MediaType.TEXT_PLAIN})
    public String importTemplate(byte[] input);

    @GET
    @Path("export/{template}")
    @Produces({MediaType.APPLICATION_OCTET_STREAM})
    public Response exportTemplate(@PathParam("template") String templateName);

}
