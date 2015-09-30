package io.subutai.core.hintegration.impl;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
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
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.net.util.SubnetUtils;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.jaxrs.ext.form.Form;
import org.apache.http.HttpStatus;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.subutai.common.host.Interface;
import io.subutai.common.protocol.N2NConfig;
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
import io.subutai.common.util.N2NUtil;
import io.subutai.core.hintegration.api.HIntegrationException;
import io.subutai.core.hintegration.api.Integration;
import io.subutai.core.hintegration.impl.settings.HSettings;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.security.api.SecurityManager;
import io.subutai.core.security.api.crypto.KeyManager;
import io.subutai.hub.common.dto.EnvironmentDto;
import io.subutai.hub.common.dto.EnvironmentPackageDto;
import io.subutai.hub.common.dto.EnvironmentPeerDataDto;
import io.subutai.hub.common.dto.HeartbeatResponseDto;
import io.subutai.hub.common.dto.PeerProductDataDto;
import io.subutai.hub.common.dto.ProductDto;
import io.subutai.hub.common.dto.RegistrationDto;
import io.subutai.hub.common.dto.TrustDataDto;
import io.subutai.hub.common.json.JsonUtil;
import io.subutai.hub.common.pgp.crypto.PGPDecrypt;
import io.subutai.hub.common.pgp.key.PGPKeyHelper;
import io.subutai.hub.common.pgp.message.PGPMessenger;


public class IntegrationImpl implements Integration
{
    private static final Logger LOG = LoggerFactory.getLogger( IntegrationImpl.class.getName() );

    private static final Pattern ENVIRONMENT_DATA_PATTERN =
            Pattern.compile( "/rest/v1/environments/([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})" );
    private static final Pattern PRODUCT_DATA_PATTERN = Pattern.compile(
            "/rest/v1/peers/([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})/products/"
                    + "([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})" );

    private static final String PATH_TO_DEPLOY = String.format( "%s/deploy", System.getProperty( "karaf.home" ) );

    private static final String SERVER_NAME = "52.19.101.194";
    private static final String SUPERNODE_SERVER = "52.19.101.194";
    private static final int SUPERNODE_PORT = 5000;
    private SecurityManager securityManager;
    private PeerManager peerManager;
    private String baseUrl = String.format( "https://%s:4000", SERVER_NAME );
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
    private String peerId;

