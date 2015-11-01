package io.subutai.core.identity.rest;


import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;


public interface RestService
{
    @POST
    @Path( "gettoken" )
    @Produces( { MediaType.TEXT_PLAIN } )
    public String getToken( @FormParam( "username" ) String userName,@FormParam( "password" ) String password  );
}