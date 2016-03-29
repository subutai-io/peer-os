package io.subutai.core.hubmanager.impl.proccessors;


import java.io.IOException;

import javax.ws.rs.core.Response;

import org.bouncycastle.openpgp.PGPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.http.HttpStatus;

import io.subutai.core.hubmanager.impl.ConfigManager;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.hub.share.dto.UserDto;
import io.subutai.hub.share.json.JsonUtil;


public class EnvironmentUserHelper
{
    private final Logger log = LoggerFactory.getLogger( getClass() );

    private ConfigManager configManager;

    private IdentityManager identityManager;


    public EnvironmentUserHelper( final ConfigManager configManager, final IdentityManager identityManager )
    {
        this.configManager = configManager;
        this.identityManager = identityManager;
    }


    public void test()
    {
        String userId = "554455fd-7fd3-47c3-b87d-cef2db75f8bc";

        UserDto userDto = getUserDataFromHub( userId );

        log.debug( "user: email={}, name={}", userDto.getEmail(), userDto.getName() );
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
