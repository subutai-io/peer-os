package io.subutai.localpeer.rest.ui;


import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;


public interface RestService
{

    @GET
    @Path( "containers/notregistered" )
    Response getNotRegisteredContainers();

    @DELETE
    @Path( "containers/notregistered/{containerId}" )
    Response destroyNotRegisteredContainer( @PathParam( "containerId" ) String containerId );
}