package io.subutai.core.hintegration.impl;


import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.net.util.SubnetUtils;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.jaxrs.ext.form.Form;
import org.apache.http.HttpStatus;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.subutai.common.host.Interface;
import io.subutai.common.peer.InterfacePattern;
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
import io.subutai.hub.common.dto.EnvironmentDTO;
import io.subutai.hub.common.dto.EnvironmentPackageDTO;
import io.subutai.hub.common.dto.EnvironmentPeerDataDTO;
import io.subutai.hub.common.dto.HeartbeatResponseDTO;
import io.subutai.hub.common.dto.RegistrationDTO;
import io.subutai.hub.common.dto.TrustDataDto;
import io.subutai.hub.common.json.JsonUtil;
import io.subutai.hub.common.pgp.crypto.PGPDecrypt;
import io.subutai.hub.common.pgp.crypto.PGPEncrypt;
import io.subutai.hub.common.pgp.key.PGPKeyHelper;
import io.subutai.hub.common.pgp.message.PGPMessenger;


public class IntegrationImpl implements Integration
{
    private static final Logger LOG = LoggerFactory.getLogger( IntegrationImpl.class.getName() );

    private static final Pattern ENVIRONMENT_DATA_PATTERN =
            Pattern.compile( "/rest/v1/environments/([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})" );
    private static final Pattern ENVIRONMENT_PACKAGE_PATTERN = Pattern.compile(
            "/rest/v1/environments/([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})/peers/"
                    + "([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})/package" );

    private static final String ENVIRONMENT_SUBNET = "10.11.111.0/24";
    //    private static final String SERVER_NAME = "52.88.77.35";
    //    private static final String SERVER_NAME = "52.19.74.127";
    private static final String SERVER_NAME = "52.19.101.194";
    private SecurityManager securityManager;
    private PeerManager peerManager;
    public static final String OWNER_USER_ID = "owner@subutai.io";
    private static String baseUrl = String.format( "https://%s:4000", SERVER_NAME );
    private BundleContext bundleContext;
    private PGPPublicKey hubPublicKey;
    private PGPPublicKey ownerPublicKey;
    private PGPPublicKey peerPublicKey;
    private ScheduledExecutorService hearbeatExecutorService = Executors.newSingleThreadScheduledExecutor();
    private HeartbeatProcessor processor;
    private KeyStore keyStore;
    private byte[] serverFingerprint;
    private PGPMessenger messenger;
    private PGPPrivateKey senderKey;
    private UUID peerId;


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


    public void init() throws HIntegrationException
    {
        LOG.debug( "H-INTEGRATION" );

        try
        {
            this.peerId = peerManager.getLocalPeerInfo().getId();

            this.hubPublicKey = PGPKeyHelper.readPublicKey( HSettings.HUB_PUB_KEY );
                        this.ownerPublicKey = PGPKeyHelper.readPublicKey( HSettings.PEER_OWNER_PUB_KEY );
                        this.peerPublicKey = PGPKeyHelper.readPublicKey( HSettings.PEER_PUB_KEY );


//            this.ownerPublicKey = securityManager.getKeyManager().getPublicKey( "owner-" + peerId.toString() );
//            this.peerPublicKey = securityManager.getKeyManager().getPublicKey( null );

            LOG.debug( String.format( "Peer fingerprint: %s",
                    PGPKeyUtil.getFingerprint( peerPublicKey.getFingerprint() ) ) );
            LOG.debug( String.format( "Hub fingerprint: %s",
                    PGPKeyUtil.getFingerprint( hubPublicKey.getFingerprint() ) ) );
            LOG.debug( String.format( "Owner fingerprint: %s",
                    PGPKeyUtil.getFingerprint( ownerPublicKey.getFingerprint() ) ) );

            serverFingerprint = hubPublicKey.getFingerprint();

            senderKey = PGPKeyHelper.readPrivateKey( HSettings.PEER_SECRET_KEY, "12345678" );
            messenger = new PGPMessenger( senderKey, hubPublicKey );

            generateX509Certificate();


            String baseUrl = String.format( "https://%s:4000", SERVER_NAME );
            String path = String.format( "/rest/v1/peers/%s/hearbeat", peerManager.getLocalPeerInfo().getId() );
            WebClient client = io.subutai.core.hintegration.impl.HttpClient.createTrustedWebClient( baseUrl + path );

            processor = new HeartbeatProcessor( this );
            this.hearbeatExecutorService.scheduleWithFixedDelay( processor, 10, 30, TimeUnit.SECONDS );

            keyStore = KeyStore.getInstance( "JKS" );

            keyStore.load( new FileInputStream( HSettings.PEER_KEYSTORE ), "subutai".toCharArray() );
        }
        catch ( IOException | PGPException | CertificateException | KeyStoreException e )
        {
            throw new HIntegrationException( "Could not initialize integration module.", e );
        }
        catch ( NoSuchAlgorithmException e )
        {
            e.printStackTrace();
        }
    }


