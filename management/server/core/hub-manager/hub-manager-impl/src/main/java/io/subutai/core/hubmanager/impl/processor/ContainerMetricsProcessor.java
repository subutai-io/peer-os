package io.subutai.core.hubmanager.impl.processor;


import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.metric.HistoricalMetrics;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.ResourceHost;
import io.subutai.common.settings.Common;
import io.subutai.core.hubmanager.api.HubRequester;
import io.subutai.core.hubmanager.api.RestClient;
import io.subutai.core.hubmanager.api.RestResult;
import io.subutai.core.hubmanager.api.dao.ContainerMetricsService;
import io.subutai.core.hubmanager.api.exception.HubManagerException;
import io.subutai.core.hubmanager.api.model.ContainerMetrics;
import io.subutai.core.hubmanager.impl.HubManagerImpl;
import io.subutai.core.hubmanager.impl.model.ContainerMetricsEntity;
import io.subutai.core.metric.api.Monitor;
import io.subutai.hub.share.dto.metrics.HostMetricsDto;
import io.subutai.hub.share.dto.metrics.ContainersMetricsDto;

import static java.lang.String.format;


public class ContainerMetricsProcessor extends HubRequester
{
    private final Logger log = LoggerFactory.getLogger( getClass() );

    private static final int DB_FETCH_LIMIT = 100;

    private static final int METRIC_TTL_DAYS = 1;

    private LocalPeer localPeer;

    private Monitor monitor;

    private ContainerMetricsService containerMetricsService;

    private int scanIntervalMinutes;


    public ContainerMetricsProcessor( HubManagerImpl hubManager, LocalPeer localPeer, Monitor monitor,
                                      RestClient restClient, ContainerMetricsService containerMetricsService,
                                      int scanIntervalMinutes )
    {
        super( hubManager, restClient );

        this.localPeer = localPeer;

        this.monitor = monitor;

        this.containerMetricsService = containerMetricsService;

        this.scanIntervalMinutes = scanIntervalMinutes;
    }


    @Override
    public void request() throws HubManagerException
    {
        processUnsentMetrics();
        processRealTimeMetrics();
        purgeOldMetrics();
    }


    private void processRealTimeMetrics()
    {
        Calendar cal = Calendar.getInstance();
        Date endTime = cal.getTime();
        //add 1 minute just in case not to loose some metrics in between, Hub side should handle duplicate metrics
        cal.add( Calendar.MINUTE, -( scanIntervalMinutes + 1 ) );
        Date startTime = cal.getTime();

        ContainersMetricsDto containersMetricsDto = new ContainersMetricsDto( localPeer.getId() );

        for ( ResourceHost host : localPeer.getResourceHosts() )
        {
            try
            {
                populateRealTimeContainerMetrics( containersMetricsDto, host, startTime, endTime );

                send( containersMetricsDto );
            }
            catch ( Exception e )
            {
                log.error( "Error sending container metrics of resource host {}: {}", host.getId(), e.getMessage() );
            }
        }
    }


    private void populateRealTimeContainerMetrics( ContainersMetricsDto containersMetricsDto, ResourceHost resourceHost,
                                                   Date startTime, Date endTime )
    {
        for ( ContainerHost containerHost : resourceHost.getContainerHosts() )
        {
            //send only Hub environment containers' metrics
            if ( Common.HUB_ID.equals( containerHost.getInitiatorPeerId() ) )
            {
                try
                {
                    HistoricalMetrics historicalMetrics = monitor.getMetricsSeries( containerHost, startTime, endTime );
                    HostMetricsDto hostMetricsDto = historicalMetrics.getHostMetrics();

                    //skip "zero" metrics
                    if ( !HistoricalMetrics.isZeroMetric( hostMetricsDto ) )
                    {
                        hostMetricsDto.setType( HostMetricsDto.HostType.CONTAINER_HOST );
                        hostMetricsDto.setHostId( containerHost.getId() );
                        //set container name instead of hostname since it is unchangeable unlike hostname
                        hostMetricsDto.setHostName( containerHost.getContainerName() );
                        hostMetricsDto.setStartTime( startTime );
                        hostMetricsDto.setEndTime( endTime );

                        containersMetricsDto.getContainerHostMetricsDto().add( hostMetricsDto );
                    }
                }
                catch ( Exception e )
                {
                    log.error( "Failed to obtain metrics of container {}: {}", containerHost.getId(), e.getMessage() );
                }
            }
        }
    }


