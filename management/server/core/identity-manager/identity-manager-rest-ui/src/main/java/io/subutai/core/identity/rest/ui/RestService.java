package io.subutai.core.identity.rest.ui;


import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public interface RestService
{
    /** Users ***********************************************/

    @GET
    @Produces( { MediaType.APPLICATION_JSON } )
    Response getUsers();

    @GET
    @Path( "/user" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response getActiveUser();


    @POST
    @Produces( { MediaType.APPLICATION_JSON } )
    Response saveUser( @FormParam( "username" ) String username,
                             @FormParam( "full_name" ) String fullName,
                             @FormParam( "password" ) String password,
                             @FormParam( "email" ) String email,
                             @FormParam( "roles" ) String roles, @FormParam( "user_id" ) Long userId,
                       @FormParam( "public_key" ) String publicKey );


    @POST
	@Path( "/signup" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response signUp( @FormParam( "username" ) String username,
                       @FormParam( "full_name" ) String fullName,
                       @FormParam( "password" ) String password,
                       @FormParam( "email" ) String email,
                       @FormParam( "public_key" ) String publicKey );


    @POST
	@Path( "/approve" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response approve( @FormParam( "username" ) String username,@FormParam( "roles" ) String roles );

    @DELETE
    @Path( "/{userId}" )
    Response deleteUser( @PathParam( "userId" ) Long userId );



    /** Roles ***********************************************/

    @GET
    @Path( "roles" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response getRoles();

    @POST
    @Path( "roles" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response saveRole( @FormParam( "rolename" ) String rolename,
                              @FormParam( "permission" ) String permissionJson,
                              @FormParam( "role_id" ) Long roleId );

    @DELETE
    @Path( "roles/{roleId}" )
    Response deleteRole( @PathParam( "roleId" ) Long roleName );



    /** Permissions ***********************************************/

    @GET
    @Path( "permissions" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response getPermissions();


    @GET
    @Path( "permissions/scopes" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response getPermissionScopes();



    /** Tokens ***********************************************/

    @GET
    @Path( "users/tokens" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response getAllUserTokens();

    @POST
    @Path( "users/tokens" )
    Response createUserToken( @FormParam( "userId" ) Long userId,
                              @FormParam( "token" ) String token,
                              @FormParam( "period" ) Integer period );

    @PUT
    @Path( "users/tokens" )
    Response updateUserToken( @FormParam( "userId" ) Long userId,
                              @FormParam( "token" ) String token,
                              @FormParam( "newToken" ) String newToken,
                              @FormParam( "period" ) Integer period );


    @DELETE
    @Path( "users/tokens/{tokenId}" )
    Response removeUserToken( @PathParam( "tokenId" ) String tokenId );


    @GET
    @Path( "tokens/types" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response getTokenTypes();


}