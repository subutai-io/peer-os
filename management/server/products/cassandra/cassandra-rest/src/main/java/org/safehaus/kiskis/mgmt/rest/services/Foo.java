package org.safehaus.kiskis.mgmt.rest.services;

/**
 * Created by bahadyr on 5/5/14.
 */

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/foo")
public interface Foo {

    @GET
    @Path("/hello")
    String hello();

}