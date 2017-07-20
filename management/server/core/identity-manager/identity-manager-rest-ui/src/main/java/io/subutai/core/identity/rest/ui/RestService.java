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

    /** Kurjun ***********/
    @GET
    @Produces( { MediaType.TEXT_PLAIN } )
    @Path( "kurjun/authid" )
    Response getKurjunAuthId();

    @POST
    @Produces( { MediaType.TEXT_PLAIN } )
    @Path( "kurjun/token" )
    Response obtainKurjunToken( @FormParam( "signedAuthId" ) String signedAuthId );

    @GET
    @Produces( { MediaType.TEXT_PLAIN } )
    @Path( "kurjun/token" )
    Response getObtainedKurjunToken();

    @GET
    @Produces( { MediaType.TEXT_PLAIN } )
    @Path( "kurjun/isRegistered" )
    Response isRegisteredWithKurjun();


    @POST
    @Produces( { MediaType.TEXT_PLAIN } )
    @Path( "kurjun/sign" )
    Response submitSignedTemplateHash( @FormParam( "signedHash" ) String signedTemplateHash );

    /** Users ***********************************************/

    @GET
    @Produces( { MediaType.APPLICATION_JSON } )
    Response getUsers();

    @GET
    @Produces( { MediaType.APPLICATION_JSON } )
    @Path( "/all" )
    Response getSystemUsers();

    @GET
    @Path( "/user" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response getActiveUser();

    @GET
    @Path( "/key-data/{userId}" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response getPublicKeyData( @PathParam( "userId" ) Long userId );


    @GET
    @Path( "/check-user-key/{userId}" )
    @Produces( { MediaType.TEXT_PLAIN } )
    Response checkUserKey( @PathParam( "userId" ) Long userId );


    @POST
    @Produces( { MediaType.APPLICATION_JSON } )
    Response saveUser( @FormParam( "username" ) String username, @FormParam( "full_name" ) String fullName,
                       @FormParam( "password" ) String password, @FormParam( "email" ) String email,
                       @FormParam( "roles" ) String roles, @FormParam( "user_id" ) Long userId,
                       @FormParam( "trustLevel" ) String trustLevel );

    @POST
    @Path( "/new-password" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response changePassword( @FormParam( "old" ) String oldPass, @FormParam( "new" ) String newPass );


    @POST
    @Path( "/approve-delegate" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response approveDelegatedUser( @FormParam( "signedDocument" ) String trustMessage );

    @POST
    @Path( "/set-public-key" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response setUserPublicKey( @FormParam( "publicKey" ) String publicKey );

    @POST
    @Path( "/delegate-identity" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response createIdentityDelegationDocument();

    @GET
    @Path( "/delegate-identity" )
    @Produces( { MediaType.TEXT_PLAIN } )
    Response getIdentityDelegationDocument();


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
    Response saveRole( @FormParam( "rolename" ) String rolename, @FormParam( "permission" ) String permissionJson,
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
    Response createUserToken( @FormParam( "userId" ) Long userId, @FormParam( "token" ) String token,
                              @FormParam( "period" ) Integer period );

    @PUT
    @Path( "users/tokens" )
    Response updateUserToken( @FormParam( "userId" ) Long userId, @FormParam( "token" ) String token,
                              @FormParam( "newToken" ) String newToken, @FormParam( "period" ) Integer period );


    @DELETE
    @Path( "users/tokens/{tokenId}" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response removeUserToken( @PathParam( "tokenId" ) String tokenId );


    @GET
    @Path( "tokens/types" )
    @Produces( { MediaType.APPLICATION_JSON } )
    Response getTokenTypes();


    @GET
    @Path( "/is-tenant-manager" )
    @Produces( { MediaType.TEXT_PLAIN } )
    Response isTenantManager();

    @GET
    @Path( "/is-admin" )
    @Produces( { MediaType.TEXT_PLAIN } )
    Response isAdmin();

    @GET
    @Path( "/has-environments/{userId}" )
    @Produces( { MediaType.TEXT_PLAIN } )
    Response hasEnvironments( @PathParam( "userId" ) Long userId );
}