    private void processUnsentMetrics()
    {
        try
        {
            ContainersMetricsDto containersMetricsDto = new ContainersMetricsDto();

            populateUnsentContainerMetrics( containersMetricsDto );

            send( containersMetricsDto );
        }
        catch ( Exception e )
        {
            log.error( "Error sending container metrics: {}", e.getMessage() );
        }
    }


    private void populateUnsentContainerMetrics( ContainersMetricsDto containersMetricsDto )
    {

        List<ContainerMetrics> containerMetricsList = containerMetricsService.getOldest( DB_FETCH_LIMIT );

        for ( ContainerMetrics containerMetrics : containerMetricsList )
        {
            HostMetricsDto hostMetricsDto = new HostMetricsDto();
            hostMetricsDto.setType( HostMetricsDto.HostType.CONTAINER_HOST );
            hostMetricsDto.setHostName( containerMetrics.getHostName() );
            hostMetricsDto.setHostId( containerMetrics.getHostId() );
            hostMetricsDto.setCpu( containerMetrics.getCpuDto() );
            hostMetricsDto.setDisk( containerMetrics.getDiskDto() );
            hostMetricsDto.setMemory( containerMetrics.getMemoryDto() );
            hostMetricsDto.setNet( containerMetrics.getNetDto() );
            hostMetricsDto.setStartTime( containerMetrics.getStartTime() );
            hostMetricsDto.setEndTime( containerMetrics.getEndTime() );
            //set transient db id
            hostMetricsDto.setDbId( containerMetrics.getId() );

            containersMetricsDto.getContainerHostMetricsDto().add( hostMetricsDto );
        }
    }


    private void send( ContainersMetricsDto containersMetricsDto )
    {
        try
        {
            String path = format( "/rest/v1/peers/%s/containers-metrics", localPeer.getId() );

            RestResult<Object> restResult = restClient.post( path, containersMetricsDto );

            for ( HostMetricsDto metricsDto : containersMetricsDto.getContainerHostMetricsDto() )
            {
                if ( restResult.isSuccess() )
                {
                    //remove the ones that are already in DB
                    if ( metricsDto.getDbId() != null )
                    {
                        containerMetricsService.removeMetrics( metricsDto.getDbId() );
                    }
                }
                else if ( metricsDto.getDbId() == null )
                {
                    //save the ones that are new
                    ContainerMetrics containerMetrics = new ContainerMetricsEntity();

                    containerMetrics.setHostId( metricsDto.getHostId() );
                    containerMetrics.setHostName( metricsDto.getHostName() );
                    containerMetrics.setEndTime( metricsDto.getEndTime() );
                    containerMetrics.setStartTime( metricsDto.getStartTime() );
                    containerMetrics.setCpu( metricsDto.getCpu() );
                    containerMetrics.setDisk( metricsDto.getDisk() );
                    containerMetrics.setMemory( metricsDto.getMemory() );
                    containerMetrics.setNet( metricsDto.getNet() );

                    containerMetricsService.save( containerMetrics );
                }
            }

            if ( !restResult.isSuccess() )
            {
                log.error( "Failed to send container metrics: {} - {}", restResult.getReasonPhrase(),
                        restResult.getError() );
            }
        }
        catch ( Exception e )
        {
            log.error( "Error sending container metrics: {}", e.getMessage() );
        }
    }


    private void purgeOldMetrics()
    {
        try
        {
            containerMetricsService.purgeOldMetrics( METRIC_TTL_DAYS );
        }
        catch ( Exception e )
        {
            log.error( "Error purging old metrics: {}", e.getMessage() );
        }
    }
}
