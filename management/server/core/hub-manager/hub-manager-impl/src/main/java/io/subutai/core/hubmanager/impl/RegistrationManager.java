package io.subutai.core.hubmanager.impl;


import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.subutai.common.security.crypto.pgp.PGPEncryptionUtil;
import io.subutai.common.security.crypto.pgp.PGPKeyUtil;
import io.subutai.common.settings.SecuritySettings;
import io.subutai.common.settings.SubutaiInfo;
import io.subutai.core.hubmanager.api.HubPluginException;
import io.subutai.core.hubmanager.api.model.Config;
import io.subutai.core.hubmanager.impl.model.ConfigEntity;
import io.subutai.hub.share.dto.PeerInfoDto;
import io.subutai.hub.share.dto.RegistrationDto;
import io.subutai.hub.share.dto.TrustDataDto;
import io.subutai.hub.share.dto.UserDto;
import io.subutai.hub.share.json.JsonUtil;
import io.subutai.hub.share.pgp.key.PGPKeyHelper;


public class RegistrationManager
{
    private static final Logger LOG = LoggerFactory.getLogger( RegistrationManager.class );

    private ConfigManager configManager;
    private IntegrationImpl manager;
    private String hubIp;


    public RegistrationManager( final IntegrationImpl manager, final ConfigManager configManager, final String hupIp )
    {
        this.configManager = configManager;
        this.manager = manager;
        this.hubIp = hupIp;
    }


    public void registerPeer( String email, String password ) throws HubPluginException
    {
        registerPeerPubKey();
        registerOwnerPubKey();

        RegistrationDto registrationData =
                new RegistrationDto( PGPKeyHelper.getFingerprint( configManager.getOwnerPublicKey() ) );

        registrationData.setOwnerEmail( email );
        registrationData.setOwnerPassword( password );

        String ssVersion = String.valueOf( SubutaiInfo.getVersion() );
        PeerInfoDto peerInfoDto = new PeerInfoDto();
        peerInfoDto.setId( configManager.getPeerId() );
        peerInfoDto.setVersion( ssVersion );
        peerInfoDto.setName( configManager.getPeerManager().getLocalPeer().getName() );
        registrationData.setPeerInfo( peerInfoDto );

        register( configManager.getPeerId(), registrationData );
        sentTrustData( configManager.getPeerPublicKey(), configManager.getOwnerPublicKey() );
    }


    private void sentTrustData( final PGPPublicKey peerPublicKey, final PGPPublicKey ownerPublicKey )
            throws HubPluginException
    {
        try
        {
            String path = String.format( "/rest/v1/keyserver/keys/%s/trust/%s",
                    PGPKeyUtil.getKeyId( peerPublicKey.getFingerprint() ),
                    PGPKeyUtil.getKeyId( ownerPublicKey.getFingerprint() ) );

            TrustDataDto trustDataDto = new TrustDataDto( PGPKeyUtil.getKeyId( peerPublicKey.getFingerprint() ),
                    PGPKeyUtil.getKeyId( ownerPublicKey.getFingerprint() ), TrustDataDto.TrustLevel.FULL );

            KeyStore keyStore = KeyStore.getInstance( "JKS" );

            keyStore.load( new FileInputStream( ConfigManager.PEER_KEYSTORE ),
                    SecuritySettings.KEYSTORE_PX1_PSW.toCharArray() );

            WebClient client = configManager.getTrustedWebClientWithAuth( path, hubIp );

            byte[] cborData = JsonUtil.toCbor( trustDataDto );

            byte[] encryptedData = configManager.getMessenger().produce( cborData );

            LOG.debug( "Sending Trust data to Hub..." );

            Response r = client.post( encryptedData );

            if ( r.getStatus() == HttpStatus.SC_NO_CONTENT )
            {
                LOG.debug( "Trust data sent successfully." );
            }
            else
            {
                throw new HubPluginException( "Could not send trust data: " + r.readEntity( String.class ) );
            }
        }
        catch ( PGPException | IOException | KeyStoreException | UnrecoverableKeyException | NoSuchAlgorithmException |
                CertificateException e )
        {
            throw new HubPluginException( e.toString(), e );
        }
    }


