package io.subutai.core.hubmanager.impl.processor;


import java.util.Date;

import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.http.HttpStatus;

import io.subutai.common.metric.ResourceHostMetric;
import io.subutai.common.network.JournalCtlLevel;
import io.subutai.common.network.P2pLogs;
import io.subutai.common.peer.ResourceHost;
import io.subutai.core.hubmanager.api.HubPluginException;
import io.subutai.core.hubmanager.impl.ConfigManager;
import io.subutai.core.hubmanager.impl.IntegrationImpl;
import io.subutai.core.metric.api.Monitor;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.hub.share.dto.ResourceHostMetricDto;
import io.subutai.hub.share.dto.SystemLogsDto;
import io.subutai.hub.share.json.JsonUtil;


public class ResourceHostDataProcessor implements Runnable
{
    private final Logger log = LoggerFactory.getLogger( ResourceHostDataProcessor.class );

    private ConfigManager configManager;

    private IntegrationImpl manager;

    private PeerManager peerManager;

    private Monitor monitor;

    private Date p2pLogsEndDate;


    public ResourceHostDataProcessor( final IntegrationImpl integration, final PeerManager peerManager,
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
        log.debug( "Getting resource host configs..." );

        ResourceHostMetricDto dto = getConfigs( rhMetric );

        log.debug( "Sending resource host configs to Hub..." );

        String path = String.format( "/rest/v1/peers/%s/resource-hosts/%s", configManager.getPeerId(), dto.getHostId() );

        WebClient client = configManager.getTrustedWebClientWithAuth( path, configManager.getHubIp() );

        byte[] cborData = JsonUtil.toCbor( dto );

        byte[] encryptedData = configManager.getMessenger().produce( cborData );

        Response response = client.post( encryptedData );

        if ( response.getStatus() == HttpStatus.SC_NO_CONTENT )
        {
            log.debug( "Resource host configs processed successfully" );
        }
        else
        {
            log.error( "Error response: {}", response.readEntity( String.class ) );
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
        log.debug( "Getting p2p logs: {} - {}", startDate, endDate );

        P2pLogs p2pLogs = rh.getP2pLogs( JournalCtlLevel.ERROR, startDate, endDate );

        log.debug( "Sending p2p logs to Hub..." );

        String path = String.format( "/rest/v1/peers/%s/resource-hosts/%s/system-logs", configManager.getPeerId(), rh.getId() );

        WebClient client = configManager.getTrustedWebClientWithAuth( path, configManager.getHubIp() );

        SystemLogsDto logsDto = new SystemLogsDto();

        logsDto.setLogs( p2pLogs.getLogs() );

        byte[] plainData = JsonUtil.toCbor( logsDto );
        byte[] encryptedData = configManager.getMessenger().produce( plainData );

        Response response = client.post( encryptedData );

        log.debug( "response.status: {}", response.getStatus() );

        if ( response.getStatus() == HttpStatus.SC_NO_CONTENT )
        {
            log.debug( "Processing p2p logs completed successfully" );
        }
        else
        {
            log.error( "Error response: {}", response.readEntity( String.class ) );
        }
    }
}
