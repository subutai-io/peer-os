package io.subutai.core.bazaarmanager.impl.util;


import java.io.IOException;

import org.bouncycastle.openpgp.PGPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.http.HttpStatus;

import io.subutai.common.security.objects.KeyTrustLevel;
import io.subutai.common.security.objects.UserType;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.bazaarmanager.api.BazaarManager;
import io.subutai.core.bazaarmanager.api.RestClient;
import io.subutai.core.bazaarmanager.api.RestResult;
import io.subutai.core.bazaarmanager.api.dao.ConfigDataService;
import io.subutai.core.bazaarmanager.api.exception.BazaarManagerException;
import io.subutai.core.bazaarmanager.api.model.Config;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.exception.UserExistsException;
import io.subutai.core.identity.api.model.Role;
import io.subutai.core.identity.api.model.User;
import io.subutai.core.identity.api.model.UserToken;
import io.subutai.bazaar.share.dto.UserDto;
import io.subutai.bazaar.share.dto.UserTokenDto;
import io.subutai.bazaar.share.dto.environment.EnvironmentPeerDto;


public class EnvironmentUserHelper
{
    private final String baseBazaarTokenUrl = "/rest/v1/users/%s/token";

    private final Logger log = LoggerFactory.getLogger( getClass() );

    private final IdentityManager identityManager;

    private final ConfigDataService configDataService;

    private final EnvironmentManager environmentManager;

    private final RestClient restClient;


    public EnvironmentUserHelper( IdentityManager identityManager, ConfigDataService configDataService,
                                  EnvironmentManager environmentManager, RestClient restClient )
    {
        this.identityManager = identityManager;

        this.configDataService = configDataService;

        this.environmentManager = environmentManager;

        this.restClient = restClient;
    }


    public void handleEnvironmentOwnerDeletion( EnvironmentPeerDto peerDto )
    {
        String envOwnerId = peerDto.getEnvironmentInfo().getOwnerId();

        User user = getUserBybazaarId( envOwnerId );

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

        if ( hasUserEnvironmentsForPeerOnBazaar( envOwnerId ) )
        {
            log.debug( "Can't delete user b/c user has Bazaar environment on this peer" );

            return;
        }

        identityManager.removeUser( user.getId() );
        deleteUserToken( user.getId() );

        log.debug( "User deleted" );
    }


    private boolean hasUserEnvironmentsForPeerOnBazaar( String userId )
    {
        String path = String.format( "/rest/v1/adapter/users/%s/environments", userId );

        RestResult<String> restResult = restClient.get( path, String.class );

        if ( !restResult.isSuccess() )
        {
            log.error( "Error to get user environments from Bazaar: " + restResult.getError() );
        }

        String json = restResult.getEntity();

        return json != null && json.contains( "id" );
    }


    public User handleEnvironmentOwnerCreation( final String envOwnerId, final String peerId )
    {
        Config config = configDataService.getBazaarConfig( peerId );

        User user = getUserBybazaarId( envOwnerId );

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

        UserDto userDto = getUserDataFromBazaar( envOwnerId );

        return getOrCreateUser( userDto );
    }


    private User getUserBybazaarId( String userId )
    {
        for ( User user : identityManager.getAllUsers() )
        {
            String email = userId + BazaarManager.BAZAAR_EMAIL_SUFFIX;
            if ( user.getEmail().equals( email ) && user.isBazaarUser() )
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


    private User getOrCreateUser( UserDto userDto )
    {
        log.info( "Creating new user: {}", userDto.getEmail() );

        // Trick to get later the user id inbazaar
        String email = userDto.getId() + BazaarManager.BAZAAR_EMAIL_SUFFIX;

        try
        {
            User user = identityManager
                    .createUser( userDto.getFingerprint(), null, "[Bazaar] " + userDto.getName(), email,
                            UserType.BAZAAR.getId(), KeyTrustLevel.MARGINAL.getId(), false, true );

            identityManager.setUserPublicKey( user.getId(), userDto.getPublicKey() );
            identityManager.assignUserRole( user, getRole( IdentityManager.ENV_MANAGER_ROLE ) );
            identityManager.assignUserRole( user, getRole( IdentityManager.TEMPLATE_MANAGER_ROLE ) );

            log.info( "User created successfully" );

            return user;
        }
        catch ( UserExistsException e )
        {
            return identityManager.getUserByUsername( userDto.getFingerprint() );
        }
        catch ( Exception e )
        {
            log.error( "Error to create user: ", e );
        }

        return null;
    }


    public UserDto getUserDataFromBazaar( String userId )
    {
        String path = "/rest/v1/users/" + userId;

        RestResult<UserDto> restResult = restClient.get( path, UserDto.class );

        if ( !restResult.isSuccess() )
        {
            log.error( "Error to get user data from Bazaar: " + restResult.getError() );
        }

        return restResult.getEntity();
    }


    public UserToken getUserTokenFromBazaar( Long ssUserId ) throws BazaarManagerException, PGPException, IOException
    {
        String url = String.format( baseBazaarTokenUrl, ssUserId );
        RestResult<UserTokenDto> res = restClient.get( url, UserTokenDto.class );

        if ( res.getStatus() != HttpStatus.SC_OK && res.getStatus() != 204 )
        {
            throw new BazaarManagerException( "Error to get user token from Bazaar: HTTP " + res.getStatus() );
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
            return updateUserTokenInBazaar( userTokenDto );
        }
    }


    private UserToken updateUserTokenInBazaar( UserTokenDto userTokenDto )
    {
        String url = String.format( baseBazaarTokenUrl, userTokenDto.getOwnerId() );

        User user = identityManager.getUser( userTokenDto.getSsUserId() );

        if ( user != null )
        {
            UserToken userToken = identityManager.updateTokenAndSession( user.getId() );

            //set new token and valid date
            userTokenDto.setToken( userToken.getFullToken() );
            userTokenDto.setValidDate( userToken.getValidDate() );
            userTokenDto.setState( UserTokenDto.State.READY );

            restClient.post( url, userTokenDto );
            return userToken;
        }

        return null;
    }


    private void deleteUserToken( Long userId )
    {
        String url = String.format( "/rest/v1/users/%s/token/delete", userId );
        restClient.post( url, userId );
    }
}