    private boolean integrationEnabled;


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
        if ( integrationEnabled )
        {
            LOG.debug( "H-INTEGRATION" );

            try
            {
                this.peerId = peerManager.getLocalPeerInfo().getId();

                this.hubPublicKey = PGPKeyHelper.readPublicKey( HSettings.H_PUB_KEY );
                this.ownerPublicKey = securityManager.getKeyManager().getPublicKey( "owner-" + peerId );
                this.peerPublicKey = securityManager.getKeyManager().getPublicKey( null );

                LOG.debug( String.format( "Peer fingerprint: %s", PGPKeyUtil.getFingerprint( peerPublicKey.getFingerprint() ) ) );
                LOG.debug( String.format( "Hub fingerprint: %s", PGPKeyUtil.getFingerprint( hubPublicKey.getFingerprint() ) ) );
                LOG.debug( String.format( "Owner fingerprint: %s", PGPKeyUtil.getFingerprint( ownerPublicKey.getFingerprint() ) ) );

                serverFingerprint = hubPublicKey.getFingerprint();

                //            senderKey = PGPKeyHelper.readPrivateKey( HSettings.PEER_SECRET_KEY, "12345678" );
                senderKey = securityManager.getKeyManager().getPrivateKey( null );
                messenger = new PGPMessenger( senderKey, hubPublicKey );

                generateX509Certificate();

                String path = String.format( "/rest/v1/peers/%s/hearbeat", peerManager.getLocalPeerInfo().getId() );
                WebClient client = io.subutai.core.hintegration.impl.HttpClient.createTrustedWebClient( baseUrl +
                        path );

                processor = new HeartbeatProcessor( this );
                this.hearbeatExecutorService.scheduleWithFixedDelay( processor, 10, 180, TimeUnit.SECONDS );

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
        try
        {
            String path = String.format( "/rest/v1/peers/%s", peerManager.getLocalPeerInfo().getId() );

            RegistrationDto registrationData = new RegistrationDto( PGPKeyHelper.getFingerprint( ownerPublicKey ) );

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
                HeartbeatResponseDto response =
                        JsonUtil.fromCbor( messenger.consume( data ), HeartbeatResponseDto.class );
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
        // Environment Data     GET /rest/v1/environments/{environment-id}

        Matcher environmentDataMatcher = ENVIRONMENT_DATA_PATTERN.matcher( link );
        if ( environmentDataMatcher.matches() )
        {
            EnvironmentDto environmentDto = getEnvironmentData( link );
            processEnvironmentData( environmentDto );
        }

        // PeerProduct Data GET /rest/v1/peers/{peer-id}/products/{product-id}

        Matcher productDataMatcher = PRODUCT_DATA_PATTERN.matcher( link );
        if ( productDataMatcher.matches() )
        {
            PeerProductDataDto peerProductDataDTO = getPeerProductDto( link );
            try
            {
                processPeerProductData( peerProductDataDTO );
            }
            catch ( UnrecoverableKeyException | IOException | KeyStoreException | NoSuchAlgorithmException e )
            {
                e.printStackTrace();
            }
        }

        else
        {
            LOG.warn( "Unknown state link: " + link );
        }
    }


    private void processPeerProductData( final PeerProductDataDto peerProductDataDTO )
            throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, HIntegrationException,
            IOException
    {
        switch ( peerProductDataDTO.getState() )
        {
            case INSTALL:
                installingProcess( peerProductDataDTO );
                break;

            case REMOVE:
                removingProcess( peerProductDataDTO );
                break;
            case INSTALLED:
                break;
        }
    }


    private void removingProcess( final PeerProductDataDto peerProductDataDTO )
            throws HIntegrationException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException
    {
        // remove file from deploy package
        ProductDto productDTO = getProductDataDTO( peerProductDataDTO.getProductId() );
        File file = new File( String.format( PATH_TO_DEPLOY + "/%s.kar", productDTO.getName() ) );
        if ( file.delete() )
        {
            LOG.debug( file.getName() + " is deleted." );
            String removePath =
                    String.format( "/rest/v1/peers/%s/products/%s", peerId, peerProductDataDTO.getProductId() );
            WebClient client = HttpClient
                    .createTrustedWebClientWithAuth( baseUrl + removePath, keyStore, "subutai".toCharArray(),
                            serverFingerprint );

            Response r = client.delete();

            if ( r.getStatus() == HttpStatus.SC_NO_CONTENT )
            {
                LOG.debug( "Status: " + "no content" );
            }
        }
    }


    private void installingProcess( final PeerProductDataDto peerProductDataDTO )
            throws IOException, HIntegrationException, UnrecoverableKeyException, NoSuchAlgorithmException,
            KeyStoreException
    {
        ProductDto productDTO = getProductDataDTO( peerProductDataDTO.getProductId() );
        JSONObject jsonObject = null;
        String url = null;
        try
        {
            jsonObject = new JSONObject( productDTO.getMetadata() );
            url = jsonObject.getString( "url" );
        }
        catch ( JSONException e )
        {
            e.printStackTrace();
        }

        // downloading plugin
        File file = new File( String.format( PATH_TO_DEPLOY + "/%s.kar", productDTO.getName() ) );
        URL website = new URL( url );

        FileUtils.copyURLToFile( website, file );

        // update status
        peerProductDataDTO.setState( PeerProductDataDto.State.INSTALLED );
        updatePeerProductData( peerProductDataDTO );
    }


    private void updatePeerProductData( final PeerProductDataDto peerProductDataDTO )
            throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, HIntegrationException
    {
        LOG.debug( "Sending update : " + peerProductDataDTO );
        String updatePath = String.format( "/rest/v1/peers/%s/products/%s", peerId, peerProductDataDTO.getProductId() );

        try
        {
            WebClient client = HttpClient
                    .createTrustedWebClientWithAuth( baseUrl + updatePath, keyStore, "subutai".toCharArray(),
                            serverFingerprint );

            byte[] plainData = JsonUtil.toCbor( peerProductDataDTO );
            byte[] encryptedData = messenger.produce( plainData );
            Response r = client.put( encryptedData );
            if ( r.getStatus() != HttpStatus.SC_NO_CONTENT )
            {
                LOG.warn( "Unexpected response: " + r.readEntity( String.class ) );
            }
        }
        catch ( PGPException |
                JsonProcessingException e )
        {
            throw new HIntegrationException( "Could not send environment peer data.", e );
        }
    }


    private ProductDto getProductDataDTO( final UUID productId ) throws HIntegrationException
    {
        ProductDto result = null;
        String path = String.format( "/rest/v1/marketplace/products/%s", productId );
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

            byte[] plainContent = messenger.consume( encryptedContent );
            result = JsonUtil.fromCbor( plainContent, ProductDto.class );
            LOG.debug( "ProductDataDTO: " + result.toString() );
            return result;
        }
        catch ( UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException | PGPException | IOException
                e )
        {
            throw new HIntegrationException( "Could not retrieve environment data", e );
        }
    }


    private PeerProductDataDto getPeerProductDto( final String link ) throws HIntegrationException
    {
        try
        {
            WebClient client = HttpClient
                    .createTrustedWebClientWithAuth( baseUrl + link, keyStore, "subutai".toCharArray(),
                            serverFingerprint );

            Response r = client.get();
            PeerProductDataDto result = null;

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
            result = JsonUtil.fromCbor( plainContent, PeerProductDataDto.class );
            LOG.debug( "PeerProductDataDTO: " + result.toString() );
            return result;
        }
        catch ( UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException | PGPException | IOException
                e )
        {
            throw new HIntegrationException( "Could not retrieve environment data", e );
        }
    }


    private EnvironmentDto getEnvironmentData( String link ) throws HIntegrationException
    {
        try
        {
            WebClient client = HttpClient
                    .createTrustedWebClientWithAuth( baseUrl + link, keyStore, "subutai".toCharArray(),
                            serverFingerprint );

            Response r = client.get();
            EnvironmentDto result = null;

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
            result = JsonUtil.fromCbor( plainContent, EnvironmentDto.class );
            LOG.debug( "EnvironmentDto: " + result.toString() );
            return result;
        }
        catch ( UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException | PGPException | IOException
                e )
        {
            throw new HIntegrationException( "Could not retrieve environment data", e );
        }
    }


    private void processEnvironmentData( final EnvironmentDto environmentDto ) throws HIntegrationException
    {

        if ( environmentDto.getState() == EnvironmentDto.State.INITIALIZING )
        {
            //GET rest/v1/environments/{environment-id}/peers/{peer-id}/package

            EnvironmentPeerDataDto dto = processInitializing( environmentDto );
            LOG.debug( String.format( "%s", dto ) );
            if ( dto != null )
            {
                sendEnvironmentPeerData( environmentDto.getId(), dto );
            }
        }
        else if ( environmentDto.getState() == EnvironmentDto.State.RENDEZVOUS )
        {
            EnvironmentPeerDataDto dto = processRendevouse( environmentDto );
            LOG.debug( String.format( "%s", dto ) );
            sendEnvironmentPeerData( environmentDto.getId(), dto );
        }
    }


    private EnvironmentPeerDataDto processRendevouse( EnvironmentDto environmentDto ) throws HIntegrationException
    {
        EnvironmentPeerDataDto dto = new EnvironmentPeerDataDto();

        try
        {
            String path = String.format( "/rest/v1/environments/%s/peers/%s/package", environmentDto.getId(), peerId );
            EnvironmentPackageDto packageDto = getEnvironmentPackageData( path, environmentDto );
            LOG.debug( String.format( "Environment package: %s", packageDto ) );


            EnvironmentPeerDataDto currentPeerData = null;
            for ( EnvironmentPeerDataDto peerData : packageDto.getPeers() )
            {
                LOG.debug( String.format( "%s", peerData ) );

                if ( peerData.getPeerId().equals( peerId ) )
                {
                    currentPeerData = peerData;
                    break;
                }
            }

            if ( currentPeerData == null )
            {
                throw new HIntegrationException( "Peer data not found." );
            }

            SubnetUtils.SubnetInfo subnetInfo =
                    new SubnetUtils( currentPeerData.getCommunityIP(), N2NUtil.N2N_SUBNET_MASK ).getInfo();

            N2NConfig config = new N2NConfig( peerId, SUPERNODE_SERVER, SUPERNODE_PORT,
                    N2NUtil.generateInterfaceName( subnetInfo.getNetworkAddress() ),
                    N2NUtil.generateCommunityName( subnetInfo.getNetworkAddress() ), currentPeerData.getCommunityIP(),
                    packageDto.getSharedSecret() );

            peerManager.getLocalPeer().setupN2NConnection( config );
            //TODO use String instead of UUID
            dto.setPeerId( UUID.fromString( peerId ) );
            dto.setState( EnvironmentPeerDataDto.State.READY );
        }
        catch ( Exception e )
        {
            dto.setState( EnvironmentPeerDataDto.State.REFUSED );
            LOG.error( e.getMessage(), e );
        }

        return dto;
    }


    private EnvironmentPeerDataDto findNotAcceptedPeerIndex( final List<EnvironmentPeerDataDto> environmentPeers )
            throws HIntegrationException
    {
        int index = 0;
        while ( index < environmentPeers.size()
                && environmentPeers.get( index ).getState() == EnvironmentPeerDataDto.State.ACCEPTED )
        {
            index++;
        }

        if ( index == environmentPeers.size() )
        {
            throw new HIntegrationException( "All peers already accepted." );
        }
        return environmentPeers.get( index );
    }


    private EnvironmentPeerDataDto processInitializing( final EnvironmentDto environmentDto )
            throws HIntegrationException
    {
        EnvironmentPeerDataDto notAcceptedPeer = findNotAcceptedPeerIndex( environmentDto.getPeers() );

        if ( !notAcceptedPeer.getPeerId().equals( peerId ) )
        {
            return null;
        }


        EnvironmentPeerDataDto result = new EnvironmentPeerDataDto();
        //todo use String instead of UUID
        result.setPeerId( UUID.fromString( peerId ) );
        try
        {
            Set<String> excludedAdresses = new HashSet<>();
            for ( EnvironmentPeerDataDto peerData : environmentDto.getPeers() )
            {
                LOG.debug( String.format( "%s", peerData ) );
                if ( peerData.getCommunityIP() != null )
                {
                    excludedAdresses.add( peerData.getCommunityIP() );
                }
            }

            String address;
            if ( excludedAdresses.isEmpty() )
            {
                Set<String> excludedSubnets = getEnvironmentSubnets();
                String freeSubnet = N2NUtil.findFreeTunnelNetwork( excludedSubnets );
                address = freeSubnet.replaceAll( ".\\d$", ".1" );
            }
            else
            {
                String subnet = new SubnetUtils( excludedAdresses.iterator().next(), N2NUtil.N2N_SUBNET_MASK ).getInfo()
                                                                                                              .getNetworkAddress();
                if ( isSubnetFree( subnet ) )
                {
                    address = N2NUtil.findFreeAddress( subnet, excludedAdresses );
                }
                else
                {
                    throw new HIntegrationException( String.format( "Subnet %s already used.", subnet ) );
                }
            }

            TrustDataDto trustDataDto = new TrustDataDto( PGPKeyUtil.getKeyId( peerPublicKey.getFingerprint() ),
                    PGPKeyUtil.getKeyId( ownerPublicKey.getFingerprint() ), TrustDataDto.TrustLevel.FULL );

            String pekId = String.format( "%s-%s", peerId, environmentDto.getId() );

            KeyPair kp = securityManager.getKeyManager().generateKeyPair( pekId, false );
            securityManager.getKeyManager().saveKeyPair( pekId, ( short ) 2, kp );

            PGPPublicKey pekPublicKey = securityManager.getKeyManager().getPublicKey( pekId );

            String pek = PGPKeyUtil.exportAscii( pekPublicKey );

            LOG.debug( String.format( "PEK fingerprint: %s. %s",
                    PGPKeyUtil.getFingerprint( pekPublicKey.getFingerprint() ),
                    pek.equals( securityManager.getKeyManager().getPublicKeyRingAsASCII( pekId ) ) ) );
            result.setCommunityIP( address );
            result.setTrustData( trustDataDto );
            result.setState( EnvironmentPeerDataDto.State.ACCEPTED );
            result.setPEK( pek );
        }
        catch ( Exception e )
        {
            result.setState( EnvironmentPeerDataDto.State.REFUSED );
            LOG.error( e.getMessage(), e );
        }
        return result;
    }


    private boolean isSubnetFree( final String subnet )
    {
        return !getEnvironmentSubnets().contains( subnet );
    }


    private Set<String> getEnvironmentSubnets()
    {
        Set<Interface> r = peerManager.getLocalPeer().getNetworkInterfaces( N2NUtil.N2N_SUBNET_INTERFACES_PATTERN );

        Collection peerSubnets = CollectionUtils.collect( r, new Transformer()
        {
            @Override
            public Object transform( final Object o )
            {
                Interface i = ( Interface ) o;
                SubnetUtils u = new SubnetUtils( i.getIp(), N2NUtil.N2N_SUBNET_MASK );
                return u.getInfo().getNetworkAddress();
            }
        } );


        return new HashSet<String>( peerSubnets );
    }


    //    PUT rest/v1/environments/{environment-id}/peers/{peer-id}
    private void sendEnvironmentPeerData( UUID environmentId, EnvironmentPeerDataDto environmentPeerDataDto )
            throws HIntegrationException
    {
        LOG.debug( "Sending: " + environmentPeerDataDto );
        String path =
                String.format( "/rest/v1/environments/%s/peers/%s", environmentId, environmentPeerDataDto.getPeerId() );
        try
        {
            WebClient client = HttpClient
                    .createTrustedWebClientWithAuth( baseUrl + path, keyStore, "subutai".toCharArray(),
                            serverFingerprint );

            byte[] plainData = JsonUtil.toCbor( environmentPeerDataDto );
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


    private EnvironmentPackageDto getEnvironmentPackageData( String path, EnvironmentDto environmentDto )
            throws HIntegrationException
    {
        EnvironmentPackageDto result = null;

        try
        {
            // Environment Package  GET /rest/v1/environments/{environment-id}/peers/{peer-id}/package
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

            byte[] peerEncryptedContent = readContent( r );

            byte[] pekEncryptedContent = messenger.consume( peerEncryptedContent );

            KeyManager keyManager = securityManager.getKeyManager();
            String pekId = String.format( "%s-%s", peerId, environmentDto.getId() );

            PGPPrivateKey pekPrivateKey = keyManager.getPrivateKey( pekId );

            if ( pekPrivateKey == null )
            {
                throw new KeyStoreException( "PEK private key not found." );
            }
            PGPPublicKey pekPublicKey = keyManager.getPublicKey( pekId );

            LOG.debug( String.format( "PEK fingerprint: %s",
                    PGPKeyUtil.getFingerprint( pekPublicKey.getFingerprint() ) ) );

            byte[] plainContent = PGPDecrypt.decrypt( pekEncryptedContent, pekPrivateKey );

            result = JsonUtil.fromCbor( plainContent, EnvironmentPackageDto.class );
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


    public void setIntegrationEnabled( final boolean integrationEnabled )
    {
        this.integrationEnabled = integrationEnabled;
    }
}
