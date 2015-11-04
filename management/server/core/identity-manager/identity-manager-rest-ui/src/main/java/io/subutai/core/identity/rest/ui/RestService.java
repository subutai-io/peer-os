package io.subutai.core.identity.rest.ui;


import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public interface RestService
{
    @GET
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getUsers();

    @POST
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response setUser( @FormParam( "username" ) String username,
                                   @FormParam( "full_name" ) String fullName,
                                   @FormParam( "password" ) String password,
                                   @FormParam( "email" ) String email);

    @GET
    @Path( "roles" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getRoles();

    @GET
    @Path( "permissions" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Response getPermissions();
}