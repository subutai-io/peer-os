package org.safehaus.subuta.pet.rest;


import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;


public interface RestService {

    @GET
    @Path( "welcome/{name}" )
    @Produces( MediaType.APPLICATION_JSON )
    public String welcome( @PathParam( "name" ) String name );
}