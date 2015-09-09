package io.subutai.core.hintegration.impl;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.io.IOUtils;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.jaxrs.ext.form.Form;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;

import io.subutai.common.security.SecurityProvider;
import io.subutai.common.security.crypto.certificate.CertificateData;
import io.subutai.common.security.crypto.certificate.CertificateTool;
import io.subutai.common.security.crypto.key.KeyPairType;
import io.subutai.common.security.crypto.keystore.KeyStoreData;
import io.subutai.common.security.crypto.keystore.KeyStoreTool;
import io.subutai.common.security.crypto.keystore.KeyStoreType;
import io.subutai.common.security.crypto.pgp.KeyPair;
import io.subutai.common.security.crypto.pgp.PGPEncryptionUtil;
import io.subutai.common.security.crypto.pgp.PGPKeyUtil;
import io.subutai.core.hintegration.api.HIntegrationException;
import io.subutai.core.hintegration.api.Integration;
import io.subutai.core.hintegration.impl.settings.HSettings;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.security.api.SecurityManager;
import io.subutai.core.security.api.crypto.KeyManager;
import io.subutai.hub.common.dto.HeartbeatResponseDTO;
import io.subutai.hub.common.dto.RegistrationDTO;
import io.subutai.hub.common.dto.TrustDataDto;
import io.subutai.hub.common.json.JsonUtil;
import io.subutai.hub.common.pgp.key.PGPKeyHelper;
import io.subutai.hub.common.pgp.message.PGPMessenger;


public class IntegrationImpl implements Integration
{
    private static final Logger LOG = LoggerFactory.getLogger( IntegrationImpl.class.getName() );

    private static final String SERVER_NAME = "52.88.77.35";
    private SecurityManager securityManager;
    private PeerManager peerManager;
    public static final String OWNER_USER_ID = "owner@subutai.io";
    private BundleContext bundleContext;
    private PGPPublicKey hubPublicKey;
    private PGPPublicKey ownerPublicKey;
    private PGPPublicKey peerPublicKey;
    private ScheduledExecutorService hearbeatExecutorService = Executors.newSingleThreadScheduledExecutor();
    private HeartbeatProcessor processor = new HeartbeatProcessor();


    public void setSecurityManager( final SecurityManager securityManager )
    {
        this.securityManager = securityManager;
    }


    public void setPeerManager( final PeerManager peerManager )
    {
        this.peerManager = peerManager;
    }


    public void setBundleContext( final BundleContext bundleContext )
    {
        this.bundleContext = bundleContext;
    }


    public void init() throws IOException, PGPException
    {
        LOG.debug( "H-INTEGRATION" );

        this.hubPublicKey = PGPKeyHelper.readPublicKey( HSettings.HUB_PUB_KEY );
        this.ownerPublicKey = PGPKeyHelper.readPublicKey( HSettings.PEER_OWNER_PUB_KEY );
        this.peerPublicKey = PGPKeyHelper.readPublicKey( HSettings.PEER_PUB_KEY );

        LOG.debug(
                String.format( "Peer fingerprint: %s", PGPKeyUtil.getFingerprint( peerPublicKey.getFingerprint() ) ) );
        LOG.debug( String.format( "Hub fingerprint: %s", PGPKeyUtil.getFingerprint( hubPublicKey.getFingerprint() ) ) );
        LOG.debug( String.format( "Owner fingerprint: %s",
                PGPKeyUtil.getFingerprint( ownerPublicKey.getFingerprint() ) ) );
        generateX509Certificate();

        String baseUrl = String.format( "https://%s:4000", SERVER_NAME );
        String path = String.format( "/rest/v1/peers/%s/hearbeat", peerManager.getLocalPeerInfo().getId() );
        WebClient client = io.subutai.core.hintegration.impl.HttpClient.createTrustedWebClient( baseUrl + path );

        processor.addEndpoint( client );
        this.hearbeatExecutorService.scheduleWithFixedDelay( processor, 10, 30, TimeUnit.SECONDS );
    }


