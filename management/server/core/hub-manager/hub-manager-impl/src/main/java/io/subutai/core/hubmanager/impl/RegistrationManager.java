package io.subutai.core.hubmanager.impl;


import java.io.IOException;

import javax.ws.rs.core.Form;

import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.http.HttpStatus;

import io.subutai.common.security.crypto.pgp.PGPEncryptionUtil;
import io.subutai.common.settings.Common;
import io.subutai.common.settings.SubutaiInfo;
import io.subutai.core.hubmanager.api.RestClient;
import io.subutai.core.hubmanager.api.RestResult;
import io.subutai.core.hubmanager.api.exception.HubManagerException;
import io.subutai.core.hubmanager.api.model.Config;
import io.subutai.core.hubmanager.impl.model.ConfigEntity;
import io.subutai.core.identity.api.model.User;
import io.subutai.core.identity.api.model.UserToken;
import io.subutai.hub.share.dto.PeerInfoDto;
import io.subutai.hub.share.dto.RegistrationDto;
import io.subutai.hub.share.dto.UserTokenDto;
import io.subutai.hub.share.pgp.key.PGPKeyHelper;

import static java.lang.String.format;


//TODO update peer name from Hub periodically since it can change on Hub side
public class RegistrationManager
{
    private final Logger log = LoggerFactory.getLogger( getClass() );

    private final HubManagerImpl hubManager;

    private final ConfigManager configManager;

    private final String peerId;

    private final RestClient restClient;


    public RegistrationManager( HubManagerImpl hubManager, ConfigManager configManager )
    {
        this.hubManager = hubManager;
        this.configManager = configManager;
        this.peerId = configManager.getPeerId();

        restClient = hubManager.getRestClient();
    }


    public void registerPeer( String email, String password, String peerName, String peerScope )
            throws HubManagerException
    {
        registerPeerPubKey();

        register( email, password, peerName, peerScope );
    }


    private String readKeyText( PGPPublicKey key ) throws HubManagerException
    {
        try
        {
            return PGPEncryptionUtil.armorByteArrayToString( key.getEncoded() );
        }
        catch ( PGPException | IOException e )
        {
            throw new HubManagerException( "Error to read PGP key as text", e );
        }
    }


    private void registerPeerPubKey() throws HubManagerException
    {
        log.info( "Registering peer public key with Hub..." );

        Form form = new Form( "keytext", readKeyText( configManager.getPeerPublicKey() ) );

        RestResult<Object> restResult = restClient.postPlain( "/pks/add", form );

        if ( !restResult.isSuccess() )
        {
            throw new HubManagerException( "Error registering peer public key with Hub: " + restResult.getError() );
        }

        log.info( "Public key successfully registered" );
    }


    private RegistrationDto getRegistrationDto( String email, String password, String peerName, String peerScope )
    {
        PeerInfoDto peerInfoDto = new PeerInfoDto();

        peerInfoDto.setId( configManager.getPeerId() );
        peerInfoDto.setVersion( String.valueOf( SubutaiInfo.getVersion() ) );
        peerInfoDto.setName( peerName );
        peerInfoDto.setScope( peerScope );

        RegistrationDto dto = new RegistrationDto( PGPKeyHelper.getFingerprint( configManager.getOwnerPublicKey() ) );
        User activeUser = configManager.getActiveUser();
        UserToken token = configManager.getUserToken();

        dto.setOwnerEmail( email );
        dto.setOwnerPassword( password );
        dto.setPeerInfo( peerInfoDto );

        UserTokenDto userTokenDto =
                new UserTokenDto( null, activeUser.getId(), null, activeUser.getAuthId(), token.getFullToken(),
                        token.getTokenId(), token.getValidDate() );
        userTokenDto.setType( UserTokenDto.Type.USER );
        dto.setUserToken( userTokenDto );

        return dto;
    }


    private void register( String email, String password, String peerName, String peerScope ) throws HubManagerException
    {
        log.info( "Registering peer with Hub..." );

        String path = format( "/rest/v1/peers/%s", peerId );

        RegistrationDto regDto = getRegistrationDto( email, password, peerName, peerScope );

        RestResult<Object> restResult = restClient.post( path, regDto );

        if ( !restResult.isSuccess() )
        {
            throw new HubManagerException( "Error registering peer: " + restResult.getError() );
        }


        Config config;
        try
        {
            config = new ConfigEntity( regDto.getPeerInfo().getId(), Common.HUB_IP,
                    hubManager.getPeerInfo().get( "OwnerId" ), email, peerName );
        }
        catch ( Exception e )
        {
            throw new HubManagerException( e );
        }

        hubManager.getConfigDataService().saveHubConfig( config );

        log.info( "Peer registered successfully" );
    }


    public void unregister() throws HubManagerException
    {
        log.info( "Unregistering peer..." );

        String path = format( "/rest/v1/peers/%s/delete", peerId );

        RestResult<Object> restResult = restClient.delete( path );

        if ( restResult.getStatus() == HttpStatus.SC_FORBIDDEN )
        {
            log.info( "Peer or its pubic key not found on Hub. Unregistered anyway." );
        }
        else if ( !restResult.isSuccess() )
        {
            throw new HubManagerException( "Error to unregister peer: " + restResult.getError() );
        }

        hubManager.getConfigDataService().deleteConfig( configManager.getPeerId() );

        log.info( "Unregistered successfully" );
    }
}
