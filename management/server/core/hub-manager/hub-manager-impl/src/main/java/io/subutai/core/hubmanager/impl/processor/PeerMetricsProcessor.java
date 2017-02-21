package io.subutai.core.hubmanager.impl.processor;


import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.http.HttpStatus;

import io.subutai.common.metric.HistoricalMetrics;
import io.subutai.common.metric.ResourceHostMetric;
import io.subutai.common.peer.ResourceHost;
import io.subutai.core.hubmanager.api.HubRequester;
import io.subutai.core.hubmanager.api.RestClient;
import io.subutai.core.hubmanager.api.exception.HubManagerException;
import io.subutai.core.hubmanager.impl.ConfigManager;
import io.subutai.core.hubmanager.impl.HubManagerImpl;
import io.subutai.core.metric.api.Monitor;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.hub.share.dto.metrics.DiskDto;
import io.subutai.hub.share.dto.metrics.HostMetricsDto;
import io.subutai.hub.share.dto.metrics.PeerMetricsDto;
import io.subutai.hub.share.json.JsonUtil;


// TODO: Replace WebClient with HubRestClient.
public class PeerMetricsProcessor extends HubRequester
{
    private final Logger log = LoggerFactory.getLogger( getClass() );

    private static final long DTO_TTL = TimeUnit.DAYS.toMillis( 2 );

    private ConfigManager configManager;

    private PeerManager peerManager;

    private Monitor monitor;

    private ConcurrentLinkedDeque<PeerMetricsDto> queue = new ConcurrentLinkedDeque<>();

    private int interval;


    public PeerMetricsProcessor( final HubManagerImpl hubManager, final PeerManager peerManager,
                                 final ConfigManager configManager, final Monitor monitor, final RestClient restClient,
                                 final int interval )
    {
        super( hubManager, restClient );

        this.peerManager = peerManager;
        this.configManager = configManager;
        this.monitor = monitor;
        this.interval = interval;
    }


    @Override
    public void request() throws HubManagerException
    {
        sendPeerMetrics();
    }


    private void sendPeerMetrics() throws HubManagerException
    {
        Calendar cal = Calendar.getInstance();
        Date endTime = cal.getTime();
        cal.add( Calendar.SECOND, -interval );
        Date startTime = cal.getTime();
        PeerMetricsDto peerMetricsDto =
                new PeerMetricsDto( peerManager.getLocalPeer().getId(), startTime.getTime(), endTime.getTime() );
        for ( ResourceHost host : peerManager.getLocalPeer().getResourceHosts() )
        {
            ResourceHostMetric resourceHostMetric = monitor.getResourceHostMetric( host );
            log.info( "{}", resourceHostMetric );
            final HistoricalMetrics historicalMetrics = monitor.getMetricsSeries( host, startTime, endTime );
            final HostMetricsDto hostMetrics = historicalMetrics.getHostMetrics();

            hostMetrics.setHostName( resourceHostMetric.getHostInfo().getHostname() );
            hostMetrics.setHostId( resourceHostMetric.getHostInfo().getId() );
            hostMetrics.setContainersCount( resourceHostMetric.getContainersCount() );
            hostMetrics.setManagement( host.isManagementHost() );

            try
            {
                hostMetrics.getMemory().setTotal( resourceHostMetric.getTotalRam() );
            }
            catch ( Exception e )
            {
                hostMetrics.getMemory().setTotal( 0.0 );
                log.info( e.getMessage(), "No info about total RAM" );
            }

            try
            {
                hostMetrics.getMemory().setAvailable( resourceHostMetric.getAvailableRam() );
            }
            catch ( Exception e )
            {
                hostMetrics.getMemory().setAvailable( 0.0 );
                log.info( e.getMessage(), "No info about available RAM" );
            }

            DiskDto diskDto = new DiskDto();
            diskDto.setTotal( resourceHostMetric.getTotalSpace() );
            diskDto.setUsed( resourceHostMetric.getUsedSpace() );
            hostMetrics.getDisk().put( HostMetricsDto.MNT_PARTITION, diskDto );
            try
            {
                hostMetrics.getCpu().setIdle( resourceHostMetric.getCpuIdle() );
                hostMetrics.getCpu().setModel( resourceHostMetric.getCpuModel() );
                hostMetrics.getCpu().setCoreCount( resourceHostMetric.getCpuCore() );
                hostMetrics.getCpu().setFrequency( resourceHostMetric.getCpuFrequency() );
            }
            catch ( Exception e )
            {
                hostMetrics.getCpu().setIdle( 0.0 );
                log.info( e.getMessage(), "No info about used CPU" );
            }
            peerMetricsDto.addHostMetrics( hostMetrics );
        }

        queue( peerMetricsDto );

        send();
    }


    private boolean queue( final PeerMetricsDto peerMetricsDto )
    {
        return queue.offer( peerMetricsDto );
    }


    private void send()
    {
        log.debug( "Peer monitor queue size = {}", queue.size() );
        final Iterator<PeerMetricsDto> iterator = queue.iterator();

        while ( iterator.hasNext() )
        {
            PeerMetricsDto dto = iterator.next();

            String path = String.format( "/rest/v1/peers/%s/monitor", configManager.getPeerId() );
            try
            {
                WebClient client = configManager.getTrustedWebClientWithAuth( path, configManager.getHubIp() );

                byte[] cborData = JsonUtil.toCbor( dto );

                byte[] encryptedData = configManager.getMessenger().produce( cborData );

                Response r = client.post( encryptedData );

                if ( r.getStatus() == HttpStatus.SC_NO_CONTENT )
                {
                    iterator.remove();
                    log.debug( "Peer monitoring data sent successfully. {}", dto );
                }
                else
                {
                    log.warn( "Could not send peer monitoring data: " + r.readEntity( String.class ) + " " + dto
                            .toString() );
                }
            }
            catch ( Exception e )
            {
                log.warn( "Could not send peer monitoring data to {}", dto.getPeerId(), e );
            }
        }

        // clean up queue to avoid memory exhaustion

        Iterator<PeerMetricsDto> i = queue.iterator();
        while ( i.hasNext() )
        {
            final PeerMetricsDto dto = i.next();
            if ( dto.getCreatedTime() + DTO_TTL < System.currentTimeMillis() )
            {
                log.warn( "Removing peer monitoring data {}", dto );
                i.remove();
            }
        }
    }
}