    public void destroy()
    {
        LOG.debug( "Destroying H-INTEGRATION" );
        this.hearbeatExecutorService.shutdown();
    }
    /*private void generateKeys()
    {
        try
        {
            final KeyManager keyManager = securityManager.getKeyManager();


            /*//**************Get Peer Public keyring ******************************************
 PGPPublicKeyRing peerPublicKeyRing = keyManager.getPublicKeyRing( null );
 String fingerprint = PGPKeyUtil.getFingerprint( peerPublicKeyRing.getPublicKey().getFingerprint() );


 /*//**************Saving certificate******************************************
 io.subutai.common.security.crypto.key.KeyManager sslkeyMan =
 new io.subutai.common.security.crypto.key.KeyManager();
 KeyPairGenerator keyPairGenerator = sslkeyMan.prepareKeyPairGeneration( KeyPairType.RSA, 1024 );
 java.security.KeyPair sslKeyPair = sslkeyMan.generateKeyPair( keyPairGenerator );

 KeyStoreData keyStoreData = new KeyStoreData();
 keyStoreData.setKeyStoreFile( HSettings.PEER_KEYSTORE );
 keyStoreData.setAlias( "root_server_px1" );
 keyStoreData.setPassword( "subutai" );
 keyStoreData.setKeyStoreType( KeyStoreType.JKS );

 KeyStoreTool keyStoreTool = new KeyStoreTool();
 KeyStore sslkeyStore = keyStoreTool.load( keyStoreData );


 CertificateData certificateData = new CertificateData();
 certificateData.setCommonName( fingerprint );

 CertificateTool certificateTool = new CertificateTool();
 X509Certificate x509cert = certificateTool
 .generateSelfSignedCertificate( sslkeyStore, sslKeyPair, SecurityProvider.BOUNCY_CASTLE,
 certificateData );

 keyStoreTool.saveX509Certificate( sslkeyStore, keyStoreData, x509cert, sslKeyPair );


 /*//****************Save Owner secret key ****************************************
 KeyPair ownerKeyPair = keyManager.generateKeyPair( OWNER_USER_ID, false );

 File ownerSecretKeyFile = new File( HSettings.PEER_OWNER_SECRET_KEY );

 try ( FileOutputStream fop = new FileOutputStream( ownerSecretKeyFile ) )
 {
 if ( !ownerSecretKeyFile.exists() )
 {
 ownerSecretKeyFile.createNewFile();
 }

 fop.write( ownerKeyPair.getSecKeyring() );
 fop.flush();
 fop.close();

 LOG.debug( "owner secret key genereated" );
 }
 catch ( Exception ex )
 {
 ex.printStackTrace();
 }


 /*//****************Save Owner Public key ****************************************
 File ownerPubKeyFile = new File( HSettings.PEER_OWNER_PUB_KEY );

 try ( FileOutputStream fop = new FileOutputStream( ownerPubKeyFile ) )
 {
 if ( !ownerPubKeyFile.exists() )
 {
 ownerPubKeyFile.createNewFile();
 }

 fop.write( ownerKeyPair.getPubKeyring() );
 fop.flush();
 fop.close();

 LOG.debug( "owner public key genereated" );
 }
 catch ( Exception ex )
 {
 ex.printStackTrace();
 }


 /*//****************Sign pub of peer with owner ****************************************
 PGPSecretKeyRing ownerSecretKeyRing = PGPKeyUtil.readSecretKeyRing( ownerKeyPair.getSecKeyring() );
 PGPPublicKeyRing publicKeyRing = PGPEncryptionUtil
 .signPublicKey( peerPublicKeyRing, OWNER_USER_ID, ownerSecretKeyRing.getSecretKey(), "12345678" );


 /*//****************Save Peer Public key ****************************************
 File peerPubKeyFile = new File( HSettings.PEER_PUB_KEY );

 try ( FileOutputStream fop = new FileOutputStream( peerPubKeyFile ) )
 {
 if ( !peerPubKeyFile.exists() )
 {
 peerPubKeyFile.createNewFile();
 }

 publicKeyRing.encode( fop );
 fop.flush();
 fop.close();

 LOG.debug( "peer public key genereated" );
 }
 catch ( Exception ex )
 {
 ex.printStackTrace();
 }

 LOG.debug( "Is encryption key of owner?:" + ownerSecretKeyRing.getPublicKey().isEncryptionKey() );
 LOG.debug( "Is encryption key of peer?:" + publicKeyRing.getPublicKey().isEncryptionKey() );
 LOG.debug( "Is signed: " + PGPEncryptionUtil.verifyPublicKey( publicKeyRing.getPublicKey(), OWNER_USER_ID,
 ownerSecretKeyRing.getPublicKey() ) );
 //
 //            /*/
    /****************
     * Loading hub key from resources **************************************** //            InputStream is =
     * bundleContext.getBundle().getEntry( "keys/hub.public.gpg" ).openStream(); //            PGPPublicKey hPubKey =
     * PGPKeyUtil.readPublicKey( is ); } catch ( Exception ex ) { ex.printStackTrace(); } }
     */


