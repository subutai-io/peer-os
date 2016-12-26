package io.subutai.core.identity.rest.ui;


import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.ws.rs.core.Response;

import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.gson.reflect.TypeToken;

import io.subutai.common.security.crypto.pgp.PGPEncryptionUtil;
import io.subutai.common.security.crypto.pgp.PGPKeyUtil;
import io.subutai.common.security.objects.PermissionScope;
import io.subutai.common.security.objects.TokenType;
import io.subutai.common.security.objects.UserType;
import io.subutai.common.util.JsonUtil;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.model.Role;
import io.subutai.core.identity.api.model.User;
import io.subutai.core.identity.api.model.UserDelegate;
import io.subutai.core.identity.rest.ui.entity.KeyDataDto;
import io.subutai.core.identity.rest.ui.entity.PermissionDto;
import io.subutai.core.security.api.SecurityManager;
import io.subutai.core.security.api.model.SecurityKey;


public class RestServiceImpl implements RestService
{
    private static final Logger LOGGER = LoggerFactory.getLogger( RestServiceImpl.class );
    private SecurityManager securityManager = null;
    protected JsonUtil jsonUtil = new JsonUtil();
    private IdentityManager identityManager;


    public RestServiceImpl( final IdentityManager identityManager, final SecurityManager securityManager )
    {
        this.identityManager = identityManager;
        this.securityManager = securityManager;
    }


    /** Users ********************************************** */

    @Override
    public Response getUsers()
    {
        try
        {
            List<User> users = identityManager.getAllUsers();

            return Response.ok( jsonUtil.to( users.stream().filter(
                    user -> user.getType() != UserType.SYSTEM.getId() && !IdentityManager.ADMIN_USERNAME
                            .equals( user.getUserName() ) ).collect( Collectors.toList() ) ) ).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error getting users #getUsers", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( JsonUtil.toJson( e.toString() ) )
                           .build();
        }
    }


    @Override
    public Response getSystemUsers()
    {
        try
        {
            return Response.ok( jsonUtil.to( identityManager.getAllUsers() ) ).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error getting users #getUsers", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( JsonUtil.toJson( e.toString() ) )
                           .build();
        }
    }


    @Override
    public Response getActiveUser()
    {
        User activeUser = identityManager.getActiveUser();

        try
        {
            return Response.ok( jsonUtil.to( activeUser ) ).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error getting activeUser user #getActiveUser", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( JsonUtil.toJson( e.toString() ) )
                           .build();
        }
    }


    @Override
    public Response getPublicKeyData( final Long userId )
    {
        User user = identityManager.getUser( userId );
        KeyDataDto keyData = new KeyDataDto();

        try
        {
            PGPPublicKeyRing pubRing = securityManager.getKeyManager().getPublicKeyRing( user.getSecurityKeyId() );

            keyData.setFingerprint( PGPKeyUtil.getFingerprint( pubRing.getPublicKey().getFingerprint() ) );
            keyData.setKey( PGPEncryptionUtil.armorByteArrayToString( pubRing.getEncoded() ) );
            keyData.setAuthId( user.getAuthId() );

            for ( Iterator<String> iter = pubRing.getPublicKey().getUserIDs(); iter.hasNext(); )
            {
                String id = iter.next();

                if ( !Strings.isNullOrEmpty( id ) )
                {
                    keyData.setUserId( keyData.getUserId() + ":" + id );
                }
            }

            return Response.ok( jsonUtil.to( keyData ) ).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error getting Public Key Data #getPublicKeyData", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( JsonUtil.toJson( e.toString() ) )
                           .build();
        }
    }


    @Override
    public Response checkUserKey( final Long userId )
    {
        User user = identityManager.getUser( userId );

        try
        {
            int status = 0;

            SecurityKey keyData = securityManager.getKeyManager().getKeyData( user.getSecurityKeyId() );

            if ( keyData != null )
            {
                String pFprint = keyData.getPublicKeyFingerprint();
                String sFprint = keyData.getSecretKeyFingerprint();

                if ( pFprint.equals( sFprint ) )
                {
                    status = 1;
                }
                else
                {
                    status = 2;
                }
            }

            return Response.ok( jsonUtil.to( status ) ).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error getting Public Key Data #getPublicKeyData", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response saveUser( final String username, final String fullName, final String password, final String email,
                              final String rolesJson, final Long userId, final String trustLevel )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( username ), "username is missing" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( fullName ), "fullname is missing" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( email ), "email must be set" );