    private void register( final String peerId, RegistrationDto registrationData ) throws HubPluginException
    {
        try
        {
            String path = String.format( "/rest/v1/peers/%s", peerId );

            WebClient client = configManager.getTrustedWebClientWithAuth( path, hubIp );

            byte[] cborData = JsonUtil.toCbor( registrationData );

            byte[] encryptedData = configManager.getMessenger().produce( cborData );

            LOG.info( "Registering Peer. Sending RegistrationDTO to Hub..." );

            Response r = client.post( encryptedData );

            if ( r.getStatus() == HttpStatus.SC_NO_CONTENT )
            {
                Config config = new ConfigEntity();
                config.setHubIp( hubIp );
                config.setPeerId( configManager.getPeerId() );
                config.setOwnerId( manager.getPeerInfo().get( "OwnerId" ) );

                //
                // Get user email
                //

                UserDto userDto = getUserDataFromHub( config.getOwnerId() );

                if ( userDto != null )
                {
                    config.setOwnerEmail( userDto.getEmail() );
                }

                manager.getConfigDataService().saveHubConfig( config );

                LOG.info( "Peer registered successfully." );
            }
            else
            {
                String error = r.readEntity( String.class );

                LOG.error( "Error to register peer: {}", error );

                throw new HubPluginException( "Error to register peer: " + error );
            }
        }
        catch ( Exception e )
        {
            LOG.error( "Error to register peer: ", e );

            throw new HubPluginException( e.toString(), e );
        }
    }


    private UserDto getUserDataFromHub( String userId )
    {
        String path = "/rest/v1/users/" + userId;

        UserDto userDto = null;

        try
        {
            WebClient client = configManager.getTrustedWebClientWithAuth( path, configManager.getHubIp() );

            Response r = client.get();

            if ( r.getStatus() == HttpStatus.SC_OK )
            {
                byte[] encryptedContent = configManager.readContent( r );

                byte[] plainContent = configManager.getMessenger().consume( encryptedContent );

                userDto = JsonUtil.fromCbor( plainContent, UserDto.class );
            }
        }
        catch ( Exception e )
        {
            LOG.error( "Error to get user data: ", e );
        }

        return userDto;
    }

    public void registerOwnerPubKey() throws HubPluginException
    {
        WebClient client = configManager.getTrustedWebClient( hubIp );
        client.type( MediaType.APPLICATION_FORM_URLENCODED ).accept( MediaType.APPLICATION_JSON );

        Form form = new Form();
        try
        {
            form.param( "keytext",
                    PGPEncryptionUtil.armorByteArrayToString( configManager.getOwnerPublicKey().getEncoded() ) );
        }
        catch ( PGPException | IOException e )
        {
            throw new HubPluginException( "Could not read owner pub key", e );
        }

        LOG.debug( "Sending Owner public key to Hub..." );

        Response response = client.path( "pks/add" ).post( form );

        if ( response.getStatus() == HttpStatus.SC_CREATED )
        {
            LOG.debug( "Owner pub key successfully registered." );
            LOG.debug( String.format( "Owner fingerprint: %s",
                    PGPKeyUtil.getFingerprint( configManager.getOwnerPublicKey().getFingerprint() ) ) );
        }
        else
        {
            LOG.error( "Owner pub key registration failed!" );
        }
    }


    public void registerPeerPubKey() throws HubPluginException
    {
        WebClient client = configManager.getTrustedWebClient( hubIp );
        client.type( MediaType.APPLICATION_FORM_URLENCODED ).accept( MediaType.APPLICATION_JSON );

        Form form = new Form();
        try
        {
            form.param( "keytext",
                    PGPEncryptionUtil.armorByteArrayToString( configManager.getPeerPublicKey().getEncoded() ) );
        }
        catch ( PGPException | IOException e )
        {
            throw new HubPluginException( "Could not read peer pub key", e );
        }

        LOG.debug( "Sending Peer public key to Hub..." );

        Response response = client.path( "pks/add" ).post( form );

        if ( response.getStatus() == HttpStatus.SC_CREATED )
        {
            LOG.debug( "Peer public key successfully registered." );
            LOG.debug( String.format( "Peer fingerprint: %s",
                    PGPKeyUtil.getFingerprint( configManager.getPeerPublicKey().getFingerprint() ) ) );
        }
        else
        {
            LOG.error( "Peer pub key registration failed!" );
        }
    }


    private static ObjectMapper createMapper( JsonFactory factory )
    {
        ObjectMapper mapper = new ObjectMapper( factory );
        mapper.setVisibility( PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY );
        return mapper;
    }
}
