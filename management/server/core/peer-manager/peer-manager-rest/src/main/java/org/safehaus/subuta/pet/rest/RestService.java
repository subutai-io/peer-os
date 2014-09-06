package org.safehaus.subuta.pet.rest;


import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;


public interface RestService {



    @POST
    @Path("peers")
    @Produces({ MediaType.APPLICATION_JSON })
    public String registerPeer( @QueryParam( "config" ) String config );
}