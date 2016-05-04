package io.subutai.core.hubmanager.impl.proccessors;


import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Response;

import org.bouncycastle.openpgp.PGPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.http.HttpStatus;

import io.subutai.common.metric.ResourceHostMetric;
import io.subutai.common.network.JournalCtlLevel;
import io.subutai.common.network.P2pLogs;
import io.subutai.common.peer.ResourceHost;
import io.subutai.common.peer.ResourceHostException;
import io.subutai.core.hubmanager.api.HubPluginException;
import io.subutai.core.hubmanager.impl.ConfigManager;
import io.subutai.core.hubmanager.impl.IntegrationImpl;
import io.subutai.core.metric.api.Monitor;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.hub.share.dto.ResourceHostMetricDto;
import io.subutai.hub.share.dto.SystemLogsDto;
import io.subutai.hub.share.json.JsonUtil;

//TODO close web clients and responses
public class ResourceHostConfProcessor implements Runnable
{
    private static final Logger LOG = LoggerFactory.getLogger( ResourceHostConfProcessor.class );
    private static final int TIME_15_MINUTES = 900;
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
            sendResourceHostConf();
            LOG.debug( "Sending resource hosts configurations finished successfully..." );
        }
        catch ( Exception e )
        {
            LOG.debug( "Sending resource hosts configurations failed." );
            LOG.error( e.getMessage(), e );
        }
    }


    public void sendResourceHostConf() throws HubPluginException
    {
        if ( manager.getRegistrationState() )
        {
            LOG.debug( "Sending resource hosts configurations started..." );

            try
            {
                Date currentDate = new Date();
                java.util.Calendar pastDate = Calendar.getInstance();
                pastDate.setTime( currentDate );
                pastDate.add( Calendar.MINUTE, TIME_15_MINUTES * -1 ); // * -1 is for subtract X amount of minute
                for ( ResourceHost resourceHost : peerManager.getLocalPeer().getResourceHosts() )
                {
                    String path =
                            String.format( "/rest/v1/peers/%s/resource-hosts/%s/system-logs", configManager.getPeerId(),
                                    resourceHost.getId() );
                    try
                    {
                        Set<String> logs = new HashSet<>();
                        P2pLogs p2pLogs =
                                resourceHost.getP2pLogs( JournalCtlLevel.ERROR, pastDate.getTime(), currentDate );
                        for ( String s : p2pLogs.getLogs() )
                        {
                            logs.add( s );
                        }

                        try
                        {
                            WebClient client =
                                    configManager.getTrustedWebClientWithAuth( path, configManager.getHubIp() );
                            SystemLogsDto logsDto = new SystemLogsDto();
                            logsDto.setLogs( logs );

                            byte[] plainData = JsonUtil.toCbor( logsDto );
                            byte[] encryptedData = configManager.getMessenger().produce( plainData );

                            LOG.debug( "Sending RH p2p logs to HUB:" );

                            Response r = client.post( encryptedData );

                            if ( r.getStatus() != HttpStatus.SC_NO_CONTENT )
                            {
                                LOG.error( "Could not send data to Hub (REST)", r.readEntity( String.class ) );
                            }

                            LOG.debug( "RH p2p logs sent to HUB successfully." );
                        }
                        catch ( PGPException | UnrecoverableKeyException | NoSuchAlgorithmException |
                                KeyStoreException |
                                IOException e )
                        {
                            LOG.error( "Could not send data to Hub (SS)", e.getMessage() );
                        }
                    }
                    catch ( ResourceHostException e )
                    {
                        LOG.error( e.getMessage() );
                    }
                }
            }
            catch ( Exception e )
            {
                LOG.error( "Could not get P2P logs", e.getMessage() );
            }
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
                    catch ( Exception e )
                    {
                        LOG.info( e.getMessage(), "No info about CPU model" );
                    }

                    try
                    {
                        resourceHostMetricDto.setMemory( resourceHostMetric.getTotalRam() );
                    }
                    catch ( Exception e )
                    {
                        LOG.info( e.getMessage(), "No info about total RAM" );
                    }

                    try
                    {
                        resourceHostMetricDto.setDisk( resourceHostMetric.getTotalSpace() );
                    }
                    catch ( Exception e )
                    {
                        LOG.info( e.getMessage(), "No info about total Space" );
                    }

                    try
                    {
                        resourceHostMetricDto.setCpuCore( resourceHostMetric.getCpuCore() );
                    }
                    catch ( Exception e )
                    {
                        LOG.info( e.getMessage(), "No info about CPU core" );
                    }

                    String path = String.format( "/rest/v1/peers/%s/resource-hosts/%s", configManager.getPeerId(),
                            resourceHostMetricDto.getHostId() );

                    WebClient client = configManager.getTrustedWebClientWithAuth( path, configManager.getHubIp() );

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
            catch ( PGPException | IOException | KeyStoreException | UnrecoverableKeyException |
                    NoSuchAlgorithmException e )
            {
                LOG.error( "Could not send resource hosts configurations.", e );
            }


            LOG.debug( "Sending resource hosts configurations finished successfully..." );
        }
    }
}
