package io.subutai.core.hubmanager.impl;


import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.Response;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.http.HttpStatus;

import com.google.common.base.Preconditions;

import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.peer.ContainerHost;
import io.subutai.core.hubmanager.api.HubManager;
import io.subutai.core.hubmanager.api.StateLinkProcessor;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.hub.share.dto.environment.ContainerStateDto;
import io.subutai.hub.share.dto.environment.EnvironmentDto;
import io.subutai.hub.share.dto.environment.EnvironmentNodeDto;
import io.subutai.hub.share.dto.environment.EnvironmentNodesDto;
import io.subutai.hub.share.json.JsonUtil;

import static java.lang.String.format;


public class EnvironmentTelemetryProcessor implements Runnable, StateLinkProcessor
{
    private final Logger log = LoggerFactory.getLogger( getClass() );

    private static final String GET_ENV_URL = "/rest/v1/peers/%s/environments";
    private static final String GET_ENV_CONTAINERS_URL = "/rest/v1/environments/%s";
    private static final String PUT_ENV_TELEMETRY_URL = "/rest/v1/environments/%s/telemetry";

    private static final String PING_COMMAND = "ping -c 5 -i 0.2 -w 5 %s";
    private static final String SSH_COMMAND = "ssh root@%s date";
    private static final String PREPARE_FILE = "MD5=`dd bs=1024 count=2 </dev/urandom | tee /tmp/tmpfile`";
    private static final String SCP_FILE_COMMAND = "scp /tmp/tmpfile root@%s:/tmp";
    private static final String DELETE_PREPARED_FILE = "rm /tmp/tmpfile";

    private PeerManager peerManager;
    private ConfigManager configManager;
    private HubManager hubManager;


    public EnvironmentTelemetryProcessor( HubManager hubManager, PeerManager peerManager, ConfigManager configManager )
    {
        this.peerManager = peerManager;
        this.configManager = configManager;
        this.hubManager = hubManager;
    }


    @Override
    public void run()
    {
        if ( hubManager.isRegistered() )
        {
            startProccess( "pingssh" );
        }
    }


    private void startProccess( String tools )
    {
        Set<String> envs = getEnvIds( format( GET_ENV_URL, configManager.getPeerId() ), configManager );

        for ( String envId : envs )
        {
            checkEnvironmentHealth( envId, tools );
        }
    }


    private JSONObject checkEnvironmentHealth( String envId, String tools )
    {
        JSONObject result = new JSONObject();
        Set<ContainerHost> containerHosts = peerManager.getLocalPeer().findContainersByEnvironmentId( envId );
        EnvironmentDto environmentDto = getEnvironmentPeerDto( format( GET_ENV_CONTAINERS_URL, envId ), configManager );

        Preconditions.checkNotNull( environmentDto );

        List<EnvironmentNodesDto> environmentNodeDtoList = environmentDto.getNodes();

        if ( environmentNodeDtoList == null || environmentNodeDtoList.size() < 2 )
        {
            return result;
        }

        for ( EnvironmentNodesDto environmentNodesDto : environmentNodeDtoList )
        {
            for ( ContainerHost sourceContainer : containerHosts )
            {
                for ( EnvironmentNodeDto environmentNodeDto : environmentNodesDto.getNodes() )
                {
                    String ip = environmentNodeDto.getIp().replaceAll( "/24", "" );

                    if ( !sourceContainer.getIp().equals( ip ) && environmentNodeDto.getState().equals(
                            ContainerStateDto.RUNNING ) )
                    {
                        if ( tools.contains( "ping" ) )
                        {
                            executeCheckCommand( "ping", sourceContainer, format( PING_COMMAND, ip ), result, 10 );
                        }
                        if ( tools.contains( "ssh" ) )
                        {
                            executeCheckCommand( "ssh", sourceContainer, format( SSH_COMMAND, ip ), result, 10 );
                        }
                        if ( tools.contains( "scp" ) )
                        {
                            fileManipulation( sourceContainer, PREPARE_FILE );
                            executeCheckCommand( "scp", sourceContainer, format( SCP_FILE_COMMAND, ip ), result, 60 );
                            fileManipulation( sourceContainer, DELETE_PREPARED_FILE );
                        }
                    }
                }
            }
        }

        JSONObject healthData = new JSONObject();
        healthData.put( configManager.getPeerId(), result.toString() );
        sendToHUB( healthData, envId );

        return result;
    }


    private void fileManipulation( ContainerHost sourceContainer, String cmd )
    {
        if ( isChConnected( sourceContainer ) )
        {
            try
            {
                sourceContainer.execute( new RequestBuilder( cmd ).withTimeout( 10 ) );
            }
            catch ( CommandException e )
            {
                log.error( e.getMessage() );
            }
        }
    }


    private void executeCheckCommand( String key, ContainerHost sourceContainer, String cmd, JSONObject result,
                                      int timeout )
    {
        CommandResult res;
        try
        {
            if ( isChConnected( sourceContainer ) )
            {
                res = sourceContainer.execute( new RequestBuilder( cmd ).withTimeout( timeout ) );

                if ( res.hasSucceeded())
                {
                    result.put( key + "status", "SUCCESS" );
                }
                else
                {
                    result.put( key, "exec: " + cmd + " result: " + res.getStdOut() + res.getStdErr() );
                    result.put( key + "status", "FAILED" );
                }
            }
        }
        catch ( Exception e )
        {
            log.error( e.getMessage() );
        }
    }


