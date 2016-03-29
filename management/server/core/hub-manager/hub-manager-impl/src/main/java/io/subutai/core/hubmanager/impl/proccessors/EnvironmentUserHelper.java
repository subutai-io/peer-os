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


    public EnvironmentUserHelper( ConfigManager configManager, IdentityManager identityManager )
    {
        this.configManager = configManager;

        this.identityManager = identityManager;
    }


    public void test()
    {
//        String userId = "554455fd-7fd3-47c3-b87d-cef2db75f8bc"; // askat
        String userId = "43163772-a8c2-459f-bfcb-4d0bcc5759f6"; // sydyk

        if ( userExists( userId ) )
        {
            log.debug( "User exists" );
        }
        else
        {
            UserDto userDto = getUserDataFromHub( userId );

            createNewUser( userDto, "!qaz@wsx" );
        }
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

    private void createNewUser( UserDto userDto, String password )
    {
        log.debug( "Creating new user: {}", userDto.getEmail() );

        // Trick to get later the user id in Hub
        String email = userDto.getId() + "@hub.subut.ai";

        try
        {
            User user = identityManager.createUser( userDto.getEmail(), password, "[Hub] " + userDto.getName(), email, UserType.Regular.getId(),
                    KeyTrustLevel.Marginal.getId(), false, false );

            identityManager.assignUserRole( user, getEnvironmentRole() );

            log.debug( "User created with id = {}", user.getId() );
        }
        catch ( Exception e )
        {
            log.error( "Error to create user: ", e );
        }
    }


    void handleEnvironmentOwner( EnvironmentPeerDto peerDto )
    {
        log.debug( ">> userId: {}", peerDto.getOwnerId() );
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
