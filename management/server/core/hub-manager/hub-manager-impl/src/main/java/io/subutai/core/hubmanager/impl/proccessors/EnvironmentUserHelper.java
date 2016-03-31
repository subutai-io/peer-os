package io.subutai.core.hubmanager.impl.proccessors;


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
import io.subutai.core.hubmanager.api.dao.ConfigDataService;
import io.subutai.core.hubmanager.api.model.Config;
import io.subutai.core.hubmanager.impl.ConfigManager;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.model.Role;
import io.subutai.core.identity.api.model.User;
import io.subutai.hub.share.dto.UserDto;
import io.subutai.hub.share.dto.environment.EnvironmentPeerDto;
import io.subutai.hub.share.json.JsonUtil;


public class EnvironmentUserHelper
{
    private final Logger log = LoggerFactory.getLogger( getClass() );

    private final ConfigManager configManager;

    private final IdentityManager identityManager;

    private final ConfigDataService configDataService;


    public EnvironmentUserHelper( ConfigManager configManager, IdentityManager identityManager, ConfigDataService configDataService )
    {
        this.configManager = configManager;

        this.identityManager = identityManager;

        this.configDataService = configDataService;
    }



    void handleEnvironmentOwnerCreation( EnvironmentPeerDto peerDto )
    {
        String envOwnerId = peerDto.getOwnerId();

        Config config = configDataService.getHubConfig( configManager.getPeerId() );

        if ( envOwnerId.equals( config.getOwnerId() ) || userExists( envOwnerId ))
        {
            log.debug( "No need to create new user for environment" );

            return;
        }

        UserDto userDto = getUserDataFromHub( envOwnerId );

        createNewUser( userDto );
    }


    private boolean userExists( String userId )
    {
        for ( User user : identityManager.getAllUsers() )
        {
            if ( user.getEmail().startsWith( userId ) )
            {
                return true;
            }
        }

        return false;
    }


    private Role getEnvironmentRole()
    {
        for ( Role role : identityManager.getAllRoles() )
        {
            if ( role.getName().equals( "Environment-Manager" ) )
            {
                return role;
            }
        }

        return null;
    }

    private void createNewUser( UserDto userDto )
    {
        log.debug( "Creating new user: {}", userDto.getEmail() );

        // Trick to get later the user id in Hub
        String email = userDto.getId() + "@hub.subut.ai";

        String password = "" + Math.abs( userDto.getEmail().hashCode() );

        try
        {
            User user = identityManager.createUser( userDto.getEmail(), password, "[Hub] " + userDto.getName(), email, UserType.Regular.getId(),
                    KeyTrustLevel.Marginal.getId(), false, false );

            identityManager.assignUserRole( user, getEnvironmentRole() );

            log.debug( "User created successfully" );
        }
        catch ( Exception e )
        {
            log.error( "Error to create user: ", e );
        }
    }


    private UserDto getUserDataFromHub( String userId )
    {
        String path = "/rest/v1/users/" + userId;

        UserDto userDto = null;

        try
        {
            WebClient client = configManager.getTrustedWebClientWithAuth( path, configManager.getHubIp() );

            Response res = client.get();

            userDto = handleResponse( res );
        }
        catch ( Exception e )
        {
            log.error( "Error to get user data: ", e );
        }

        return userDto;
    }


    private UserDto handleResponse( Response response ) throws IOException, PGPException
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

        return JsonUtil.fromCbor( plainContent, UserDto.class );
    }
}