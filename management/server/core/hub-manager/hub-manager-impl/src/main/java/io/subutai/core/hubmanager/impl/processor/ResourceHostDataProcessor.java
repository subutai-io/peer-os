package io.subutai.core.hubmanager.impl.processor;


import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.time.DateUtils;

import io.subutai.common.metric.ResourceHostMetric;
import io.subutai.common.network.JournalCtlLevel;
import io.subutai.common.network.P2pLogs;
import io.subutai.common.peer.ResourceHost;
import io.subutai.core.hubmanager.api.HubPluginException;
import io.subutai.core.hubmanager.impl.ConfigManager;
import io.subutai.core.hubmanager.impl.HubManagerImpl;
import io.subutai.core.hubmanager.impl.http.HubRestClient;
import io.subutai.core.hubmanager.impl.http.RestResult;
import io.subutai.core.metric.api.Monitor;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.hub.share.dto.ResourceHostMetricDto;
import io.subutai.hub.share.dto.SystemLogsDto;

import static java.lang.String.format;


public class ResourceHostDataProcessor implements Runnable
{
    private final Logger log = LoggerFactory.getLogger( ResourceHostDataProcessor.class );

    private final HubManagerImpl manager;

    private final PeerManager peerManager;

    private final HubRestClient restClient;

    private final Monitor monitor;

    private final String peerId;

    private Date p2pLogsEndDate;


    public ResourceHostDataProcessor( final HubManagerImpl integration, final PeerManager peerManager,
                                      final ConfigManager configManager, final Monitor monitor )
    {
        this.peerManager = peerManager;
        this.manager = integration;
        this.monitor = monitor;

        restClient = new HubRestClient( configManager );

        peerId = configManager.getPeerId();
    }


    @Override
    public void run()
    {
        try
        {
            process();
        }
        catch ( Exception e )
        {
            log.error( "Error to process resource host data: ", e );
        }
    }


    public void process() throws HubPluginException
    {
        if ( manager.getRegistrationState() )
        {
            processConfigs();

            processP2PLogs();
        }
    }


    private void processConfigs()
    {
        for ( ResourceHostMetric rhMetric : monitor.getResourceHostMetrics().getResources() )
        {
            try
            {
                processConfigs( rhMetric );
            }
            catch ( Exception e )
            {
                log.error( "Error to process resource host configs: ", e );
            }
        }
    }


    private void processConfigs( ResourceHostMetric rhMetric ) throws Exception
    {
        log.info( "Getting resource host configs..." );

        ResourceHostMetricDto metricDto = getConfigs( rhMetric );

        log.info( "Sending resource host configs to Hub..." );

        String path = format( "/rest/v1/peers/%s/resource-hosts/%s", peerId, metricDto.getHostId() );

        RestResult<Object> restResult = restClient.post( path, metricDto );

        if ( restResult.isSuccess() )
        {
            log.info( "Resource host configs processed successfully" );
        }
    }


    private ResourceHostMetricDto getConfigs( ResourceHostMetric rhMetric )
    {
        ResourceHostMetricDto dto = new ResourceHostMetricDto();

        dto.setPeerId( peerManager.getLocalPeer().getId() );
        dto.setName( rhMetric.getHostInfo().getHostname() );
        dto.setHostId( rhMetric.getHostInfo().getId() );

        try
        {
            dto.setCpuModel( rhMetric.getCpuModel() );
        }
        catch ( Exception e )
        {
            log.error( "Error to get CPU model: {}", e.getMessage() );
        }

        try
        {
            dto.setMemory( rhMetric.getTotalRam() );
        }
        catch ( Exception e )
        {
            log.error( "Error to get total RAM: {}", e.getMessage() );
        }

        try
        {
            dto.setDisk( rhMetric.getTotalSpace() );
        }
        catch ( Exception e )
        {
            log.error( "Error to get total space: {}", e.getMessage() );
        }

        try
        {
            dto.setCpuCore( rhMetric.getCpuCore() );
        }
        catch ( Exception e )
        {
            log.error( "Error to get CPU cores: {}", e.getMessage() );
        }

        return dto;
    }


    private void processP2PLogs()
    {
        Date currentDate = new Date();

        Date startDate = p2pLogsEndDate != null
                         ? p2pLogsEndDate
                         : DateUtils.addMinutes( currentDate, -15 );

        p2pLogsEndDate = currentDate;

        for ( ResourceHost rh : peerManager.getLocalPeer().getResourceHosts() )
        {
            try
            {
                processP2PLogs( rh, startDate, currentDate );
            }
            catch ( Exception e )
            {
                log.error( "Error to process p2p logs: ", e );
            }
        }
    }


    private void processP2PLogs( ResourceHost rh, Date startDate, Date endDate ) throws Exception
    {
        log.info( "Getting p2p logs: {} - {}", startDate, endDate );

        P2pLogs p2pLogs = rh.getP2pLogs( JournalCtlLevel.ERROR, startDate, endDate );

        log.info( "logs.size: {}", p2pLogs.getLogs().size() );

        if ( p2pLogs.isEmpty() )
        {
            return;
        }

        SystemLogsDto logsDto = new SystemLogsDto();

        logsDto.setLogs( p2pLogs.getLogs() );

        log.info( "Sending p2p logs to Hub..." );

        String path = format( "/rest/v1/peers/%s/resource-hosts/%s/system-logs", peerId, rh.getId() );

        RestResult<Object> restResult = restClient.post( path, logsDto );

        if ( restResult.isSuccess() )
        {
            log.info( "Processing p2p logs completed successfully" );
        }
    }
}
