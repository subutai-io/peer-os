package io.subutai.core.hubmanager.impl.processor;


import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;

import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.http.HttpStatus;

import io.subutai.common.metric.ResourceHostMetric;
import io.subutai.core.hubmanager.api.exception.HubManagerException;
import io.subutai.core.hubmanager.impl.ConfigManager;
import io.subutai.core.hubmanager.impl.HubManagerImpl;
import io.subutai.core.metric.api.Monitor;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.hub.share.dto.ResourceHostMonitorDto;
import io.subutai.hub.share.json.JsonUtil;


// TODO: Replace WebClient with HubRestClient.
public class ResourceHostMonitorProcessor implements Runnable
{
    private final Logger log = LoggerFactory.getLogger( getClass() );

    private static final int DTO_TTL = 1000 * 60 * 60 * 24 * 2;

    private ConfigManager configManager;

    private HubManagerImpl manager;

    private PeerManager peerManager;

    private Monitor monitor;

    protected ConcurrentLinkedDeque<ResourceHostMonitorDto> queue = new ConcurrentLinkedDeque<>();


    public ResourceHostMonitorProcessor( final HubManagerImpl integration, final PeerManager peerManager,
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
            log.error( "Sending resource hosts monitoring data failed.", e.getMessage() );
        }
    }


    public void sendResourceHostMonitoringData() throws HubManagerException
    {
        if ( manager.isRegisteredWithHub() )
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
                    log.info( e.getMessage(), "No info about available RAM" );
                }

                try
                {
                    resourceHostMonitorDto.setAvailableSpace( resourceHostMetric.getAvailableSpace() );
                }
                catch ( Exception e )
                {
                    resourceHostMonitorDto.setAvailableSpace( 0.0 );
                    log.info( e.getMessage(), "No info about available Space" );
                }

                try
                {
                    resourceHostMonitorDto.setTotalRam( resourceHostMetric.getTotalRam() );
                }
                catch ( Exception e )
                {
                    resourceHostMonitorDto.setTotalRam( 0.0 );
                    log.info( e.getMessage(), "No info about total RAM" );
                }
                try
                {
                    resourceHostMonitorDto.setTotalSpace( resourceHostMetric.getTotalSpace() );
                }
                catch ( Exception e )
                {
                    resourceHostMonitorDto.setTotalSpace( 0.0 );
                    log.info( e.getMessage(), "No info about total Space" );
                }
                try
                {
                    resourceHostMonitorDto.setUsedCpu( resourceHostMetric.getUsedCpu() );
                }
                catch ( Exception e )
                {
                    resourceHostMonitorDto.setUsedCpu( 0.0 );
                    log.info( e.getMessage(), "No info about used CPU" );
                }

                queue( resourceHostMonitorDto );
                send();
            }
        }
    }


    private boolean queue( final ResourceHostMonitorDto resourceHostMonitorDto )
    {
        return queue.offer( resourceHostMonitorDto );
    }


    private void send()
    {
        log.debug( "RH monitor queue size = {}", queue.size() );
        final Iterator<ResourceHostMonitorDto> iterator = queue.iterator();

        while ( iterator.hasNext() )
        {
            ResourceHostMonitorDto dto = iterator.next();

            String path = String.format( "/rest/v1/peers/%s/resource-hosts/%s/monitor", configManager.getPeerId(),
                    dto.getHostId() );
            try
            {
                WebClient client = configManager.getTrustedWebClientWithAuth( path, configManager.getHubIp() );

                byte[] cborData = JsonUtil.toCbor( dto );

                byte[] encryptedData = configManager.getMessenger().produce( cborData );

                Response r = client.post( encryptedData );

                if ( r.getStatus() == HttpStatus.SC_NO_CONTENT )
                {
                    iterator.remove();
                    log.debug( "Resource hosts monitoring data sent successfully. {}", dto );
                }
                else
                {
                    log.warn( "Could not send RH monitoring data: " + r.readEntity( String.class ) );
                }
            }
            catch ( Exception e )
            {
                log.warn( "Could not send RH monitoring data to {}", dto.getPeerId(), e );
            }
        }

        // clean up queue to avoid memory exhaustion
        Iterator<ResourceHostMonitorDto> i = queue.iterator();

        while ( i.hasNext() )
        {
            ResourceHostMonitorDto dto = iterator.next();
            if ( dto.getCreated().getTime() + DTO_TTL < new Date().getTime() )
            {
                log.warn( "Removing RH monitoring data {}", dto );
                iterator.remove();
            }
        }
    }
}