    public void destroy()
    {
        LOG.debug( "Destroying H-INTEGRATION" );
        this.hearbeatExecutorService.shutdown();
    }


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


            WebClient client = HttpClient
                    .createTrustedWebClientWithAuth( baseUrl + path, keyStore, "subutai".toCharArray(),
                            serverFingerprint );

            byte[] cborData = JsonUtil.toCbor( registrationData );

            byte[] encryptedData = messenger.produce( cborData );
            Response r = client.post( encryptedData );


            if ( r.getStatus() != HttpStatus.SC_NO_CONTENT )
            {
                throw new HIntegrationException( "Could not register local peer: " + r.readEntity( String.class ) );
            }
        }
        catch ( PGPException | IOException | KeyStoreException | UnrecoverableKeyException | NoSuchAlgorithmException
                e )
        {
            throw new HIntegrationException( e.toString(), e );
        }
    }


    @Override
    public Set<String> sendHeartbeat() throws HIntegrationException
    {

        Set<String> result = new HashSet<>();
        try
        {
            String path = String.format( "/rest/v1/peers/%s/heartbeat", peerManager.getLocalPeerInfo().getId() );

            WebClient client = HttpClient
                    .createTrustedWebClientWithAuth( baseUrl + path, keyStore, "subutai".toCharArray(),
                            serverFingerprint );

            Response r = client.put( null );


            if ( r.getStatus() != HttpStatus.SC_OK )
            {
                throw new HIntegrationException( "Could not send heartbeat: " + r.readEntity( String.class ) );
            }

            byte[] data = readContent( r );

            if ( data != null )
            {
                HeartbeatResponseDTO response =
                        JsonUtil.fromCbor( messenger.consume( data ), HeartbeatResponseDTO.class );
                LOG.debug( response.getStateLinks().toString() );
                result.addAll( new HashSet<String>( response.getStateLinks() ) );
            }
            else
            {
                LOG.debug( "Data is null." );
            }
        }
        catch ( PGPException | IOException | KeyStoreException | UnrecoverableKeyException | NoSuchAlgorithmException
                e )
        {
            LOG.error( "Could not send heartbeat.", e );
        }

        return result;
    }


    @Override
    public void processStateLink( final String link ) throws HIntegrationException
    {
        // Environment Package  GET /rest/v1/environments/{environment-id}/peers/{peer-id}/package
        // Environment Data     GET /rest/v1/environments/{environment-id}

        Matcher environmentDataMatcher = ENVIRONMENT_DATA_PATTERN.matcher( link );
        if ( environmentDataMatcher.matches() )
        {
            EnvironmentDTO environmentDTO = getEnvironmentData( link );
            processEnvironmentData( environmentDTO );
        }
        else
        {
            LOG.warn( "Unknown state link: " + link );
        }
    }


    private EnvironmentDTO getEnvironmentData( String link ) throws HIntegrationException
    {
        try
        {
            WebClient client = HttpClient
                    .createTrustedWebClientWithAuth( baseUrl + link, keyStore, "subutai".toCharArray(),
                            serverFingerprint );

            Response r = client.get();
            EnvironmentDTO result = null;

            if ( r.getStatus() == HttpStatus.SC_NO_CONTENT )
            {
                return result;
            }

            if ( r.getStatus() != HttpStatus.SC_OK )
            {
                LOG.error( r.readEntity( String.class ) );
                return result;
            }

            byte[] encryptedContent = readContent( r );

            byte[] plainContent = messenger.consume( encryptedContent );
            result = JsonUtil.fromCbor( plainContent, EnvironmentDTO.class );
            LOG.debug( "EnvironmentDTO: " + result.toString() );
            return result;
        }
        catch ( UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException | PGPException | IOException
                e )
        {
            throw new HIntegrationException( "Could not retrieve environment data", e );
        }
    }


    private void processEnvironmentData( final EnvironmentDTO environmentDTO ) throws HIntegrationException
    {

        if ( environmentDTO.getState() == EnvironmentDTO.State.INITIALIZING )
        {
            //GET rest/v1/environments/{environment-id}/peers/{peer-id}/package

            EnvironmentPeerDataDTO dto = processInitializing( environmentDTO );
            LOG.debug( String.format( "%s", dto ) );
            sendEnvironmentPeerData( environmentDTO.getId(), dto );
        }
        else if ( environmentDTO.getState() == EnvironmentDTO.State.RENDEZVOUS )
        {
            EnvironmentPeerDataDTO dto = processRendevouse( environmentDTO );
            LOG.debug( String.format( "%s", dto ) );
            //            sendEnvironmentPeerData( environmentDTO.getId(), dto );
        }
    }


    private EnvironmentPeerDataDTO processRendevouse( EnvironmentDTO environmentDTO ) throws HIntegrationException
    {
        EnvironmentPeerDataDTO dto = new EnvironmentPeerDataDTO();

        try
        {
            String path = String.format( "/rest/v1/environments/%s/peers/%s/package", environmentDTO.getId(), peerId );
            EnvironmentPackageDTO packageDTO = getEnvironmentPackageData( path, environmentDTO );
            LOG.debug( String.format( "Environment package: %s", packageDTO ) );
            Set<String> exceptedAddresses = new HashSet<>();
            if ( packageDTO.getPeers() != null )
            {
                for ( EnvironmentPeerDataDTO peerData : packageDTO.getPeers() )
                {
                    exceptedAddresses.add( peerData.getCommunityIP() );
                    LOG.debug( String.format( "%s", peerData ) );
                }
            }


            dto.setPeerId( peerId );
            dto.setState( EnvironmentPeerDataDTO.State.READY );
        }
        catch ( Exception e )
        {
            dto.setState( EnvironmentPeerDataDTO.State.REFUSED );
            LOG.error( e.getMessage(), e );
        }

        return dto;
    }


    private EnvironmentPeerDataDTO processInitializing( final EnvironmentDTO environmentDTO )
            throws HIntegrationException
    {
        EnvironmentPeerDataDTO result = new EnvironmentPeerDataDTO();
        result.setPeerId( peerId );
        try
        {
            //            String path = String.format( "/rest/v1/environments/%s/peers/%s/package", environmentDTO
            // .getId(), peerId );
            //            EnvironmentPackageDTO packageDTO = getEnvironmentPackageData( path );
            //            LOG.debug( String.format( "Environment package: %s", packageDTO ) );
            Set<String> exceptedAddresses = new HashSet<>();
            //            if ( packageDTO.getPeers() != null )
            //            {
            //                for ( EnvironmentPeerDataDTO peerData : packageDTO.getPeers() )
            //                {
            //                    exceptedAddresses.add( peerData.getCommunityIP() );
            //                }
            //            }

            String address = findFreeAddress( exceptedAddresses );

            TrustDataDto trustDataDto = new TrustDataDto( PGPKeyUtil.getKeyId( peerPublicKey.getFingerprint() ),
                    PGPKeyUtil.getKeyId( ownerPublicKey.getFingerprint() ), TrustDataDto.TrustLevel.FULL );

            String pekId = String.format( "%s-%s", peerId, environmentDTO.getId() );

            KeyPair kp = securityManager.getKeyManager().generateKeyPair( pekId, false );
            securityManager.getKeyManager().saveKeyPair( pekId, ( short ) 2, kp );

            //            String pek = securityManager.getKeyManager().getPublicKeyRingAsASCII( pekId );

            PGPPublicKey pekPublicKey = securityManager.getKeyManager().getPublicKey( pekId );

            String pek = PGPKeyUtil.exportAscii( pekPublicKey );

            LOG.debug( String.format( "PEK fingerprint: %s. %s",
                    PGPKeyUtil.getFingerprint( pekPublicKey.getFingerprint() ),
                    pek.equals( securityManager.getKeyManager().getPublicKeyRingAsASCII( pekId ) ) ) );
            result.setCommunityIP( address );
            result.setTrustData( trustDataDto );
            result.setState( EnvironmentPeerDataDTO.State.ACCEPTED );
            result.setPEK( pek );
        }
        catch ( Exception e )
        {
            result.setState( EnvironmentPeerDataDTO.State.REFUSED );
            LOG.error( e.getMessage(), e );
        }
        return result;
    }


    private String findFreeAddress( Set<String> exceptedAddresses )
    {
        SubnetUtils.SubnetInfo info = new SubnetUtils( ENVIRONMENT_SUBNET ).getInfo();

        InterfacePattern interfacePattern = new InterfacePattern( "ip", "10\\.111\\.11\\..*" );
        Set<Interface> interfaces = peerManager.getLocalPeer().getNetworkInterfaces( interfacePattern );

        String result = null;
        Set<String> hosts = new HashSet<>( exceptedAddresses );

        for ( Interface intf : interfaces )
        {
            hosts.add( intf.getIp() );
        }

        Integer i = 1;
        result = null;
        while ( i < 255 && result == null )
        {
            String ip = String.format( "10.11.111.%d", i );
            if ( !hosts.contains( ip ) )
            {
                result = ip;
            }
        }

        return result;
    }


    //    PUT rest/v1/environments/{environment-id}/peers/{peer-id}
    private void sendEnvironmentPeerData( UUID environmentId, EnvironmentPeerDataDTO environmentPeerDataDTO )
            throws HIntegrationException
    {
        LOG.debug( "Sending: " + environmentPeerDataDTO );
        String path =
                String.format( "/rest/v1/environments/%s/peers/%s", environmentId, environmentPeerDataDTO.getPeerId() );
        try
        {
            WebClient client = HttpClient
                    .createTrustedWebClientWithAuth( baseUrl + path, keyStore, "subutai".toCharArray(),
                            serverFingerprint );

            byte[] plainData = JsonUtil.toCbor( environmentPeerDataDTO );
            byte[] encryptedData = messenger.produce( plainData );
            Response r = client.put( encryptedData );
            if ( r.getStatus() != HttpStatus.SC_NO_CONTENT )
            {
                LOG.warn( "Unexpected response: " + r.readEntity( String.class ) );
            }
        }
        catch ( UnrecoverableKeyException | PGPException | NoSuchAlgorithmException | KeyStoreException |
                JsonProcessingException e )
        {
            throw new HIntegrationException( "Could not send environment peer data.", e );
        }
    }


    private EnvironmentPackageDTO getEnvironmentPackageData( String path, EnvironmentDTO environmentDTO )
            throws HIntegrationException
    {
        EnvironmentPackageDTO result = null;

        try
        {
            WebClient client = HttpClient
                    .createTrustedWebClientWithAuth( baseUrl + path, keyStore, "subutai".toCharArray(),
                            serverFingerprint );

            Response r = client.get();


            if ( r.getStatus() == HttpStatus.SC_NO_CONTENT )
            {
                return result;
            }

            if ( r.getStatus() != HttpStatus.SC_OK )
            {
                LOG.error( r.readEntity( String.class ) );
                return result;
            }

            byte[] encryptedContent = readContent( r );


            String doc = new String( encryptedContent );
//            LOG.debug( "DOC: " + doc );
            byte[] cborContent = messenger.consume( encryptedContent );

//            doc = new String( cborContent  );
//            LOG.debug( "PEK encrypted content: " + doc );
            KeyManager keyManager = securityManager.getKeyManager();
            String pekId = String.format( "%s-%s", peerId, environmentDTO.getId() );

            PGPPrivateKey pekPrivateKey = keyManager.getPrivateKey( pekId );

            if (pekPrivateKey == null) {
                throw new KeyStoreException("PEK private key not found.");
            }
            PGPPublicKey pekPublicKey = keyManager.getPublicKey( pekId );

            LOG.debug( String.format( "PEK fingerprint: %s",
                    PGPKeyUtil.getFingerprint( pekPublicKey.getFingerprint() ) ) );

//
//            byte[] e = PGPEncrypt.encrypt( "Timur".getBytes(), pekPublicKey );
//            byte[] d = PGPDecrypt.decrypt( e, pekPrivateKey );

//            LOG.debug( String.format( "-----> original text: %s", new String( d ) ) );
//            LOG.debug( String.format( "-----> PEK encrypted content length: %d", cborContent.length ) );
//            LOG.debug( PGPEncryptionUtil.armorByteArrayToString( pekPublicKey.getPublicKeyPacket().getEncoded() ) );

//            PGPSecretKeyRing kr = keyManager.getSecretKeyRing( pekId );
//
//            LOG.debug( "PEK: " + PGPEncryptionUtil.armorByteArrayToString( kr.getEncoded() ) );

//            PGPSecretKeyRing krPeer = keyManager.getSecretKeyRing( null );

//            LOG.debug( "Peer: " + PGPEncryptionUtil.armorByteArrayToString( krPeer.getEncoded() ) );

            byte[] pekEncryptedContent = JsonUtil.fromCbor( cborContent, byte[].class );

            byte[] plainContent = PGPDecrypt.decrypt( pekEncryptedContent, pekPrivateKey );

            result = JsonUtil.fromCbor( plainContent, EnvironmentPackageDTO.class );
            return result;
        }
        catch ( UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException | IOException | PGPException
                e )
        {
            throw new HIntegrationException( "Could not retrieve environment package data.", e );
        }
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
