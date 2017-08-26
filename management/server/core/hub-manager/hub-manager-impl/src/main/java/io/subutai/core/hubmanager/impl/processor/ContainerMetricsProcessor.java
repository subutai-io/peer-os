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

    private LocalPeer localPeer;

    private Monitor monitor;

    private ContainerMetricsService containerMetricsService;


    public ContainerMetricsProcessor( HubManagerImpl hubManager, LocalPeer localPeer, Monitor monitor,
                                      RestClient restClient, ContainerMetricsService containerMetricsService )
    {
        super( hubManager, restClient );

        this.localPeer = localPeer;

        this.monitor = monitor;

        this.containerMetricsService = containerMetricsService;
    }


    @Override
    public void request() throws HubManagerException
    {
        processUnsentMetrics();
        processRealTimeMetrics();
    }


    private void processRealTimeMetrics()
    {
        Calendar cal = Calendar.getInstance();
        Date endTime = cal.getTime();
        cal.add( Calendar.HOUR, -1 );
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
            try
            {
                HistoricalMetrics historicalMetrics = monitor.getMetricsSeries( containerHost, startTime, endTime );
                HostMetricsDto hostMetricsDto = historicalMetrics.getHostMetrics();

                hostMetricsDto.setType( HostMetricsDto.HostType.CONTAINER_HOST );
                hostMetricsDto.setHostId( containerHost.getId() );
                //set container name instead of hostname since it is unchangeable unlike hostname
                hostMetricsDto.setHostName( containerHost.getContainerName() );
                hostMetricsDto.setStartTime( startTime );
                hostMetricsDto.setEndTime( endTime );

                containersMetricsDto.getContainerHostMetricsDto().add( hostMetricsDto );
            }
            catch ( Exception e )
            {
                log.error( "Failed to obtain metrics of container {}: {}", containerHost.getId(), e.getMessage() );
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

        List<ContainerMetrics> containerMetricsList = containerMetricsService.getAll();

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
                    if ( metricsDto.getDbId() != null )
                    {
                        //containerMetricsService.removeMetrics( metricsDto.getDbId() );
                        log.debug( "REMOVING SENT METRIC {}", metricsDto.getDbId() );
                    }
                }
                else if ( metricsDto.getDbId() == null )
                {
                    ContainerMetrics containerMetrics = new ContainerMetricsEntity();

                    containerMetrics.setHostId( metricsDto.getHostId() );
                    containerMetrics.setHostName( metricsDto.getHostName() );
                    containerMetrics.setEndTime( metricsDto.getEndTime() );
                    containerMetrics.setStartTime( metricsDto.getStartTime() );
                    containerMetrics.setCpu( metricsDto.getCpu() );
                    containerMetrics.setDisk( metricsDto.getDisk() );
                    containerMetrics.setMemory( metricsDto.getMemory() );
                    containerMetrics.setNet( metricsDto.getNet() );

//                    containerMetricsService.save( containerMetrics );
                    log.debug( "SAVING UNSENT METRIC {}", metricsDto );
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
}
