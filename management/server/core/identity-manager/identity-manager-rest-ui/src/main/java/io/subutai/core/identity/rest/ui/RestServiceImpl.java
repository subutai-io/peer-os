package io.subutai.core.identity.rest.ui;


import java.io.IOException;
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

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.gson.reflect.TypeToken;

import io.subutai.common.security.crypto.pgp.PGPEncryptionUtil;
import io.subutai.common.security.crypto.pgp.PGPKeyUtil;
import io.subutai.common.security.objects.PermissionScope;
import io.subutai.common.security.objects.TokenType;
import io.subutai.common.security.objects.UserType;
import io.subutai.common.settings.Common;
import io.subutai.common.util.JsonUtil;
import io.subutai.common.util.ServiceLocator;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.model.Role;
import io.subutai.core.identity.api.model.User;
import io.subutai.core.identity.api.model.UserDelegate;
import io.subutai.core.identity.rest.ui.entity.KeyDataDto;
import io.subutai.core.identity.rest.ui.entity.PermissionDto;
import io.subutai.core.security.api.SecurityManager;
import io.subutai.core.security.api.model.SecurityKey;
import io.subutai.core.template.api.TemplateManager;


public class RestServiceImpl implements RestService
{
    private static final Logger LOGGER = LoggerFactory.getLogger( RestServiceImpl.class );
    private SecurityManager securityManager = null;
    private JsonUtil jsonUtil = new JsonUtil();
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

