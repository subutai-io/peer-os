package io.subutai.core.hubmanager.impl.processor;


import java.io.IOException;

import javax.ws.rs.core.Response;

import org.bouncycastle.openpgp.PGPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.http.HttpStatus;

import io.subutai.common.security.objects.KeyTrustLevel;
import io.subutai.common.security.objects.UserType;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.hubmanager.api.dao.ConfigDataService;
import io.subutai.core.hubmanager.api.model.Config;
import io.subutai.core.hubmanager.impl.ConfigManager;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.model.Role;
import io.subutai.core.identity.api.model.User;
import io.subutai.hub.share.dto.UserDto;
import io.subutai.hub.share.dto.environment.EnvironmentPeerDto;
import io.subutai.hub.share.json.JsonUtil;


// TODO: Replace WebClient with HubRestClient.
public class EnvironmentUserHelper
{
    private final Logger log = LoggerFactory.getLogger( getClass() );

    private final ConfigManager configManager;

    private final IdentityManager identityManager;

    private final ConfigDataService configDataService;

    private final EnvironmentManager environmentManager;


    public EnvironmentUserHelper( ConfigManager configManager, IdentityManager identityManager,
                                  ConfigDataService configDataService, EnvironmentManager environmentManager )
    {
        this.configManager = configManager;

        this.identityManager = identityManager;

        this.configDataService = configDataService;

        this.environmentManager = environmentManager;
    }


    public void handleEnvironmentOwnerDeletion( EnvironmentPeerDto peerDto )
    {
        String envOwnerId = peerDto.getOwnerId();

        User user = getUserByHubId( envOwnerId );

        if ( user == null )
        {
            return;
        }

        log.debug( "Deleting environment owner: id={}, name={}, email={}", user.getId(), user.getUserName(),
                user.getEmail() );

        boolean hasLocalEnvironments = environmentManager.getEnvironmentsByOwnerId( user.getId() ).size() > 0;

        if ( hasLocalEnvironments )
        {
            log.debug( "Can't delete user b/c user has local environment" );

            return;
        }

        if ( hasUserEnvironmentsForPeerOnHub( envOwnerId ) )
        {
            log.debug( "Can't delete user b/c user has Hub environment on this peer" );

            return;
        }

        identityManager.removeUser( user.getId() );

        log.debug( "User deleted" );
    }


    private boolean hasUserEnvironmentsForPeerOnHub( String userId )
    {
        String urlFormat = "/rest/v1/adapter/users/%s/environments";

        String url = String.format( urlFormat, userId );

        String json = null;

        try
        {
            WebClient client = configManager.getTrustedWebClientWithAuth( url, configManager.getHubIp() );

            Response res = client.get();

            json = handleResponse( res, String.class );
        }
        catch ( Exception e )
        {
            log.error( "Error to get user environments from hub: ", e );
        }

        return json != null && json.contains( "id" );
    }


    public User handleEnvironmentOwnerCreation( EnvironmentPeerDto peerDto )
    {
        String envOwnerId = peerDto.getOwnerId();

        Config config = configDataService.getHubConfig( configManager.getPeerId() );

        if ( envOwnerId.equals( config.getOwnerId() ) || getUserByHubId( envOwnerId ) != null )
        {
            log.debug( "No need to create new user for environment" );

            return identityManager.getActiveUser();
        }

        UserDto userDto = getUserDataFromHub( envOwnerId );

        return createNewUser( userDto );
    }


    private User getUserByHubId( String userId )
    {
        for ( User user : identityManager.getAllUsers() )
        {
            // Email contains the user id in Hub
            if ( user.getEmail().startsWith( userId ) )
            {
                return user;
            }
        }

        return null;
    }


    private Role getRole( String roleName )
    {
        for ( Role role : identityManager.getAllRoles() )
        {
            if ( role.getName().equals( roleName ) )
            {
                return role;
            }
        }

        return null;
    }


    //    public void test()
    //    {
    //        UserDto dto = getUserDataFromHub( "43163772-a8c2-459f-bfcb-4d0bcc5759f6" );
    //
    //        createNewUser( dto );
    //    }


    private User createNewUser( UserDto userDto )
    {
        log.debug( "Creating new user: {}", userDto.getEmail() );

        // Trick to get later the user id in Hub
        String email = userDto.getId() + "@hub.subut.ai";
        try
        {
            User user = identityManager.createUser( userDto.getFingerprint(), null, "[Hub] " + userDto.getName(), email,
                    UserType.Regular.getId(), KeyTrustLevel.Marginal.getId(), false, true );

            identityManager.setUserPublicKey( user.getId(), userDto.getPublicKey() );
            identityManager.assignUserRole( user, getRole( "Environment-Manager" ) );
            identityManager.assignUserRole( user, getRole( "Template-Management" ) );

            log.debug( "User created successfully" );
            return user;
        }
        catch ( Exception e )
        {
            log.error( "Error to create user: ", e );
        }
        return null;
    }


    private UserDto getUserDataFromHub( String userId )
    {
        String path = "/rest/v1/users/" + userId;

        UserDto userDto = null;

        try
        {
            WebClient client = configManager.getTrustedWebClientWithAuth( path, configManager.getHubIp() );

            Response res = client.get();

            userDto = handleResponse( res, UserDto.class );
        }
        catch ( Exception e )
        {
            log.error( "Error to get user data: ", e );
        }

        return userDto;
    }


    private <T> T handleResponse( Response response, Class<T> clazz ) throws IOException, PGPException
    {
        if ( response.getStatus() != HttpStatus.SC_OK && response.getStatus() != HttpStatus.SC_NO_CONTENT )
        {
            String content = response.readEntity( String.class );

            log.error( "HTTP {}: {}", response.getStatus(), StringUtils.abbreviate( content, 250 ) );

            return null;
        }

        if ( response.getStatus() == HttpStatus.SC_NO_CONTENT )
        {
            return null;
        }

        byte[] encryptedContent = configManager.readContent( response );

        byte[] plainContent = configManager.getMessenger().consume( encryptedContent );

        return JsonUtil.fromCbor( plainContent, clazz );
    }
}