package io.subutai.core.hubmanager.impl;


import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.Response;

import io.subutai.core.hubmanager.impl.model.ConfigEntity;
import io.subutai.core.hubmanager.impl.proccessors.*;

import org.bouncycastle.openpgp.PGPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.io.FileUtils;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;

import io.subutai.common.dao.DaoManager;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.hubmanager.api.HubPluginException;
import io.subutai.core.hubmanager.api.Integration;
import io.subutai.core.hubmanager.api.StateLinkProccessor;
import io.subutai.core.hubmanager.api.dao.ConfigDataService;
import io.subutai.core.hubmanager.api.model.Config;
import io.subutai.core.hubmanager.impl.dao.ConfigDataServiceImpl;
import io.subutai.core.hubmanager.impl.environment.EnvironmentBuilder;
import io.subutai.core.hubmanager.impl.environment.EnvironmentDestroyer;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.metric.api.Monitor;
import io.subutai.core.network.api.NetworkManager;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.security.api.SecurityManager;
import io.subutai.hub.share.dto.PeerDto;
import io.subutai.hub.share.dto.product.ProductsDto;
import io.subutai.hub.share.json.JsonUtil;


public class IntegrationImpl implements Integration
{
    private static final long TIME_15_MINUTES = 900;

    private static final Logger LOG = LoggerFactory.getLogger( IntegrationImpl.class );

    private SecurityManager securityManager;
    private EnvironmentManager environmentManager;
    private PeerManager peerManager;
    private ConfigManager configManager;

    private ScheduledExecutorService hearbeatExecutorService = Executors.newSingleThreadScheduledExecutor();

    private ScheduledExecutorService resourceHostConfExecutorService = Executors.newSingleThreadScheduledExecutor();

    private ScheduledExecutorService resourceHostMonitorExecutorService = Executors.newSingleThreadScheduledExecutor();

    private ScheduledExecutorService hubLoggerExecutorService = Executors.newSingleThreadScheduledExecutor();

    private ScheduledExecutorService containerEventExecutor = Executors.newSingleThreadScheduledExecutor();


    private HeartbeatProcessor heartbeatProcessor;
    private ResourceHostConfProcessor resourceHostConfProcessor;
    private SystemConfProcessor systemConfProcessor;
    private ResourceHostMonitorProcessor resourceHostMonitorProcessor;
    private HubLoggerProcessor hubLoggerProcessor;

    private DaoManager daoManager;
    private ConfigDataService configDataService;
    private Monitor monitor;
    private IdentityManager identityManager;
    private HubEnvironmentManager hubEnvironmentManager;
    private NetworkManager networkManager;

    private ContainerEventProcessor containerEventProcessor;

    private EnvironmentBuilder envBuilder;

    private EnvironmentDestroyer envDestroyer;
    private ScheduledExecutorService sumChecker = Executors.newSingleThreadScheduledExecutor();
    private String checksum = "";


    public IntegrationImpl( DaoManager daoManager )
    {
        this.daoManager = daoManager;
    }


