package io.subutai.core.hubmanager.impl.proccessors;


import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

import javax.ws.rs.core.Response;

import org.bouncycastle.openpgp.PGPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.http.HttpStatus;

import io.subutai.common.metric.ResourceHostMetric;
import io.subutai.core.hubmanager.api.HubPluginException;
import io.subutai.core.hubmanager.api.model.Config;
import io.subutai.core.hubmanager.impl.ConfigManager;
import io.subutai.core.hubmanager.impl.IntegrationImpl;
import io.subutai.core.metric.api.Monitor;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.hub.share.dto.ResourceHostMetricDto;
import io.subutai.hub.share.json.JsonUtil;


/**
 * Created by ${Zubaidullo} on 11/20/15.
 */
public class ResourceHostConfProcessor implements Runnable
{
    private static final Logger LOG = LoggerFactory.getLogger( ResourceHostConfProcessor.class );
    private ConfigManager configManager;
    private IntegrationImpl manager;
    private PeerManager peerManager;
    private Monitor monitor;


    public ResourceHostConfProcessor( final IntegrationImpl integration, final PeerManager peerManager,
                                      final ConfigManager configManager, final Monitor monitor )
    {
        this.peerManager = peerManager;
        this.configManager = configManager;
        this.manager = integration;
        this.monitor = monitor;
    }


    @Override
    public void run()
    {
        try
        {
            if ( manager.getConfigDataService().getHubConfig( configManager.getPeerId() ) != null )
            {
                Config config = manager.getConfigDataService().getHubConfig( configManager.getPeerId() );
                LOG.debug( "Sending resource hosts configurations started..." );
                configManager.setHubIp( config.getHubIp() );
                configManager.setSuperNodeIp( config.getSuperNodeIp() );
                sendResourceHostConf();
                LOG.debug( "Sending resource hosts configurations finished successfully..." );
            }
        }
        catch ( Exception e )
        {
            LOG.debug( "Sending resource hosts configurations failed." );
            LOG.error( e.getMessage(), e );
        }
    }


    public void sendResourceHostConf() throws HubPluginException
    {
        try
        {
            for ( ResourceHostMetric resourceHostMetric : monitor.getResourceHostMetrics().getResources() )
            {
                ResourceHostMetricDto resourceHostMetricDto = new ResourceHostMetricDto();
                resourceHostMetricDto.setPeerId( peerManager.getLocalPeer().getId() );
                resourceHostMetricDto.setName( resourceHostMetric.getHostInfo().getHostname() );
                resourceHostMetricDto.setHostId( resourceHostMetric.getHostInfo().getId() );

                try
                {
                    resourceHostMetricDto.setCpuModel( resourceHostMetric.getCpuModel() );
                }
                catch(Exception e)
                {
                    LOG.info( e.getMessage(), "No info about CPU model" );
                }

                try
                {
                    resourceHostMetricDto.setMemory( resourceHostMetric.getTotalRam() );
                }
                catch(Exception e)
                {
                    LOG.info( e.getMessage(), "No info about total RAM" );
                }

                try
                {
                    resourceHostMetricDto.setDisk( resourceHostMetric.getTotalSpace() );
                }
                catch(Exception e)
                {
                    LOG.info( e.getMessage(), "No info about total Space" );
                }

                try
                {
                    resourceHostMetricDto.setCpuCore( resourceHostMetric.getCpuCore() );
                }
                catch(Exception e)
                {
                    LOG.info( e.getMessage(), "No info about CPU core" );
                }

                String path = String.format( "/rest/v1/peers/%s/resource-hosts/%s", configManager.getPeerId(),
                        resourceHostMetricDto.getHostId() );

                WebClient client = configManager.getTrustedWebClientWithAuth( path );

                byte[] cborData = JsonUtil.toCbor( resourceHostMetricDto );

                byte[] encryptedData = configManager.getMessenger().produce( cborData );

                Response r = client.post( encryptedData );

                if ( r.getStatus() == HttpStatus.SC_NO_CONTENT )
                {
                    LOG.debug( "Resource hosts configurations sent successfully." );
                }
                else
                {
                    throw new HubPluginException(
                            "Could not send resource hosts configurations: " + r.readEntity( String.class ) );
                }
            }
        }
        catch ( PGPException | IOException | KeyStoreException | UnrecoverableKeyException | NoSuchAlgorithmException
                e )
        {
            LOG.error( "Could not send resource hosts configurations.", e );
        }
    }
}