            for ( Iterator iter = pubRing.getPublicKey().getUserIDs(); iter.hasNext(); )
            {
                String id = ( String ) iter.next();

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
            User user;

            if ( userId == null || userId <= 0 )
            {


                if ( username.toLowerCase().indexOf( IdentityManager.SYSTEM_USERNAME ) == 0
                        || username.toLowerCase().indexOf( IdentityManager.ADMIN_USERNAME ) == 0 )
                {
                    LOGGER.warn( "#saveUser forbidden, username is reserved" );
                    return Response.status( Response.Status.INTERNAL_SERVER_ERROR )
                                   .entity( JsonUtil.toJson( "User name is reserved by the system." ) ).build();
                }

                user = identityManager.createUser( username, password, fullName, email, UserType.REGULAR.getId(),
                        Integer.parseInt( trustLevel ), false, true );

                if ( !Strings.isNullOrEmpty( rolesJson ) )
                {
                    List<Long> roleIds = jsonUtil.from( rolesJson, new TypeToken<ArrayList<Long>>()
                    {
                    }.getType() );

                    roleIds.forEach( r -> identityManager.assignUserRole( user, identityManager.getRole( r ) ) );
                }

                //add env mgr role by default
                identityManager
                        .assignUserRole( user, identityManager.findRoleByName( IdentityManager.ENV_MANAGER_ROLE ) );
            }
            else
            {
                user = identityManager.getUser( userId );
                user.setEmail( email );
                user.setFullName( fullName );
                user.setTrustLevel( Integer.parseInt( trustLevel ) );

                List<Long> roleIds = jsonUtil.from( rolesJson, new TypeToken<ArrayList<Long>>()
                {
                }.getType() );

                //add env mgr role by default
                roleIds.add( identityManager.findRoleByName( IdentityManager.ENV_MANAGER_ROLE ).getId() );

                user.setRoles(
                        roleIds.stream().map( r -> identityManager.getRole( r ) ).collect( Collectors.toList() ) );

                identityManager.modifyUser( user, password );
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

            for ( Role role : roles )
            {
                LOGGER.debug( "ROLE: " + role.getName() );
            }

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


    @Override
    public Response hasEnvironments( Long userId )
    {
        EnvironmentManager environmentManager = ServiceLocator.lookup( EnvironmentManager.class );

        boolean hasLocalEnvironments = !environmentManager.getEnvironmentsByOwnerId( userId ).isEmpty();

        return Response.status( Response.Status.OK ).entity( hasLocalEnvironments ).build();
    }

    //****** Kurjun ***********/
    //TODO extract Kurjun related functionality into separate KurjunClient


    private String getFingerprint()
    {
        User user = identityManager.getActiveUser();
        return user.getFingerprint().toLowerCase();
    }


    @Override
    public Response getKurjunAuthId()
    {
        CloseableHttpClient client = getHttpsClient();
        try
        {
            if ( isRegisteredWithGorjun() )
            {
                HttpGet httpGet = new HttpGet(
                        String.format( "%s/auth/token?user=%s", Common.LOCAL_KURJUN_BASE_URL, getFingerprint() ) );
                CloseableHttpResponse response = client.execute( httpGet );
                HttpEntity entity = response.getEntity();
                try
                {
                    String authId = IOUtils.toString( entity.getContent() );
                    EntityUtils.consume( entity );
                    return Response.ok( authId ).build();
                }
                finally
                {
                    IOUtils.closeQuietly( response );
                }
            }
            else
            {
                return Response.status( Response.Status.NOT_FOUND ).build();
            }
        }
        catch ( Exception e )
        {
            return Response.serverError().entity( e.getMessage() ).build();
        }
        finally
        {
            IOUtils.closeQuietly( client );
        }
    }


    @Override
    public Response submitSignedTemplateHash( final String signedTemplateHash )
    {
        CloseableHttpClient client = getHttpsClient();
        try
        {

            HttpPost post = new HttpPost( String.format( "%s/auth/sign", Common.LOCAL_KURJUN_BASE_URL ) );

            MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
            entityBuilder.setMode( HttpMultipartMode.BROWSER_COMPATIBLE );
            entityBuilder.addTextBody( "signature", signedTemplateHash );
            entityBuilder.addTextBody( "token", identityManager.getActiveSession().getKurjunToken() );
            HttpEntity httpEntity = entityBuilder.build();
            post.setEntity( httpEntity );
            CloseableHttpResponse response = client.execute( post );

            try
            {
                if ( response.getStatusLine().getStatusCode() != 200 )
                {
                    HttpEntity entity = response.getEntity();
                    String errMsg = IOUtils.toString( entity.getContent() );
                    EntityUtils.consume( entity );

                    return Response.serverError().entity(
                            "Http code: " + response.getStatusLine().getStatusCode() + " Msg: " + errMsg ).build();
                }
                else
                {
                    return Response.ok().build();
                }
            }
            finally
            {
                IOUtils.closeQuietly( response );
            }
        }
        catch ( Exception e )
        {
            LOGGER.error( e.getMessage() );

            return Response.serverError().entity( e.getMessage() ).build();
        }
        finally
        {
            IOUtils.closeQuietly( client );
        }
    }


    @Override
    public Response obtainKurjunToken( final String signedAuthId )
    {

        CloseableHttpClient client = getHttpsClient();
        try
        {

            HttpPost post = new HttpPost( String.format( "%s/auth/token", Common.LOCAL_KURJUN_BASE_URL ) );

            MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
            entityBuilder.setMode( HttpMultipartMode.BROWSER_COMPATIBLE );
            entityBuilder.addTextBody( "user", getFingerprint() );
            entityBuilder.addTextBody( "message", signedAuthId );
            HttpEntity httpEntity = entityBuilder.build();
            post.setEntity( httpEntity );
            CloseableHttpResponse response = client.execute( post );

            try
            {
                HttpEntity entity = response.getEntity();
                String content = IOUtils.toString( entity.getContent() );
                EntityUtils.consume( entity );

                if ( response.getStatusLine().getStatusCode() == 200 )
                {
                    identityManager.getActiveSession().setKurjunToken( content );

                    TemplateManager templateManager = ServiceLocator.getServiceOrNull( TemplateManager.class );
                    if ( templateManager != null )
                    {
                        templateManager.resetTemplateCache();
                    }

                    return Response.ok( content ).build();
                }
                else
                {
                    return Response.serverError().entity(
                            "Http code: " + response.getStatusLine().getStatusCode() + " Msg: " + content ).build();
                }
            }
            finally
            {
                IOUtils.closeQuietly( response );
            }
        }
        catch ( Exception e )
        {
            LOGGER.error( e.getMessage() );

            return Response.serverError().entity( e.getMessage() ).build();
        }
        finally
        {
            IOUtils.closeQuietly( client );
        }
    }


    @Override
    public Response getObtainedKurjunToken()
    {
        String token = null;

        if ( identityManager.getActiveSession() != null )
        {
            token = identityManager.getActiveSession().getKurjunToken();
        }

        return Response.status( Response.Status.OK ).entity( token ).build();
    }


    @Override
    public Response isRegisteredWithKurjun()
    {
        try
        {
            return Response.ok( isRegisteredWithGorjun() ).build();
        }
        catch ( Exception e )
        {
            return Response.serverError().entity( e.getMessage() ).build();
        }
    }


    private boolean isRegisteredWithGorjun() throws IOException
    {
        CloseableHttpClient client = getHttpsClient();
        try
        {
            HttpGet httpGet = new HttpGet(
                    String.format( "%s/auth/key?user=%s", Common.LOCAL_KURJUN_BASE_URL, getFingerprint() ) );
            CloseableHttpResponse response = client.execute( httpGet );
            try
            {
                return response.getStatusLine().getStatusCode() == 200;
            }
            finally
            {
                IOUtils.closeQuietly( response );
            }
        }

        finally
        {
            IOUtils.closeQuietly( client );
        }
    }


    private CloseableHttpClient getHttpsClient()
    {
        try
        {
            RequestConfig config = RequestConfig.custom().setSocketTimeout( 5000 ).setConnectTimeout( 5000 ).build();

            SSLContextBuilder sslContextBuilder = new SSLContextBuilder();
            sslContextBuilder.loadTrustMaterial( null, ( TrustStrategy ) ( x509Certificates, s ) -> true );
            SSLConnectionSocketFactory sslSocketFactory =
                    new SSLConnectionSocketFactory( sslContextBuilder.build(), NoopHostnameVerifier.INSTANCE );

            return HttpClients.custom().setDefaultRequestConfig( config ).setSSLSocketFactory( sslSocketFactory )
                              .build();
        }
        catch ( Exception e )
        {
            LOGGER.error( e.getMessage() );
        }

        return HttpClients.createDefault();
    }
}
