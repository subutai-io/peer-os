package io.subutai.core.hubmanager.impl.processor;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.security.objects.KeyTrustLevel;
import io.subutai.common.security.objects.UserType;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.hubmanager.api.dao.ConfigDataService;
import io.subutai.core.hubmanager.api.model.Config;
import io.subutai.core.hubmanager.impl.http.HubRestClient;
import io.subutai.core.hubmanager.impl.http.RestResult;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.model.Role;
import io.subutai.core.identity.api.model.User;
import io.subutai.hub.share.dto.UserDto;
import io.subutai.hub.share.dto.environment.EnvironmentPeerDto;


public class EnvironmentUserHelper
{
    private final Logger log = LoggerFactory.getLogger( getClass() );

    private final IdentityManager identityManager;

    private final ConfigDataService configDataService;

    private final EnvironmentManager environmentManager;

    private final HubRestClient restClient;


    public EnvironmentUserHelper( IdentityManager identityManager, ConfigDataService configDataService, EnvironmentManager environmentManager,
                                  HubRestClient restClient )
    {
        this.identityManager = identityManager;

        this.configDataService = configDataService;

        this.environmentManager = environmentManager;

        this.restClient = restClient;
    }


    public void handleEnvironmentOwnerDeletion( EnvironmentPeerDto peerDto )
    {
        String envOwnerId = peerDto.getOwnerId();

        User user = getUserByHubId( envOwnerId );

        if ( user == null )
        {
            return;
        }

        log.debug( "Deleting environment owner: id={}, name={}, email={}", user.getId(), user.getUserName(), user.getEmail() );

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
        String envOwnerId = peerDto.getOwnerId();

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


    private User createUser( UserDto userDto )
    {
        log.info( "Creating new user: {}", userDto.getEmail() );

        // Trick to get later the user id in Hub
        String email = userDto.getId() + "@hub.subut.ai";

        try
        {
            User user = identityManager.createUser( userDto.getFingerprint(), null, "[Hub] " + userDto.getName(), email,
                    UserType.Regular.getId(), KeyTrustLevel.Marginal.getId(), false, true );

            identityManager.setUserPublicKey( user.getId(), userDto.getPublicKey() );
            identityManager.assignUserRole( user, getRole( "Environment-Manager" ) );
            identityManager.assignUserRole( user, getRole( "Template-Management" ) );

            log.info( "User created successfully" );

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

        RestResult<UserDto> restResult = restClient.get( path, UserDto.class );

        if ( !restResult.isSuccess() )
        {
            log.error( "Error to get user data from Hub: " + restResult.getError() );
        }

        return restResult.getEntity();
    }
}