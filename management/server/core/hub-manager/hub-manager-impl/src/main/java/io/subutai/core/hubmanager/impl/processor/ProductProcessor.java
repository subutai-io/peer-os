package io.subutai.core.hubmanager.impl.processor;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.core.Response;

import org.bouncycastle.openpgp.PGPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.io.FileUtils;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.http.HttpStatus;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.subutai.common.dao.DaoManager;
import io.subutai.common.util.CollectionUtil;
import io.subutai.common.util.RestUtil;
import io.subutai.core.hubmanager.api.HubManager;
import io.subutai.core.hubmanager.api.StateLinkProcessor;
import io.subutai.core.hubmanager.impl.ConfigManager;
import io.subutai.core.hubmanager.impl.HubManagerImpl;
import io.subutai.hub.share.common.HubEventListener;
import io.subutai.hub.share.dto.PeerProductDataDto;
import io.subutai.hub.share.dto.ProductDto;
import io.subutai.hub.share.json.JsonUtil;


// TODO: Replace WebClient with HubRestClient.
public class ProductProcessor implements StateLinkProcessor
{
    private static final Logger LOG = LoggerFactory.getLogger( ProductProcessor.class.getName() );
    private ConfigManager configManager;
    private Set<HubEventListener> hubEventListeners = new HashSet<>();

    private static final Pattern PRODUCT_DATA_PATTERN = Pattern.compile( "/rest/v1/peers/[a-zA-z0-9]{1,100}/products/"
            + "([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})" );

    private static final String PATH_TO_DEPLOY = String.format( "%s/deploy", System.getProperty( "karaf.home" ) );


    public ProductProcessor( final ConfigManager hConfigManager, final Set<HubEventListener> hubEventListeners )
    {
        this.configManager = hConfigManager;
        this.hubEventListeners = hubEventListeners != null? hubEventListeners : new HashSet<HubEventListener>();
    }


    @Override
    public boolean processStateLinks( final Set<String> stateLinks ) throws Exception
    {
        for ( String link : stateLinks )
        {
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
        }

        return false;
    }


    private PeerProductDataDto getPeerProductDto( final String link ) throws Exception
    {
        try
        {
            WebClient client = configManager.getTrustedWebClientWithAuth( link, configManager.getHubIp() );

            LOG.debug( "Sending request for getting PeerProductDTO..." );
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

            byte[] encryptedContent = configManager.readContent( r );
            byte[] plainContent = configManager.getMessenger().consume( encryptedContent );

            result = JsonUtil.fromCbor( plainContent, PeerProductDataDto.class );

            LOG.debug( "PeerProductDataDTO: " + result.toString() );

            return result;
        }
        catch ( UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException | PGPException | IOException
                e )
        {
            throw new Exception( "Could not retrieve environment data", e );
        }
    }


    private void processPeerProductData( final PeerProductDataDto peerProductDataDTO )
            throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, Exception, IOException
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


    private void installingProcess( final PeerProductDataDto peerProductDataDTO )
            throws IOException, Exception, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException
    {
        LOG.debug( "Installing Product to Local Peer..." );
        boolean isSuccess = false;
        ProductDto productDTO = getProductDataDTO( peerProductDataDTO.getProductId() );


        // downloading plugin files
        for ( String url : productDTO.getMetadata() )
        {
            int indexOfStr = url.indexOf( "/package/" );
            String fileName;
            if ( indexOfStr != -1 )
            {
                fileName = url.substring( indexOfStr + 9, url.length() );
            }
            else
            {
                fileName = productDTO.getId() + ".kar";
            }

            WebClient webClient = RestUtil.createTrustedWebClient( url );

            File product = webClient.get( File.class );
            InputStream initialStream = FileUtils.openInputStream( product );
            File targetFile = new File( PATH_TO_DEPLOY + "/" + fileName );
            FileUtils.copyInputStreamToFile( initialStream, targetFile );
        }

        LOG.debug( "Product installed successfully..." );

        // update status
        peerProductDataDTO.setState( PeerProductDataDto.State.INSTALLED );
        updatePeerProductData( peerProductDataDTO );

        // save info about installation into DB
        notifyPluginEventListeners( peerProductDataDTO.getProductId(), peerProductDataDTO.getState() );
    }