        try
        {
            User newUser;

            if ( userId == null || userId <= 0 )
            {


                if ( username.toLowerCase().indexOf( IdentityManager.SYSTEM_USERNAME ) == 0
                        || username.toLowerCase().indexOf( IdentityManager.ADMIN_USERNAME ) == 0 )
                {
                    LOGGER.warn( "#saveUser forbidden, username is reserved" );
                    return Response.status( Response.Status.INTERNAL_SERVER_ERROR )
                                   .entity( JsonUtil.toJson( "User name is reserved by the system." ) ).build();
                }

                newUser = identityManager.createUser( username, password, fullName, email, UserType.REGULAR.getId(),
                        Integer.parseInt( trustLevel ), false, true );

                if ( !Strings.isNullOrEmpty( rolesJson ) )
                {
                    List<Long> roleIds = jsonUtil.from( rolesJson, new TypeToken<ArrayList<Long>>()
                    {
                    }.getType() );


                    roleIds.stream()
                           .forEach( r -> identityManager.assignUserRole( newUser, identityManager.getRole( r ) ) );
                }
            }
            else
            {
                newUser = identityManager.getUser( userId );
                newUser.setEmail( email );
                newUser.setFullName( fullName );
                newUser.setTrustLevel( Integer.parseInt( trustLevel ) );

                List<Long> roleIds = jsonUtil.from( rolesJson, new TypeToken<ArrayList<Long>>()
                {
                }.getType() );

                newUser.setRoles(
                        roleIds.stream().map( r -> identityManager.getRole( r ) ).collect( Collectors.toList() ) );

                identityManager.modifyUser( newUser, password );
            }
        }
        catch ( Exception e )
        {
            if ( e.getClass() == AccessControlException.class )
            {
                LOGGER.error( "Error setting new user #setUser", e );
                return Response.status( Response.Status.INTERNAL_SERVER_ERROR )
                               .entity( JsonUtil.toJson( "You don't have permission to perform this operation" ) )
                               .build();
            }
            else
            {
                LOGGER.error( "Error setting new user #setUser", e );
                return Response.status( Response.Status.INTERNAL_SERVER_ERROR )
                               .entity( JsonUtil.toJson( e.toString() ) ).build();
            }
        }

