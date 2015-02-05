package org.safehaus.subutai.core.identity.rest;


import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;


public interface RestService
{
    @GET
    @Path( "key/{username}" )
    @Produces( { MediaType.TEXT_PLAIN } )
    public String getKey( @PathParam( "username" ) String username );
}