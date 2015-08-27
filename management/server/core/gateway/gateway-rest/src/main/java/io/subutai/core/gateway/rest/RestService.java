package io.subutai.core.gateway.rest;


import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;


public interface RestService
{
    @POST
    @Path( "authenticate" )
    @Consumes( MediaType.APPLICATION_JSON )
    String authenticate( UserCredentials userCredentials );
}