        return Response.ok().build();
    }


    @Override
    public Response changePassword( final String oldPass, final String newPass )
    {
        try
        {
            identityManager.changeUserPassword( identityManager.getActiveUser().getId(), oldPass, newPass );
        }
        catch ( Exception e )
        {
            return Response.serverError().entity( JsonUtil.toJson( e.toString() ) ).build();
        }


        return Response.ok().build();
    }


    @Override
    public Response approveDelegatedUser( final String trustMessage )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( trustMessage ), "message is missing" );

        try
        {
            identityManager.approveDelegatedUser( trustMessage );
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error setting new user #setUser", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( JsonUtil.toJson( e.toString() ) )
                           .build();
        }

        return Response.ok().build();
    }


    @Override
    public Response createIdentityDelegationDocument()
    {
        try
        {
            identityManager.createIdentityDelegationDocument();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error setting new user #setUser", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( JsonUtil.toJson( e.toString() ) )
                           .build();
        }

        return Response.ok().build();
    }


    @Override
    public Response getIdentityDelegationDocument()
    {
        try
        {
            User activeUser = identityManager.getActiveUser();
            UserDelegate userDelegate = identityManager.getUserDelegate( activeUser.getId() );
            return Response.ok( userDelegate.getRelationDocument() ).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error setting new user #setUser", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response setUserPublicKey( String publicKey )
    {
        try
        {
            identityManager.setUserPublicKey( identityManager.getActiveUser().getId(), publicKey );
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error updating user public key", e );
            return Response.serverError().build();
        }
        return Response.ok().build();
    }


    @Override
    public Response deleteUser( final Long userId )
    {
        try
        {
            identityManager.removeUser( userId );
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error deleting user #deleteUser", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( JsonUtil.toJson( e.toString() ) )
                           .build();
        }

        return Response.ok().build();
    }


    /** Roles ********************************************** */

    @Override
    public Response getRoles()
    {
        try
        {
            List<Role> roles = identityManager.getAllRoles();

            return Response.ok( jsonUtil.to( roles.stream().filter( role -> role.getType() != UserType.SYSTEM.getId() )
                                                  .collect( Collectors.toList() ) ) ).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error getting roles #getRoles", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( JsonUtil.toJson( e.toString() ) )
                           .build();
        }
    }


    @Override
    public Response saveRole( final String rolename, final String permissionJson, final Long roleId )
    {
        try
        {
            Preconditions.checkArgument( !Strings.isNullOrEmpty( rolename ) );

            Role role;

            if ( roleId == null || roleId <= 0 )
            {
                role = identityManager.createRole( rolename, UserType.REGULAR.getId() );
            }
            else
            {
                role = identityManager.getRole( roleId );
            }

            if ( !Strings.isNullOrEmpty( permissionJson ) )
            {
                ArrayList<PermissionDto> permissions =
                        JsonUtil.fromJson( permissionJson, new TypeToken<ArrayList<PermissionDto>>()
                        {
                        }.getType() );


                if ( !Strings.isNullOrEmpty( rolename ) )
                {
                    role.setName( rolename );
                }

                role.setPermissions( permissions.stream().map( p -> identityManager
                        .createPermission( p.getObject(), p.getScope(), p.getRead(), p.getWrite(), p.getUpdate(),
                                p.getDelete() ) ).collect( Collectors.toList() ) );
            }

            identityManager.updateRole( role );
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error setting new role #createRole", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( JsonUtil.toJson( e.toString() ) )
                           .build();
        }

        return Response.ok().build();
    }


    @Override
    public Response deleteRole( final Long roleId )
    {
        try
        {
            identityManager.removeRole( roleId );
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error deleting role #deleteRole", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( JsonUtil.toJson( e.toString() ) )
                           .build();
        }

        return Response.ok().build();
    }


    /** Permissions ********************************************** */

    @Override
    public Response getPermissions()
    {
        try
        {
            return Response.ok( jsonUtil.to( identityManager.getAllPermissions() ) ).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error receiving permissions", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( JsonUtil.toJson( e.toString() ) )
                           .build();
        }
    }


    @Override
    public Response getPermissionScopes()
    {
        Map<Integer, String> map = Maps.newHashMap();
        for ( PermissionScope permissionScope : PermissionScope.values() )
        {
            map.put( permissionScope.getId(), permissionScope.getName() );
        }
        return Response.ok( JsonUtil.toJson( map ) ).build();
    }


    /** Tokens ********************************************** */

    @Override
    public Response getAllUserTokens()
    {
        try
        {
            /*
            List<UserTokenDto> list = identityManager.getAllUserTokens().stream()
                                                      .map( p -> new UserTokenDto( p.getUserId(),
                                                              identityManager.getUser( p.getUserId() ).getUserName(),
                                                              p.getTokenId(), p.getFullToken(), p.getType(),
                                                              p.getHashAlgorithm(), p.getIssuer(), p.getValidDate() ) )
                                                      .collect( Collectors.toList() );

            return Response.ok( JsonUtil.toJson( list ) ).build();
            */
            return Response.ok( "Tokens" ).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error receiving user tokens", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( JsonUtil.toJson( e.toString() ) )
                           .build();
        }
    }


    @Override
    public Response createUserToken( final Long userId, final String token, final Integer period )
    {
        try
        {
            Preconditions.checkNotNull( userId, "Invalid userId" );
            Preconditions.checkArgument( !Strings.isNullOrEmpty( token ), "Invalid token" );
            Preconditions.checkNotNull( period, "Invalid period" );

            Date newDate = new Date();
            java.util.Calendar cal = Calendar.getInstance();
            cal.setTime( newDate );
            cal.add( Calendar.HOUR_OF_DAY, period );

            //identityManager
            //      .createUserToken( identityManager.getUser( userId ), token, null, "subutai.io", 2, cal.getTime() );
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error creating new user token", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( JsonUtil.toJson( e.toString() ) )
                           .build();
        }

        return Response.ok().build();
    }


    @Override
    public Response updateUserToken( final Long userId, final String token, final String newToken,
                                     final Integer period )
    {
        try
        {
            Preconditions.checkNotNull( userId, "Invalid userId" );
            Preconditions.checkArgument( !Strings.isNullOrEmpty( token ), "Invalid token id to be replaced" );
            Preconditions.checkArgument( !Strings.isNullOrEmpty( newToken ), "Invalid newToken" );
            Preconditions.checkNotNull( period, "Invalid period" );
            /*

            Date newDate = new Date();
            java.util.Calendar cal = Calendar.getInstance();
            cal.setTime( newDate );
            cal.add( Calendar.HOUR_OF_DAY, period );

            identityManager.updateUserToken( token, identityManager.getUser( userId ), newToken, null, "issuer", 1,
                    cal.getTime() );
                    */
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error updating user token", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( JsonUtil.toJson( e.toString() ) )
                           .build();
        }

        return Response.ok().build();
    }


    @Override
    public Response removeUserToken( final String tokenId )
    {
        try
        {
            //Preconditions.checkArgument( !Strings.isNullOrEmpty( tokenId ), "Invalid tokenId" );

            //identityManager.removeUserToken( tokenId );
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error updating new user token", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( JsonUtil.toJson( e.toString() ) )
                           .build();
        }


        return Response.ok().build();
    }


    @Override
    public Response getTokenTypes()
    {
        Map<Integer, String> map = Maps.newHashMap();
        for ( TokenType tokenType : TokenType.values() )
        {
            map.put( tokenType.getId(), tokenType.getName() );
        }

        return Response.ok( JsonUtil.toJson( map ) ).build();
    }


    @Override
    public Response isTenantManager()
    {
        return Response.status( Response.Status.OK ).entity( identityManager.isTenantManager() ).build();
    }


    @Override
    public Response isAdmin()
    {
        return Response.status( Response.Status.OK ).entity( identityManager.isAdmin() ).build();
    }
}