    public void init()
    {
        try
        {
            configDataService = new ConfigDataServiceImpl( daoManager );

            configManager = new ConfigManager( securityManager, peerManager, configDataService );

            hubEnvironmentManager =
                    new HubEnvironmentManager( environmentManager, configManager, peerManager, identityManager,
                            networkManager );

            heartbeatProcessor = new HeartbeatProcessor( this, configManager );

            resourceHostConfProcessor = new ResourceHostConfProcessor( this, peerManager, configManager, monitor );

            hubLoggerProcessor = new HubLoggerProcessor( configManager, this );

            resourceHostMonitorProcessor =
                    new ResourceHostMonitorProcessor( this, peerManager, configManager, monitor );

            StateLinkProccessor systemConfProcessor = new SystemConfProcessor( configManager );

            StateLinkProccessor hubEnvironmentProccessor =
                    new HubEnvironmentProccessor( hubEnvironmentManager, configManager, peerManager );

            heartbeatProcessor.addProccessor( hubEnvironmentProccessor );
            heartbeatProcessor.addProccessor( systemConfProcessor );

            hearbeatExecutorService.scheduleWithFixedDelay( heartbeatProcessor, 10, 120, TimeUnit.SECONDS );

            resourceHostConfExecutorService
                    .scheduleWithFixedDelay( resourceHostConfProcessor, 20, TIME_15_MINUTES, TimeUnit.SECONDS );

            resourceHostMonitorExecutorService
                    .scheduleWithFixedDelay( resourceHostMonitorProcessor, 30, 300, TimeUnit.SECONDS );

            containerEventProcessor = new ContainerEventProcessor( this, configManager, peerManager );

            containerEventExecutor
                    .scheduleWithFixedDelay( containerEventProcessor, 30, TIME_15_MINUTES, TimeUnit.SECONDS );

            hubLoggerExecutorService.scheduleWithFixedDelay( hubLoggerProcessor, 40, 3600, TimeUnit.SECONDS );


            //            envBuilder = new EnvironmentBuilder( peerManager.getLocalPeer() );
            //
            //            envDestroyer = new EnvironmentDestroyer( peerManager.getLocalPeer() );
            this.sumChecker.scheduleWithFixedDelay( new Runnable()
            {
                @Override
                public void run()
                {
                    LOG.info( "Starting sumchecker" );
                    generateChecksum();
                }
            }, 1, 3600000, TimeUnit.MILLISECONDS );
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage() );
        }
    }


    public void destroy()
    {
        hearbeatExecutorService.shutdown();
        resourceHostConfExecutorService.shutdown();
        resourceHostMonitorExecutorService.shutdown();
    }


    @Override
    public void sendHeartbeat() throws HubPluginException
    {
        heartbeatProcessor.sendHeartbeat();

        resourceHostConfProcessor.sendResourceHostConf();

        containerEventProcessor.process();

        //        envBuilder.test();

        //        envDestroyer.test();
    }


    @Override
    public void sendResourceHostInfo() throws HubPluginException
    {
        resourceHostConfProcessor.sendResourceHostConf();
    }


    @Override
    public void registerPeer( String hupIp, String email, String password ) throws HubPluginException
    {

        // todo revert

        configManager.addHubConfig( hupIp );

        RegistrationManager registrationManager = new RegistrationManager( this, configManager, hupIp );

        registrationManager.registerPeer( email, password );

        generateChecksum();
    }


    @Override
    public String getHubDns() throws HubPluginException
    {
        Config config = getConfigDataService().getHubConfig( configManager.getPeerId() );

        if ( config != null )
        {
            return config.getHubIp();
        }
        else
        {
            return null;
        }
    }


    @Override
    public String getProducts() throws HubPluginException
    {
        ProductsDto result;
        try
        {
            //String hubIp = configDataService.getHubConfig( configManager.getPeerId() ).getHubIp();
            WebClient client = configManager.getTrustedWebClientWithAuth( "/rest/v1/marketplace/products", "hub.subut.ai" );

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

            byte[] encryptedContent = configManager.readContent( r );
            ObjectMapper mapper = createMapper( new CBORFactory() );

            byte[] plainContent = configManager.getMessenger().consume( encryptedContent );
            result = mapper.readValue( plainContent, ProductsDto.class );
            String output = JsonUtil.toJson( result );
            LOG.debug( "ProductsDataDTO: " + result.toString() );
            return output;
        }
        catch ( UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException | PGPException | IOException
                e )
        {
            e.printStackTrace();
            throw new HubPluginException( "Could not retrieve product data", e );
        }
    }


    @Override
    public void installPlugin( String url ) throws HubPluginException
    {
        try
        {
            int indexOfStr = url.indexOf( "/package/" );
            String fileName = url.substring( indexOfStr + 9, url.length() );
            File file = new File( String.format( "%s/deploy", System.getProperty( "karaf.home" ) ) + "/" + fileName );
            URL website = new URL( url );
            FileUtils.copyURLToFile( website, file );
        }
        catch ( IOException e )
        {
            throw new HubPluginException( "Could not install plugin", e );
        }
        LOG.debug( "Product installed successfully..." );
    }


    @Override
    public void uninstallPlugin( final String url, final String name )
    {
        int indexOfStr = url.indexOf( "/package/" );
        String fileName = url.substring( indexOfStr + 9, url.length() );
        File file = new File( String.format( "%s/deploy", System.getProperty( "karaf.home" ) ) + "/" + fileName );
        File repo = new File( "/opt/subutai-mng/system/io/subutai/" );
        File[] dirs = repo.listFiles( new FileFilter()
        {
            @Override
            public boolean accept( File pathname )
            {
                return pathname.getName().matches( ".*" + name + ".*" );
            }
        } );
        if ( dirs != null )
        {
            for ( File f : dirs )
            {
                LOG.info( f.getAbsolutePath() );
                try
                {
                    FileUtils.deleteDirectory( f );
                    LOG.debug( f.getName() + " is removed." );
                }
                catch ( IOException e )
                {
                    e.printStackTrace();
                }
            }
        }
        if ( file.delete() )
        {
            LOG.debug( file.getName() + " is removed." );
        }
        LOG.debug( "Product uninstalled successfully..." );
    }


    @Override
    public void unregisterPeer() throws HubPluginException
    {
        try
        {
            String hubIp = configDataService.getHubConfig( configManager.getPeerId() ).getHubIp();
            String path = String.format( "/rest/v1/peers/%s/delete", configManager.getPeerId() );

            WebClient client = configManager.getTrustedWebClientWithAuth( path, hubIp );

            Response r = client.delete();


            if ( r.getStatus() == HttpStatus.SC_NO_CONTENT )
            {
                LOG.debug( "Peer unregistered successfully." );
                configDataService.deleteConfig( configManager.getPeerId() );
            }
            else
            {
                LOG.error( r.readEntity( String.class ) );
                throw new HubPluginException( "Could not unregister peer" );
            }
        }
        catch ( UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException e )
        {
            throw new HubPluginException( "Could not unregister peer", e );
        }
    }


    @Override
    public boolean getRegistrationState()
    {
        return getConfigDataService().getHubConfig( configManager.getPeerId() ) != null;
    }


    @Override
    public Map<String, String> getPeerInfo() throws HubPluginException
    {
        Map<String, String> result = new HashMap<>();
        try
        {
            String path = "/rest/v1/peers/" + configManager.getPeerId();

            WebClient client = configManager.getTrustedWebClientWithAuth( path, configManager.getHubIp() );

            Response r = client.get();

            if ( r.getStatus() == HttpStatus.SC_OK )
            {
                byte[] encryptedContent = configManager.readContent( r );
                byte[] plainContent = configManager.getMessenger().consume( encryptedContent );
                PeerDto dto = JsonUtil.fromCbor( plainContent, PeerDto.class );
                result.put( "OwnerId", dto.getOwnerId() );

                LOG.debug( "PeerDto: " + result.toString() );
            }
        }
        catch ( UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException | PGPException | IOException
                e )
        {
            throw new HubPluginException( "Could not retrieve Peer info", e );
        }
        return result;
    }


    @Override
    public Config getHubConfiguration()
    {
        return configDataService.getHubConfig( configManager.getPeerId() );
    }


    public void setEnvironmentManager( final EnvironmentManager environmentManager )
    {
        this.environmentManager = environmentManager;
    }


    public void setSecurityManager( final SecurityManager securityManager )
    {
        this.securityManager = securityManager;
    }


    public void setPeerManager( final PeerManager peerManager )
    {
        this.peerManager = peerManager;
    }


    public ConfigDataService getConfigDataService()
    {
        return configDataService;
    }


    public void setConfigDataService( final ConfigDataService configDataService )
    {
        this.configDataService = configDataService;
    }


    public void setMonitor( Monitor monitor )
    {
        this.monitor = monitor;
    }


    private static ObjectMapper createMapper( JsonFactory factory )
    {
        ObjectMapper mapper = new ObjectMapper( factory );
        mapper.setVisibility( PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY );
        return mapper;
    }


    public void setIdentityManager( final IdentityManager identityManager )
    {
        this.identityManager = identityManager;
    }


    public void setNetworkManager( final NetworkManager networkManager )
    {
        this.networkManager = networkManager;
    }


    private void generateChecksum()
	{
/*		if (getRegistrationState ())
		{*/
			try
			{
				LOG.info ("Generating plugins list md5 checksum");
				String productList = getProducts ();
				MessageDigest md = MessageDigest.getInstance ("MD5");
				byte[] bytes = md.digest (productList.getBytes ("UTF-8"));
				StringBuilder hexString = new StringBuilder ();

                for ( int i = 0; i < bytes.length; i++ )
                {
                    String hex = Integer.toHexString( 0xFF & bytes[i] );
                    if ( hex.length() == 1 )
                    {
                        hexString.append( '0' );
                    }
                    hexString.append( hex );
                }

				checksum = hexString.toString ();
				LOG.info ("Checksum generated: " + checksum);
			}
			catch (NoSuchAlgorithmException | UnsupportedEncodingException | HubPluginException e)
			{
				LOG.error (e.getMessage ());
				e.printStackTrace ();
			}
/*		}
		else
		{
			LOG.info ("Peer not registered. Trying again in 1 hour.");
		}*/
	}


    @Override
    public String getChecksum()
    {
        return this.checksum;
    }
}