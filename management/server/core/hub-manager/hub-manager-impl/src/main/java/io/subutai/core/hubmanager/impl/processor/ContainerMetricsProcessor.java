package io.subutai.core.hubmanager.impl.processor;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.metric.HistoricalMetrics;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.ResourceHost;
import io.subutai.core.hubmanager.api.HubRequester;
import io.subutai.core.hubmanager.api.RestResult;
import io.subutai.core.hubmanager.api.dao.ContainerMetricsService;
import io.subutai.core.hubmanager.api.exception.HubManagerException;
import io.subutai.core.hubmanager.api.model.ContainerMetrics;
import io.subutai.core.hubmanager.impl.HubManagerImpl;
import io.subutai.core.hubmanager.impl.http.HubRestClient;
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
                                      HubRestClient restClient, ContainerMetricsService containerMetricsService )
    {
        super( hubManager, restClient );

        this.localPeer = localPeer;

        this.monitor = monitor;

        this.containerMetricsService = containerMetricsService;
    }


    @Override
    public void request() throws HubManagerException
    {
        confgureSavedMetrics();
        configureNewMetrics();
    }


    private void configureNewMetrics()
    {
        Calendar cal = Calendar.getInstance();
        Date endTime = cal.getTime();
        cal.add( Calendar.HOUR, 1 );
        Date startTime = cal.getTime();

        for ( ResourceHost host : localPeer.getResourceHosts() )
        {
            ContainersMetricsDto containersMetricsDto = getContainersMetrics( host, startTime, endTime );
            sent( containersMetricsDto );
        }
    }


    private ContainersMetricsDto getContainersMetrics( ResourceHost resourceHost, Date startTime, Date endTime )
    {
        ContainersMetricsDto containersMetricsDto = new ContainersMetricsDto( localPeer.getId(), resourceHost.getId() );

        for ( ContainerHost containerHost : resourceHost.getContainerHosts() )
        {
            HistoricalMetrics historicalMetrics = monitor.getMetricsSeries( containerHost, startTime, endTime );
            HostMetricsDto containerMetricsDto = historicalMetrics.getHostMetrics();
            containerMetricsDto.setType( HostMetricsDto.HostType.CONTAINER_HOST );
            containerMetricsDto.setHostId( containerHost.getId() );
            containerMetricsDto.setHostName( containerHost.getHostname() );

            containersMetricsDto.getContainerHostMetricsDto().add( containerMetricsDto );
        }

        return containersMetricsDto;
    }


    private void confgureSavedMetrics()
    {
        ContainersMetricsDto containersMetricsDto = new ContainersMetricsDto();

        Set<Long> ids = new HashSet<>();
        List<ContainerMetrics> containerMetricsList = containerMetricsService.getAll();
        for ( ContainerMetrics containerMetrics : containerMetricsList )
        {
            HostMetricsDto hostMetricsDto = new HostMetricsDto();
            hostMetricsDto.setHostName( containerMetrics.getHostName() );
            hostMetricsDto.setHostId( containerMetrics.getHostId() );
            hostMetricsDto.setCpu( containerMetrics.getCpuDto() );
            hostMetricsDto.setDisk( containerMetrics.getDiskDto() );
            hostMetricsDto.setMemory( containerMetrics.getMemoryDto() );
            hostMetricsDto.setNet( containerMetrics.getNetDto() );
            containersMetricsDto.getContainerHostMetricsDto().add( hostMetricsDto );
            ids.add( containerMetrics.getId() );
        }

        for ( Long id : ids )
        {
            containerMetricsService.removeMetrics( id );
        }

        sent( containersMetricsDto );
    }


    private void sent( ContainersMetricsDto containersMetricsDto )
    {
        try
        {
            String path = format( "/rest/v1/peers/%s/resource-hosts/%s/containers-metrics", localPeer.getId(),
                    containersMetricsDto.getHostId() );

            RestResult<Object> restResult = restClient.post( path, containersMetricsDto );

            if ( restResult.isSuccess() )
            {
                log.info( "Resource host containers metrics sent successfully" );
            }
            else
            {
                for ( HostMetricsDto metricsDto : containersMetricsDto.getContainerHostMetricsDto() )
                {
                    ContainerMetrics containerMetrics = new ContainerMetricsEntity();
                    containerMetrics.setHostId( containersMetricsDto.getHostId() );
                    containerMetrics.setHostName( metricsDto.getHostName() );
                    containerMetrics.setEndTime( containersMetricsDto.getEndTime() );
                    containerMetrics.setStartTime( containersMetricsDto.getStartTime() );
                    containerMetrics.setCpu( metricsDto.getCpu() );
                    containerMetrics.setDisk( metricsDto.getDisk() );
                    containerMetrics.setMemory( metricsDto.getMemory() );
                    containerMetrics.setNet( metricsDto.getNet() );
                    containerMetricsService.save( containerMetrics );
                }
            }
        }
        catch ( Exception e )
        {
            log.error( e.getMessage() );
        }
    }
}
