package io.subutai.core.hubmanager.impl.processor;


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
import io.subutai.core.hubmanager.impl.ConfigManager;
import io.subutai.core.hubmanager.impl.IntegrationImpl;
import io.subutai.core.metric.api.Monitor;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.hub.share.dto.ResourceHostMonitorDto;
import io.subutai.hub.share.json.JsonUtil;

//TODO close web clients and responses
public class ResourceHostMonitorProcessor implements Runnable
{
    private static final Logger LOG = LoggerFactory.getLogger( ResourceHostMonitorProcessor.class );
    private ConfigManager configManager;
    private IntegrationImpl manager;
    private PeerManager peerManager;
    private Monitor monitor;


    public ResourceHostMonitorProcessor( final IntegrationImpl integration, final PeerManager peerManager,
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
            sendResourceHostMonitoringData();
        }
        catch ( Exception e )
        {
            LOG.debug( "Sending resource hosts monitoring data failed." );
            LOG.error( e.getMessage(), e );
        }
    }


    public void sendResourceHostMonitoringData() throws HubPluginException
    {
        if ( manager.getRegistrationState() )
        {
            try
            {
                for ( ResourceHostMetric resourceHostMetric : monitor.getResourceHostMetrics().getResources() )
                {
                    ResourceHostMonitorDto resourceHostMonitorDto = new ResourceHostMonitorDto();

                    resourceHostMonitorDto.setPeerId( peerManager.getLocalPeer().getId() );
                    resourceHostMonitorDto.setName( resourceHostMetric.getHostInfo().getHostname() );
                    resourceHostMonitorDto.setHostId( resourceHostMetric.getHostInfo().getId() );

                    try
                    {
                        resourceHostMonitorDto.setAvailableRam( resourceHostMetric.getAvailableRam() );
                    }
                    catch ( Exception e )
                    {
                        resourceHostMonitorDto.setAvailableRam( 0.0 );
                        LOG.info( e.getMessage(), "No info about available RAM" );
                    }

                    try
                    {
                        resourceHostMonitorDto.setAvailableSpace( resourceHostMetric.getAvailableSpace() );
                    }
                    catch ( Exception e )
                    {
                        resourceHostMonitorDto.setAvailableSpace( 0.0 );
                        LOG.info( e.getMessage(), "No info about available Space" );
                    }

                    try
                    {
                        resourceHostMonitorDto.setFreeRam( resourceHostMetric.getFreeRam() );
                    }
                    catch ( Exception e )
                    {
                        resourceHostMonitorDto.setFreeRam( 0.0 );
                        LOG.info( e.getMessage(), "No info about free RAM" );
                    }

                    try
                    {
                        resourceHostMonitorDto.setTotalRam( resourceHostMetric.getTotalRam() );
                    }
                    catch ( Exception e )
                    {
                        resourceHostMonitorDto.setTotalRam( 0.0 );
                        LOG.info( e.getMessage(), "No info about total RAM" );
                    }
                    try
                    {
                        resourceHostMonitorDto.setTotalSpace( resourceHostMetric.getTotalSpace() );
                    }
                    catch ( Exception e )
                    {
                        resourceHostMonitorDto.setTotalSpace( 0.0 );
                        LOG.info( e.getMessage(), "No info about total Space" );
                    }
                    try
                    {
                        resourceHostMonitorDto.setUsedCpu( resourceHostMetric.getUsedCpu() );
                    }
                    catch ( Exception e )
                    {
                        resourceHostMonitorDto.setUsedCpu( 0.0 );
                        LOG.info( e.getMessage(), "No info about used CPU" );
                    }

                    String path =
                            String.format( "/rest/v1/peers/%s/resource-hosts/%s/monitor", configManager.getPeerId(),
                                    resourceHostMonitorDto.getHostId() );

                    WebClient client = configManager.getTrustedWebClientWithAuth( path, configManager.getHubIp() );

                    byte[] cborData = JsonUtil.toCbor( resourceHostMonitorDto );

                    byte[] encryptedData = configManager.getMessenger().produce( cborData );

                    Response r = client.post( encryptedData );

                    if ( r.getStatus() == HttpStatus.SC_NO_CONTENT )
                    {
                        LOG.debug( "Resource hosts monitoring data sent successfully." );
                    }
                    else
                    {
                        throw new HubPluginException(
                                "Could not send resource hosts monitoring data: " + r.readEntity( String.class ) );
                    }
                }
            }
            catch ( PGPException | IOException | KeyStoreException | UnrecoverableKeyException |
                    NoSuchAlgorithmException e )
            {
                LOG.error( "Could not send resource hosts monitoring data.", e );
            }
        }
    }
}
