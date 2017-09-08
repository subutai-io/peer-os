package io.subutai.core.hubmanager.impl.processor;


import java.io.File;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.io.FileUtils;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.http.HttpStatus;

import com.google.common.base.Preconditions;

import io.subutai.common.security.utils.SafeCloseUtil;
import io.subutai.common.settings.Common;
import io.subutai.common.util.CollectionUtil;
import io.subutai.common.util.RestUtil;
import io.subutai.core.hubmanager.api.RestClient;
import io.subutai.core.hubmanager.api.RestResult;
import io.subutai.core.hubmanager.api.StateLinkProcessor;
import io.subutai.core.hubmanager.api.exception.HubManagerException;
import io.subutai.core.hubmanager.impl.ConfigManager;
import io.subutai.hub.share.common.HubEventListener;
import io.subutai.hub.share.dto.PeerProductDataDto;
import io.subutai.hub.share.dto.product.ProductDtoV1_2;


public class ProductProcessor implements StateLinkProcessor
{
    private static final Logger LOG = LoggerFactory.getLogger( ProductProcessor.class.getName() );
    private ConfigManager configManager;
    private Set<HubEventListener> hubEventListeners = new HashSet<>();

    private static final Pattern PRODUCT_DATA_PATTERN = Pattern.compile( "/rest/v1/peers/[a-zA-z0-9]{1,100}/products/"
            + "([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})" );

    private static final String PATH_TO_DEPLOY = String.format( "%s/deploy", System.getProperty( "karaf.home" ) );
    private RestClient restClient;


    public ProductProcessor( final ConfigManager hConfigManager, final Set<HubEventListener> hubEventListeners,
                             final RestClient restClient )
    {
        this.configManager = hConfigManager;
        this.hubEventListeners = hubEventListeners != null ? hubEventListeners : new HashSet<HubEventListener>();
        this.restClient = restClient;
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
            RestResult<PeerProductDataDto> restResult = restClient.get( link, PeerProductDataDto.class );

            LOG.debug( "Sending request for getting PeerProductDTO..." );

            if ( restResult.getStatus() == HttpStatus.SC_NO_CONTENT )
            {
                return null;
            }

            if ( restResult.getStatus() != HttpStatus.SC_OK )
            {
                LOG.error( restResult.getError() );
                return null;
            }

            Preconditions.checkNotNull( restResult.getEntity() );

            LOG.debug( "PeerProductDataDTO: " + restResult.getEntity().toString() );

            return restResult.getEntity();
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
            default:
                LOG.info( "Requested {}", peerProductDataDTO.getState() );
                break;
        }
    }


    private void installingProcess( final PeerProductDataDto peerProductDataDTO ) throws HubManagerException
    {
        LOG.debug( "Installing Product to Local Peer..." );
        ProductDtoV1_2 productDTO = getProductDataDTO( peerProductDataDTO.getProductId() );

        InputStream initialStream = null;
        try
        {
            // install dependencies first (if plugin has dependencies)
            assert productDTO != null;
            for ( String dependencyId : productDTO.getDependencies() )
            {
                PeerProductDataDto _peerProductDataDto = getPeerProductDto( getProductProcessUrl( dependencyId ) );
                assert _peerProductDataDto != null;
                if ( _peerProductDataDto.getState() != PeerProductDataDto.State.INSTALLED )
                {
                    installingProcess( _peerProductDataDto );
                }
            }

            //Assume plugin has 1 file, all other files should go as dependencies
            String url = productDTO.getMetadata().iterator().next();

            //Using WebClient directly here since we need to close it only after request is processed
            WebClient webClient = null;
            try
            {
                webClient = RestUtil.createTrustedWebClient( url );

                File product = webClient.get( File.class );
                initialStream = FileUtils.openInputStream( product );
                File targetFile = new File( PATH_TO_DEPLOY + "/" + productDTO.getName() + ".kar" );
                FileUtils.copyInputStreamToFile( initialStream, targetFile );
            }
            catch ( Exception e )
            {
                LOG.error( "Error downloading plugin", e );

                throw e;
            }
            finally
            {
                SafeCloseUtil.close( initialStream );
                RestUtil.close( webClient );
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

        // TODO: check that there are no installed plugins, that depend on this plugin
        assert productDTO != null;
        File file = new File( PATH_TO_DEPLOY + "/" + productDTO.getName() + ".kar" );

        if ( file.delete() || !file.exists() )
        {
            LOG.debug( file.getName() + " is removed." );

            LOG.debug( " Product uninstalled successfully." );
            deletePeerProductData( peerProductDataDTO );

            // remove info about installation into DB
            notifyPluginEventListeners( peerProductDataDTO.getProductId(), peerProductDataDTO.getState() );
        }
    }


    private ProductDtoV1_2 getProductDataDTO( final String productId ) throws HubManagerException
    {
        ProductDtoV1_2 result;

        String path = String.format( "/rest/v1/marketplace/products/%s", productId );
        try
        {
            RestResult<String> restResult = restClient.getPlain( path, String.class );

            if ( restResult.getStatus() == HttpStatus.SC_NO_CONTENT )
            {
                return null;
            }

            if ( restResult.getStatus() != HttpStatus.SC_OK )
            {
                LOG.error( restResult.getError() );
                return null;
            }

            String jsonString = restResult.getEntity();
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


    private String getProductProcessUrl( String productId )
    {
        return String.format( "/rest/v1/peers/%s/products/%s", configManager.getPeerId(), productId );
    }


    public void updatePeerProductData( final PeerProductDataDto peerProductDataDTO ) throws HubManagerException
    {
        LOG.debug( "Sending update : " + peerProductDataDTO );
        String updatePath = getProductProcessUrl( peerProductDataDTO.getProductId() );

        try
        {
            RestResult<Object> restResult = restClient.put( updatePath, peerProductDataDTO, Object.class );

            if ( restResult.getStatus() != HttpStatus.SC_NO_CONTENT )
            {
                LOG.warn( "Unexpected response: " + restResult.getError() );
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

        RestResult<Object> restResult = restClient.delete( removePath );

        if ( restResult.getStatus() == HttpStatus.SC_NO_CONTENT )
        {
            LOG.debug( "Status: no content" );
        }
    }


    private void notifyPluginEventListeners( final String pluginUid, final PeerProductDataDto.State state )
    {
        if ( !CollectionUtil.isCollectionEmpty( hubEventListeners ) )
        {
            ExecutorService notifier =
                    Executors.newFixedThreadPool( Math.min( Common.MAX_EXECUTOR_SIZE, hubEventListeners.size() ) );

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
