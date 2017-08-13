package io.subutai.core.hubmanager.impl.processor;


import java.util.ArrayList;
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
        sentSavedMetrics();
        sentNewMetrics();
    }


    private void sentNewMetrics()
    {
        Calendar cal = Calendar.getInstance();
        Date endTime = cal.getTime();
        cal.add( Calendar.HOUR, 1 );
        Date startTime = cal.getTime();

        for ( ResourceHost host : localPeer.getResourceHosts() )
        {
            ContainersMetricsDto containersMetricsDto = getContainersMetrics( host, startTime, endTime )

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

                        containerMetrics.setCpu( metricsDto.getCpu().getIdle() ); //TODO review which data to save
                        containerMetrics.setDisk( 0.0 );
                        containerMetrics.setMemory( metricsDto.getMemory().getActive() ); //TODO review which data to save
                        containerMetrics.setNet( 0.0 );
                        containerMetricsService.persistMetrics( containerMetrics );
                    }
                }
            }
            catch ( Exception e )
            {
                log.error( e.getMessage() );
            }
        }
    }


    private ContainersMetricsDto getContainersMetrics( ResourceHost resourceHost, Date startTime, Date endTime )
    {
        ContainersMetricsDto containersMetricsDto = new ContainersMetricsDto( localPeer.getId(), resourceHost.getId() );

        List<HostMetricsDto> containersMetricsDtos = new ArrayList<>();
        for ( ContainerHost containerHost : resourceHost.getContainerHosts() )
        {
            HistoricalMetrics historicalMetrics = monitor.getMetricsSeries( containerHost, startTime, endTime );
            HostMetricsDto containerMetricsDto = historicalMetrics.getHostMetrics();
            containerMetricsDto.setType( HostMetricsDto.HostType.CONTAINER_HOST );
            containerMetricsDto.setHostId( containerHost.getId() );
            containerMetricsDto.setHostName( containerHost.getHostname() );

            containersMetricsDtos.add( containerMetricsDto );
        }

        containersMetricsDto.setContainerHostMetricsDto( containersMetricsDtos );

        return containersMetricsDto;
    }


    private void sentSavedMetrics()
    {
        List<ContainerMetrics> containerMetrics = containerMetricsService.getAll();
    }
}