    private void generateX509Certificate()
    {
        try
        {
            final KeyManager keyManager = securityManager.getKeyManager();


            String fingerprint = PGPKeyUtil.getFingerprint( peerPublicKey.getFingerprint() );


            //**************Saving X509 certificate******************************************
            io.subutai.common.security.crypto.key.KeyManager sslkeyMan =
                    new io.subutai.common.security.crypto.key.KeyManager();
            KeyPairGenerator keyPairGenerator = sslkeyMan.prepareKeyPairGeneration( KeyPairType.RSA, 1024 );
            java.security.KeyPair sslKeyPair = sslkeyMan.generateKeyPair( keyPairGenerator );

            KeyStoreData keyStoreData = new KeyStoreData();
            keyStoreData.setKeyStoreFile( HSettings.PEER_KEYSTORE );
            keyStoreData.setAlias( "root_server_px1" );
            keyStoreData.setPassword( "subutai" );
            keyStoreData.setKeyStoreType( KeyStoreType.JKS );

            KeyStoreTool keyStoreTool = new KeyStoreTool();
            KeyStore sslkeyStore = keyStoreTool.load( keyStoreData );


            CertificateData certificateData = new CertificateData();
            certificateData.setCommonName( fingerprint );

            CertificateTool certificateTool = new CertificateTool();
            X509Certificate x509cert = certificateTool
                    .generateSelfSignedCertificate( sslkeyStore, sslKeyPair, SecurityProvider.BOUNCY_CASTLE,
                            certificateData );

            keyStoreTool.saveX509Certificate( sslkeyStore, keyStoreData, x509cert, sslKeyPair );
        }
        catch ( Exception ex )
        {
            ex.printStackTrace();
        }
    }


    @Override
    public void registerOwnerPubKey() throws HIntegrationException
    {
        //        String baseUrl = String.format( "https://test.stage-hub.net/" );
        String baseUrl = String.format( "https://" + SERVER_NAME + "/" );
        WebClient client = io.subutai.core.hintegration.impl.HttpClient.createTrustedWebClient( baseUrl );

        client.type( MediaType.APPLICATION_FORM_URLENCODED ).accept( MediaType.APPLICATION_JSON );

        Form form = new Form();
        try
        {
            form.set( "keytext", PGPEncryptionUtil.armorByteArrayToString( ownerPublicKey.getEncoded() ) );
        }
        catch ( PGPException | IOException e )
        {
            throw new HIntegrationException( "Could not read owner pub key", e );
        }

        Response response = client.path( "pks/add" ).post( form );

        if ( response.getStatus() == HttpStatus.SC_CREATED )
        {
            LOG.debug( "Owner pub key successfully registered." );
        }
        else
        {
            LOG.error( "Owner pub key registration problem." );
        }
    }


    @Override
    public void registerPeerPubKey() throws HIntegrationException
    {
        //        String baseUrl = String.format( "https://test.stage-hub.net/" );
        String baseUrl = String.format( "https://" + SERVER_NAME + "/" );
        WebClient client = io.subutai.core.hintegration.impl.HttpClient.createTrustedWebClient( baseUrl );

        client.type( MediaType.APPLICATION_FORM_URLENCODED ).accept( MediaType.APPLICATION_JSON );

        Form form = new Form();
        try
        {
            form.set( "keytext", PGPEncryptionUtil.armorByteArrayToString( peerPublicKey.getEncoded() ) );
        }
        catch ( PGPException | IOException e )
        {
            throw new HIntegrationException( "Could not read peer pub key", e );
        }

        Response response = client.path( "pks/add" ).post( form );

        if ( response.getStatus() == HttpStatus.SC_CREATED )
        {
            LOG.debug( "Owner pub key successfully registered." );
        }
        else
        {
            LOG.error( "Owner pub key registration problem." );
        }
    }


    @Override
    public void sendTrustData() throws HIntegrationException
    {
        String baseUrl = String.format( "https://%s:4000", SERVER_NAME );

        try
        {
            String path = String.format( "/rest/v1/keyserver/keys/%s/trust/%s",
                    PGPKeyUtil.getKeyId( peerPublicKey.getFingerprint() ),
                    PGPKeyUtil.getKeyId( ownerPublicKey.getFingerprint() ) );

            TrustDataDto trustDataDto = new TrustDataDto( PGPKeyUtil.getKeyId( peerPublicKey.getFingerprint() ),
                    PGPKeyUtil.getKeyId( ownerPublicKey.getFingerprint() ), TrustDataDto.TrustLevel.FULL );

            byte[] serverFingerprint = hubPublicKey.getFingerprint();

            KeyStore keyStore = KeyStore.getInstance( "JKS" );

            keyStore.load( new FileInputStream( HSettings.PEER_KEYSTORE ), "subutai".toCharArray() );

            WebClient client = HttpClient
                    .createTrustedWebClientWithAuth( baseUrl + path, keyStore, "subutai".toCharArray(),
                            serverFingerprint );

            byte[] cborData = JsonUtil.toCbor( trustDataDto );

            KeyManager keyManager = securityManager.getKeyManager();
            PGPPrivateKey senderKey = PGPKeyHelper.readPrivateKey( HSettings.PEER_SECRET_KEY, "12345678" );
            PGPMessenger messenger = new PGPMessenger( senderKey, hubPublicKey );

            byte[] encryptedData = messenger.produce( cborData );
            Response r = client.post( encryptedData );

            if ( r.getStatus() != HttpStatus.SC_NO_CONTENT )
            {
                throw new HIntegrationException( "Could not send trust data: " + r.readEntity( String.class ) );
            }
        }
        catch ( PGPException | IOException | KeyStoreException | UnrecoverableKeyException | NoSuchAlgorithmException |
                CertificateException e )
        {
            throw new HIntegrationException( e.toString(), e );
        }
    }