    private void removingProcess( final PeerProductDataDto peerProductDataDTO )
            throws Exception, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException
    {
        // remove file from deploy package
        LOG.debug( "Removing product from Local Peer..." );
        ProductDto productDTO = getProductDataDTO( peerProductDataDTO.getProductId() );
        int deleteFileCounter = 0;
        for ( String url : productDTO.getMetadata() )
        {
            int indexOfStr = url.indexOf( "/package/" );
            String fileName;
            if ( indexOfStr != -1 )
            {
                fileName = url.substring( indexOfStr + 9, url.length() );
            }
            else
            {
                fileName = productDTO.getId() + ".kar";
            }
            File file = new File( PATH_TO_DEPLOY + "/" + fileName );
            if ( file.delete() )
            {
                LOG.debug( file.getName() + " is removed." );
                deleteFileCounter++;
            }
        }

        if ( deleteFileCounter == productDTO.getMetadata().size() )
        {
            LOG.debug( " Product uninstalled successfully." );
            deletePeerProductData( peerProductDataDTO );

            // remove info about installation into DB
            notifyPluginEventListeners( peerProductDataDTO.getProductId(), peerProductDataDTO.getState() );
        }
    }


    private ProductDto getProductDataDTO( final String productId ) throws Exception
    {
        ProductDto result = null;
        String path = String.format( "/rest/v1/marketplace/products/%s", productId );
        try
        {
            WebClient client = configManager.getTrustedWebClientWithAuth( path, configManager.getHubIp() );

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

            byte[] encryptedContent = configManager.readContent( r );

            byte[] plainContent = configManager.getMessenger().consume( encryptedContent );
            result = JsonUtil.fromCbor( plainContent, ProductDto.class );
            LOG.debug( "ProductDataDTO: " + result.toString() );
            return result;
        }
        catch ( UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException | PGPException | IOException
                e )
        {
            throw new Exception( "Could not retrieve product data", e );
        }
    }


    public String getProductProcessUrl( String peerId, String productId )
    {
        return String.format( "/rest/v1/peers/%s/products/%s", peerId, productId );
    }

    public void updatePeerProductData( final PeerProductDataDto peerProductDataDTO )
            throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, Exception
    {
        LOG.debug( "Sending update : " + peerProductDataDTO );
        String updatePath = getProductProcessUrl( configManager.getPeerId(), peerProductDataDTO.getProductId() );

        try
        {
            WebClient client = configManager.getTrustedWebClientWithAuth( updatePath, configManager.getHubIp() );

            byte[] plainData = JsonUtil.toCbor( peerProductDataDTO );
            byte[] encryptedData = configManager.getMessenger().produce( plainData );
            Response r = client.put( encryptedData );
            if ( r.getStatus() != HttpStatus.SC_NO_CONTENT )
            {
                LOG.warn( "Unexpected response: " + r.readEntity( String.class ) );
            }
        }
        catch ( PGPException |
                JsonProcessingException e )
        {
            throw new Exception( "Could not send product data.", e );
        }
    }

    public void deletePeerProductData( final PeerProductDataDto peerProductDataDto )
            throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException
    {
        String removePath = getProductProcessUrl( configManager.getPeerId(), peerProductDataDto.getProductId() );

        WebClient client = configManager.getTrustedWebClientWithAuth( removePath, configManager.getHubIp() );

        Response r = client.delete();

        if ( r.getStatus() == HttpStatus.SC_NO_CONTENT )
        {
            LOG.debug( "Status: " + "no content" );
        }
    }

    public void notifyPluginEventListeners( final String pluginUid, final PeerProductDataDto.State state )
    {
        if ( !CollectionUtil.isCollectionEmpty( hubEventListeners ) )
        {
            ExecutorService notifier = Executors.newFixedThreadPool( hubEventListeners.size() );

            for ( final HubEventListener hubEventListener : hubEventListeners )
            {
                notifier.execute( new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            hubEventListener.onPluginEvent( pluginUid, state );
                        }
                        catch ( Exception e )
                        {
                            //ignore
                        }
                    }
                } );
            }

            notifier.shutdown();
        }
    }
}
