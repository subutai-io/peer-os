package io.subutai.core.hubmanager.impl.processor;


import java.io.File;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.core.Response;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.io.FileUtils;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.http.HttpStatus;

import com.google.common.base.Preconditions;

import io.subutai.common.util.CollectionUtil;
import io.subutai.common.util.RestUtil;
import io.subutai.core.hubmanager.api.StateLinkProcessor;
import io.subutai.core.hubmanager.api.exception.HubManagerException;
import io.subutai.core.hubmanager.impl.ConfigManager;
import io.subutai.hub.share.common.HubEventListener;
import io.subutai.hub.share.dto.PeerProductDataDto;
import io.subutai.hub.share.dto.product.ProductDtoV1_2;
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
        this.hubEventListeners = hubEventListeners != null ? hubEventListeners : new HashSet<HubEventListener>();
    }


    @Override
    public boolean processStateLinks( final Set<String> stateLinks ) throws HubManagerException
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
                catch ( Exception e )
                {
                    LOG.warn( e.getMessage() );
                }
            }
        }

        return false;
    }


    private PeerProductDataDto getPeerProductDto( final String link ) throws HubManagerException
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

            Preconditions.checkNotNull( result );

            LOG.debug( "PeerProductDataDTO: " + result.toString() );

            return result;
        }
        catch ( Exception e )
        {
            throw new HubManagerException( "Could not retrieve environment data", e );
        }
    }


    private void processPeerProductData( final PeerProductDataDto peerProductDataDTO ) throws HubManagerException
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


    private void installingProcess( final PeerProductDataDto peerProductDataDTO ) throws HubManagerException
    {
        LOG.debug( "Installing Product to Local Peer..." );
        boolean isSuccess = false;
        ProductDtoV1_2 productDTO = getProductDataDTO( peerProductDataDTO.getProductId() );

        // install dependencies first (if plugin has dependencies)
        for ( String dependencyId : productDTO.getDependencies() )
        {
            PeerProductDataDto _peerProductDataDto = getPeerProductDto( getProductProcessUrl( dependencyId ) );
            if ( _peerProductDataDto.getState() != PeerProductDataDto.State.INSTALLED )
            {
                installingProcess( _peerProductDataDto );
            }
        }
        try
        {

            // downloading plugin files
            for ( String url : productDTO.getMetadata() )
            {
                WebClient webClient = RestUtil.createTrustedWebClient( url );

                File product = webClient.get( File.class );
                InputStream initialStream = FileUtils.openInputStream( product );
                File targetFile = new File( PATH_TO_DEPLOY + "/" + productDTO.getName() + ".kar" );
                FileUtils.copyInputStreamToFile( initialStream, targetFile );
            }
        }
        catch ( Exception e )
        {
            throw new HubManagerException( e );
        }

        LOG.debug( "Product installed successfully..." );

        // update status
        peerProductDataDTO.setState( PeerProductDataDto.State.INSTALLED );
        updatePeerProductData( peerProductDataDTO );

        // save info about installation into DB
        notifyPluginEventListeners( peerProductDataDTO.getProductId(), peerProductDataDTO.getState() );
    }


    private void removingProcess( final PeerProductDataDto peerProductDataDTO ) throws HubManagerException
    {
        // remove file from deploy package
        LOG.debug( "Removing product from Local Peer..." );
        ProductDtoV1_2 productDTO = getProductDataDTO( peerProductDataDTO.getProductId() );
        int deleteFileCounter = 0;

        // TODO: check that there is no installed plugins, which depends on this plugin

        for ( String url : productDTO.getMetadata() )
        {
            File file = new File( PATH_TO_DEPLOY + "/" + productDTO.getName() + ".kar" );
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


    private ProductDtoV1_2 getProductDataDTO( final String productId ) throws HubManagerException
    {
        ProductDtoV1_2 result = null;
        String path = String.format( "/rest/v1.2/marketplace/products/%s", productId );
        try
        {
            WebClient client = configManager.getTrustedWebClientWithAuth( path, configManager.getHubIp() );

            Response r = client.get();


            if ( r.getStatus() == HttpStatus.SC_NO_CONTENT )
            {
                return null;
            }

            if ( r.getStatus() != HttpStatus.SC_OK )
            {
                LOG.error( r.readEntity( String.class ) );
                return null;
            }

            String jsonString = r.readEntity( String.class );
            JSONObject jsonObj = new JSONObject( jsonString );
            result = new ProductDtoV1_2( jsonObj );

            LOG.debug( "ProductDataDTO: " + result.toString() );
            return result;
        }
        catch ( Exception e )
        {
            throw new HubManagerException( "Could not retrieve product data", e );
        }
    }


    public String getProductProcessUrl( String productId )
    {
        return String.format( "/rest/v1/peers/%s/products/%s", configManager.getPeerId(), productId );
    }


    public void updatePeerProductData( final PeerProductDataDto peerProductDataDTO ) throws HubManagerException
    {
        LOG.debug( "Sending update : " + peerProductDataDTO );
        String updatePath = getProductProcessUrl( peerProductDataDTO.getProductId() );

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
        catch ( Exception e )
        {
            throw new HubManagerException( "Could not send product data.", e );
        }
    }


    public void deletePeerProductData( final PeerProductDataDto peerProductDataDto ) throws HubManagerException
    {
        String removePath = getProductProcessUrl( peerProductDataDto.getProductId() );

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
