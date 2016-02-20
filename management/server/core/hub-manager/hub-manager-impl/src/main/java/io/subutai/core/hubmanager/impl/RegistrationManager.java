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
import io.subutai.common.settings.SubutaiInfo;
import io.subutai.common.settings.SystemSettings;
import io.subutai.core.hubmanager.api.HubPluginException;
import io.subutai.hub.share.dto.PeerInfoDto;
import io.subutai.hub.share.dto.RegistrationDto;
import io.subutai.hub.share.dto.TrustDataDto;
import io.subutai.hub.share.json.JsonUtil;
import io.subutai.hub.share.pgp.key.PGPKeyHelper;


public class RegistrationManager
{
    private static final Logger LOG = LoggerFactory.getLogger( RegistrationManager.class.getName() );

    private ConfigManager configManager;
    private IntegrationImpl manager;


    public RegistrationManager( final IntegrationImpl manager, final ConfigManager configManager )
    {
        this.configManager = configManager;
        this.manager = manager;
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
        peerInfoDto.setId( configManager.getPeerManager().getLocalPeer().getId() );
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

            keyStore.load( new FileInputStream( ConfigManager.PEER_KEYSTORE ), "subutai".toCharArray() );

            WebClient client = configManager.getTrustedWebClientWithAuth( path );

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
            WebClient client = configManager.getTrustedWebClientWithAuth( path );

            byte[] cborData = JsonUtil.toCbor( registrationData );

            byte[] encryptedData = configManager.getMessenger().produce( cborData );

            LOG.debug( "Registering Peer. Sending RegistrationDTO to Hub..." );

            Response r = client.post( encryptedData );


            if ( r.getStatus() == HttpStatus.SC_NO_CONTENT )
            {
                manager.getConfigDataService().saveHubConfig( configManager.getHubConfiguration() );
                LOG.debug( "Hub configuration saved successfully." );
                LOG.debug( "Peer registered successfully." );
                SystemSettings.setRegisterToHubState( true );
            }
            else
            {
                LOG.debug( "Could not register Peer: ", r.readEntity( String.class ) );
                throw new HubPluginException( "Could not register Peer: " + r.readEntity( String.class ) );
            }
        }
        catch ( PGPException | IOException | KeyStoreException | UnrecoverableKeyException | NoSuchAlgorithmException
                e )
        {
            LOG.error( "Could not register Peer", e );
            throw new HubPluginException( e.toString(), e );
        }
    }


    public void registerOwnerPubKey() throws HubPluginException
    {
        WebClient client = configManager.getTrustedWebClient();
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
        WebClient client = configManager.getTrustedWebClient();
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