    @Override
    public void register() throws HIntegrationException
    {
        String baseUrl = String.format( "https://%s:4000", SERVER_NAME );

        try
        {
            String path = String.format( "/rest/v1/peers/%s", peerManager.getLocalPeerInfo().getId() );

            RegistrationDTO registrationData =
                    new RegistrationDTO( PGPKeyHelper.getFingerprint( ownerPublicKey ).toUpperCase() );

            LOG.debug( "HEX owner key id: " + PGPKeyUtil.getKeyId( ownerPublicKey.getFingerprint() ) );

            byte[] serverFingerprint = hubPublicKey.getFingerprint();

            KeyStore keyStore = KeyStore.getInstance( "JKS" );

            keyStore.load( new FileInputStream( HSettings.PEER_KEYSTORE ), "subutai".toCharArray() );

            WebClient client = HttpClient
                    .createTrustedWebClientWithAuth( baseUrl + path, keyStore, "subutai".toCharArray(),
                            serverFingerprint );

            byte[] cborData = JsonUtil.toCbor( registrationData );

            KeyManager keyManager = securityManager.getKeyManager();
            PGPPrivateKey senderKey = PGPKeyHelper.readPrivateKey( HSettings.PEER_SECRET_KEY, "12345678" );
            PGPMessenger messenger = new PGPMessenger( senderKey, hubPublicKey );

            byte[] encryptedData = messenger.produce( cborData );
            Response r = client.post( encryptedData );


            if ( r.getStatus() != HttpStatus.SC_NO_CONTENT )
            {
                throw new HIntegrationException( "Could not register local peer: " + r.readEntity( String.class ) );
            }
        }
        catch ( PGPException | IOException | KeyStoreException | UnrecoverableKeyException | NoSuchAlgorithmException |
                CertificateException e )
        {
            throw new HIntegrationException( e.toString(), e );
        }
    }


    @Override
    public HeartbeatResponseDTO sendHeartbeat() throws HIntegrationException
    {
        String baseUrl = String.format( "https://%s:4000", SERVER_NAME );
        HeartbeatResponseDTO result = null;
        try
        {
            String path = String.format( "/rest/v1/peers/%s/heartbeat", peerManager.getLocalPeerInfo().getId() );

            byte[] serverFingerprint = hubPublicKey.getFingerprint();

            KeyStore keyStore = KeyStore.getInstance( "JKS" );

            keyStore.load( new FileInputStream( HSettings.PEER_KEYSTORE ), "subutai".toCharArray() );

            WebClient client = HttpClient
                    .createTrustedWebClientWithAuth( baseUrl + path, keyStore, "subutai".toCharArray(),
                            serverFingerprint );

            PGPPrivateKey senderKey = PGPKeyHelper.readPrivateKey( HSettings.PEER_SECRET_KEY, "12345678" );
            PGPMessenger messenger = new PGPMessenger( senderKey, hubPublicKey );

            Response r = client.put( null );


            if ( r.getStatus() != HttpStatus.SC_OK )
            {
                throw new HIntegrationException( "Could not send heartbeat: " + r.readEntity( String.class ) );
            }

            byte[] data = readContent( r );

            if ( data != null )
            {
                result = JsonUtil.fromCbor( messenger.consume( data ), HeartbeatResponseDTO.class );
                LOG.debug( result.getStateLinks().toString() );
            }
            else
            {
                LOG.debug( "Data is null." );
            }
        }
        catch ( PGPException | IOException | KeyStoreException | UnrecoverableKeyException | NoSuchAlgorithmException |
                CertificateException e )
        {
            throw new HIntegrationException( e.toString(), e );
        }

        return result;
    }


    private byte[] readContent( Response response ) throws IOException
    {
        if ( response.getEntity() == null )
        {
            return null;
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        InputStream is = ( ( InputStream ) response.getEntity() );

        IOUtils.copy( is, bos );
        return bos.toByteArray();
    }
}