    private boolean isChConnected( ContainerHost ch )
    {
        boolean exec = true;
        int tryCount = 0;

        while ( exec )
        {
            tryCount++;
            exec = tryCount <= 3;

            if ( !ch.isConnected() )
            {
                return true;
            }

            try
            {
                Thread.sleep( 5000 );
            }
            catch ( InterruptedException e )
            {
                Thread.currentThread().interrupt();
            }
        }

        return true;
    }


    private void sendToHUB( JSONObject healthData, String envId )
    {
        WebClient client = null;
        try
        {
            client = configManager
                    .getTrustedWebClientWithAuth( format( PUT_ENV_TELEMETRY_URL, envId ), configManager.getHubIp() );
            byte[] cborData = JsonUtil.toCbor( healthData );
            byte[] encryptedData = configManager.getMessenger().produce( cborData );
            Response response = client.put( encryptedData );

            if ( response.getStatus() != HttpStatus.SC_OK && response.getStatus() != 204 )
            {
                log.error( "Error to get  environment  ids data from Hub: HTTP {} - {}", response.getStatus(),
                        response.getStatusInfo().getReasonPhrase() );
            }
        }
        catch ( Exception e )
        {
            log.error( "Could not sent  telemetry data to hub.", e.getMessage() );
        }
        finally
        {
            if ( client != null )
            {
                client.close();
            }
        }
    }


    public JSONObject getTelemetry( String link, ConfigManager configManager )
    {
        try
        {
            WebClient client = configManager.getTrustedWebClientWithAuth( link, configManager.getHubIp() );
            Response res = client.get();

            log.debug( "Response: HTTP {} - {}", res.getStatus(), res.getStatusInfo().getReasonPhrase() );

            if ( res.getStatus() != HttpStatus.SC_OK )
            {
                log.error( "Error to get telemetry  data from Hub: HTTP {} - {}", res.getStatus(),
                        res.getStatusInfo().getReasonPhrase() );

                return null;
            }

            byte[] encryptedContent = configManager.readContent( res );

            byte[] plainContent = configManager.getMessenger().consume( encryptedContent );

            return JsonUtil.fromCbor( plainContent, JSONObject.class );
        }
        catch ( Exception e )
        {

            log.error( e.getMessage() );
            return null;
        }
    }


    private Set<String> getEnvIds( String link, ConfigManager configManager )
    {
        try
        {
            WebClient client = configManager.getTrustedWebClientWithAuth( link, configManager.getHubIp() );
            Response res = client.get();

            log.debug( "Response: HTTP {} - {}", res.getStatus(), res.getStatusInfo().getReasonPhrase() );

            if ( res.getStatus() != HttpStatus.SC_OK && res.getStatus() != 204 )
            {
                log.error( "Error to get  environment  ids data from Hub: HTTP {} - {}", res.getStatus(),
                        res.getStatusInfo().getReasonPhrase() );

                return Collections.emptySet();
            }

            byte[] encryptedContent = configManager.readContent( res );

            byte[] plainContent = configManager.getMessenger().consume( encryptedContent );

            return JsonUtil.fromCbor( plainContent, Set.class );
        }
        catch ( Exception e )
        {
            log.error( e.getMessage() );

            return Collections.emptySet();
        }
    }


    private EnvironmentDto getEnvironmentPeerDto( String link, ConfigManager configManager )
    {
        try
        {
            WebClient client = configManager.getTrustedWebClientWithAuth( link, configManager.getHubIp() );
            Response res = client.get();

            log.debug( "Response: HTTP {} - {}", res.getStatus(), res.getStatusInfo().getReasonPhrase() );

            if ( res.getStatus() != HttpStatus.SC_OK && res.getStatus() != 204 )
            {
                log.error( "Error to get environmentPeerDto from Hub: HTTP {} - {}", res.getStatus(),
                        res.getStatusInfo().getReasonPhrase() );

                return null;
            }

            byte[] encryptedContent = configManager.readContent( res );

            byte[] plainContent = configManager.getMessenger().consume( encryptedContent );

            return JsonUtil.fromCbor( plainContent, EnvironmentDto.class );
        }
        catch ( Exception e )
        {
            log.error( e.getMessage() );
            return null;
        }
    }


    @Override
    public boolean processStateLinks( Set<String> stateLinks )
    {

        if ( hubManager.isRegistered() )
        {
            for ( String link : stateLinks )
            {
                processStateLink( link );
            }
        }
        return false;
    }


    private void processStateLink( String link )
    {
        if ( link.contains( "telemetry" ) )
        {
            process( link );
        }
    }


    private void process( String link )
    {
        JSONObject result = getTelemetry( link, configManager );
        checkEnvironmentHealth( result.getString( "envId" ), result.getString( "tools" ) );
    }
}
