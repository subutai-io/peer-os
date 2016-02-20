package io.subutai.core.hubmanager.impl;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.Response;

import org.bouncycastle.openpgp.PGPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
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
import io.subutai.core.hubmanager.impl.dao.ConfigDataServiceImpl;
import io.subutai.core.hubmanager.impl.proccessors.EnvironmentProccessor;
import io.subutai.core.hubmanager.impl.proccessors.HeartbeatProcessor;
import io.subutai.core.hubmanager.impl.proccessors.ResourceHostConfProcessor;
import io.subutai.core.hubmanager.impl.proccessors.ResourceHostMonitorProcessor;
import io.subutai.core.hubmanager.impl.proccessors.SystemConfProcessor;
import io.subutai.core.metric.api.Monitor;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.security.api.SecurityManager;
import io.subutai.hub.share.dto.ProductsDto;
import io.subutai.hub.share.json.JsonUtil;


public class IntegrationImpl implements Integration
{
    private static final Logger LOG = LoggerFactory.getLogger( IntegrationImpl.class.getName() );

    private SecurityManager securityManager;
    private EnvironmentManager environmentManager;
    private PeerManager peerManager;
    private ConfigManager configManager;
    private String peerSecretKeyringPwd;
    private ScheduledExecutorService hearbeatExecutorService = Executors.newSingleThreadScheduledExecutor();
    private ScheduledExecutorService resourceHostConfExecutorService = Executors.newSingleThreadScheduledExecutor();
    private ScheduledExecutorService resourceHostMonitorExecutorService = Executors.newSingleThreadScheduledExecutor();
    private HeartbeatProcessor heartbeatProcessor;
    private ResourceHostConfProcessor resourceHostConfProcessor;
    private SystemConfProcessor systemConfProcessor;
    private ResourceHostMonitorProcessor resourceHostMonitorProcessor;
    private DaoManager daoManager;
    private ConfigDataService configDataService;
    private Monitor monitor;


    public IntegrationImpl( String peerSecretKeyringPwd, DaoManager daoManager )
    {
        this.peerSecretKeyringPwd = peerSecretKeyringPwd;
        this.daoManager = daoManager;
    }


    public void init()
    {
        try
        {
            configDataService = new ConfigDataServiceImpl( daoManager );

            this.configManager = new ConfigManager( securityManager, peerManager, peerSecretKeyringPwd );
            heartbeatProcessor = new HeartbeatProcessor( this, configManager );
            resourceHostConfProcessor = new ResourceHostConfProcessor( this, peerManager, configManager, monitor );
            resourceHostMonitorProcessor =
                    new ResourceHostMonitorProcessor( this, peerManager, configManager, monitor );

            StateLinkProccessor systemConfProcessor = new SystemConfProcessor( configManager );
            StateLinkProccessor environmentProccessor =
                    new EnvironmentProccessor( environmentManager, configManager, peerManager );

            heartbeatProcessor.addProccessor( environmentProccessor );
            heartbeatProcessor.addProccessor( systemConfProcessor );


            this.hearbeatExecutorService.scheduleWithFixedDelay( heartbeatProcessor, 10, 120, TimeUnit.SECONDS );
            this.resourceHostConfExecutorService.scheduleWithFixedDelay( resourceHostConfProcessor, 180, 900,
                    TimeUnit.SECONDS ); // Executes every 3 hours
            this.resourceHostMonitorExecutorService
                    .scheduleWithFixedDelay( resourceHostMonitorProcessor, 30, 300, TimeUnit.SECONDS );
        }
        catch ( IOException | PGPException | CertificateException | KeyStoreException | NoSuchAlgorithmException e )
        {
            LOG.error( e.getMessage() );
        }
    }


    @Override
    public void sendHeartbeat() throws HubPluginException
    {
        heartbeatProcessor.sendHeartbeat();
    }


    @Override
    public void sendResourceHostInfo() throws HubPluginException
    {
        resourceHostConfProcessor.sendResourceHostConf();
    }


    @Override
    public void registerPeer( String hupIp, String email, String password ) throws HubPluginException
    {
        configManager.addHubConfig( hupIp );
        RegistrationManager registrationManager = new RegistrationManager( this, configManager );

        registrationManager.registerPeer( email, password );
        sendHeartbeat();
    }


    @Override
    public String getHubDns() throws HubPluginException
    {
        return configManager.getHubIp();
    }


    @Override
    public String getProducts() throws HubPluginException
    {
        ProductsDto result;
        try
        {
            WebClient client = configManager.getTrustedWebClientWithAuth( "/rest/v1/marketplace/products" );

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

            byte[] encryptedContent = readContent( r );
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
    public void uninstallPlugin( String url )
    {
        int indexOfStr = url.indexOf( "/package/" );
        String fileName = url.substring( indexOfStr + 9, url.length() );
        File file = new File( String.format( "%s/deploy", System.getProperty( "karaf.home" ) ) + "/" + fileName );
        if ( file.delete() )
        {
            LOG.debug( file.getName() + " is removed." );
        }
        LOG.debug( "Product uninstalled successfully..." );
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


    private static ObjectMapper createMapper( JsonFactory factory )
    {
        ObjectMapper mapper = new ObjectMapper( factory );
        mapper.setVisibility( PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY );
        return mapper;
    }
}
