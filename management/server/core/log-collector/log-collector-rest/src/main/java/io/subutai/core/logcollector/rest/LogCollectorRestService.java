package io.subutai.core.logcollector.rest;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

public interface LogCollectorRestService
{
    @Path( "/" )
    @POST
    Response respondPost();

    @Path( "/" )
    @GET
    Response respondGet();

    @Path( "_bulk" )
    @POST
    Response respondGet( String data );
}
