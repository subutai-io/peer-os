package io.subutai.core.identity.rest;


import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;


public interface RestService
{
    @POST
    @Path( "gettoken" )
    @Produces( { MediaType.TEXT_PLAIN } )
    public String createToken( @FormParam( "username" ) String userName, @FormParam( "password" ) String password );

    @GET
    @Path( "gettoken" )
    @Produces( MediaType.TEXT_PLAIN )
    public String getToken( @QueryParam( "username" ) String userName, @QueryParam( "password" ) String password );

    @POST
    @Path( "token" )
    @Produces( MediaType.APPLICATION_JSON )
    @Consumes( MediaType.APPLICATION_JSON )
    public Token createToken( UserCrdentials userCrdentials );
}