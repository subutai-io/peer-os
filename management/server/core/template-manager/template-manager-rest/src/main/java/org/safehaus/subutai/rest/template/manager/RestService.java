package org.safehaus.subutai.rest.template.manager;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

@Path("template")
public interface RestService {

    @POST
    @Path("import")
    public String importTemplate();

}
