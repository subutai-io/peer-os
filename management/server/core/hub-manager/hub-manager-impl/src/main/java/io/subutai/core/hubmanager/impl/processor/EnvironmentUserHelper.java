package io.subutai.core.hubmanager.impl.processor;


import java.io.IOException;

import io.subutai.common.security.objects.TokenType;
import org.bouncycastle.openpgp.PGPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.http.HttpStatus;

import io.subutai.common.security.objects.KeyTrustLevel;
import io.subutai.common.security.objects.UserType;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.hubmanager.api.HubManager;
import io.subutai.core.hubmanager.api.RestResult;
import io.subutai.core.hubmanager.api.dao.ConfigDataService;
import io.subutai.core.hubmanager.api.exception.HubManagerException;
import io.subutai.core.hubmanager.api.model.Config;
import io.subutai.core.hubmanager.impl.http.HubRestClient;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.model.Role;
import io.subutai.core.identity.api.model.User;
import io.subutai.core.identity.api.model.UserToken;
import io.subutai.hub.share.dto.UserDto;
import io.subutai.hub.share.dto.UserTokenDto;
import io.subutai.hub.share.dto.environment.EnvironmentPeerDto;


public class EnvironmentUserHelper
{
    private final String baseHubTokenUrl = "/rest/v1/users/%s/token";

    private final Logger log = LoggerFactory.getLogger( getClass() );

    private final IdentityManager identityManager;

    private final ConfigDataService configDataService;

    private final EnvironmentManager environmentManager;

    private final HubRestClient restClient;


    public EnvironmentUserHelper( IdentityManager identityManager, ConfigDataService configDataService,
                                  EnvironmentManager environmentManager, HubRestClient restClient )
    {
        this.identityManager = identityManager;

        this.configDataService = configDataService;

        this.environmentManager = environmentManager;

        this.restClient = restClient;
    }


    public void handleEnvironmentOwnerDeletion( EnvironmentPeerDto peerDto )
    {
        String envOwnerId = peerDto.getEnvironmentInfo().getOwnerId();

        User user = getUserByHubId( envOwnerId );

        if ( user == null )
        {
            return;
        }

        log.debug( "Deleting environment owner: id={}, name={}, email={}", user.getId(), user.getUserName(),
                user.getEmail() );

        boolean hasLocalEnvironments = !environmentManager.getEnvironmentsByOwnerId( user.getId() ).isEmpty();

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
        deleteUserToken( user.getId() );

        log.debug( "User deleted" );
    }


    private boolean hasUserEnvironmentsForPeerOnHub( String userId )
    {
        String path = String.format( "/rest/v1/adapter/users/%s/environments", userId );

        RestResult<String> restResult = restClient.get( path, String.class );

        if ( !restResult.isSuccess() )
        {
            log.error( "Error to get user environments from Hub: " + restResult.getError() );
        }

        String json = restResult.getEntity();

        return json != null && json.contains( "id" );
    }


    public User handleEnvironmentOwnerCreation( EnvironmentPeerDto peerDto )
    {
        String envOwnerId = peerDto.getEnvironmentInfo().getOwnerId();

        Config config = configDataService.getHubConfig( peerDto.getPeerId() );

        User user = getUserByHubId( envOwnerId );

        if ( user != null )
        {
            log.info( "User already exists: username={}, email={}", user.getUserName(), user.getEmail() );

            return user;
        }
        else if ( envOwnerId.equals( config.getOwnerId() ) )
        {
            log.info( "No need to create a user: peer owner is environment owner. For example, admin." );

            return identityManager.getActiveUser();
        }

        UserDto userDto = getUserDataFromHub( envOwnerId );

        return createUser( userDto );
    }


    private User getUserByHubId( String userId )
    {
        for ( User user : identityManager.getAllUsers() )
        {
            String email = userId + HubManager.HUB_EMAIL_SUFFIX;
            if ( user.getEmail().equals( email ) && user.isHubUser() )
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
            if ( role.getName().equalsIgnoreCase( roleName ) )
            {
                return role;
            }
        }

        return null;
    }


    private User createUser( UserDto userDto )
    {
        log.info( "Creating new user: {}", userDto.getEmail() );

        // Trick to get later the user id in Hub
        String email = userDto.getId() + HubManager.HUB_EMAIL_SUFFIX;

        try
        {
            User user = identityManager.createUser( userDto.getFingerprint(), null, "[Hub] " + userDto.getName(), email,
                    UserType.HUB.getId(), KeyTrustLevel.MARGINAL.getId(), false, true );

            identityManager.setUserPublicKey( user.getId(), userDto.getPublicKey() );
            identityManager.assignUserRole( user, getRole( IdentityManager.ENV_MANAGER_ROLE ) );
            identityManager.assignUserRole( user, getRole( IdentityManager.TEMPLATE_MANAGER_ROLE ) );

            log.info( "User created successfully" );

            return user;
        }
        catch ( Exception e )
        {
            log.error( "Error to create user: ", e );
        }

        return null;
    }


    public UserDto getUserDataFromHub( String userId )
    {
        String path = "/rest/v1/users/" + userId;

        RestResult<UserDto> restResult = restClient.get( path, UserDto.class );

        if ( !restResult.isSuccess() )
        {
            log.error( "Error to get user data from Hub: " + restResult.getError() );
        }

        return restResult.getEntity();
    }


    public UserToken getUserTokenFromHub( Long ssUserId ) throws HubManagerException, PGPException, IOException
    {
        String url = String.format( baseHubTokenUrl, ssUserId );
        RestResult<UserTokenDto> res = restClient.get( url, UserTokenDto.class );

        if ( res.getStatus() != HttpStatus.SC_OK && res.getStatus() != 204 )
        {
            throw new HubManagerException( "Error to get user token form Hub: HTTP " + res.getStatus() );
        }

        UserTokenDto userTokenDto = res.getEntity();

        if ( userTokenDto == null )
        {
            return null;
        }

        try
        {
            User user = identityManager.authenticateByToken( userTokenDto.getToken() );
            return identityManager.getUserToken( user.getId() );
        }
        catch ( Exception exception )
        {
            return updateUserTokenInHub( userTokenDto );
        }
    }


    public UserToken updateUserTokenInHub( UserTokenDto userTokenDto )
    {
        String url = String.format( baseHubTokenUrl, userTokenDto.getOwnerId() );

        User user = identityManager.getUser( userTokenDto.getSsUserId() );
        UserToken userToken = identityManager.updateTokenAndSession( user.getId() );

        //set new token and valid date
        userTokenDto.setToken( userToken.getFullToken() );
        userTokenDto.setValidDate( userToken.getValidDate() );
        userTokenDto.setState( UserTokenDto.State.READY );

        restClient.post( url, userTokenDto );
        return userToken;
    }

    public void deleteUserToken ( Long userId )
    {
        String url = String.format( "/rest/v1/users/%s/token/delete", userId );
        restClient.post( url, userId );
    }
}