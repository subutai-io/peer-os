package io.subutai.core.bazaarmanager.impl;


import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.http.HttpStatus;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.settings.Common;
import io.subutai.core.bazaarmanager.api.BazaarManager;
import io.subutai.core.bazaarmanager.api.BazaarRequester;
import io.subutai.core.bazaarmanager.api.RestClient;
import io.subutai.core.bazaarmanager.api.RestResult;
import io.subutai.core.bazaarmanager.api.StateLinkProcessor;
import io.subutai.core.bazaarmanager.api.exception.BazaarManagerException;
import io.subutai.core.bazaarmanager.impl.util.Utils;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.bazaar.share.dto.environment.ContainerStateDto;
import io.subutai.bazaar.share.dto.environment.EnvironmentDto;
import io.subutai.bazaar.share.dto.environment.EnvironmentNodeDto;
import io.subutai.bazaar.share.dto.environment.EnvironmentNodesDto;

import static java.lang.String.format;


public class EnvironmentTelemetryProcessor extends BazaarRequester implements StateLinkProcessor
{
    private static final String GET_ENV_URL = "/rest/v1/peers/%s/environments";
    private static final String GET_ENV_CONTAINERS_URL = "/rest/v1/environments/%s";
    private static final String PUT_ENV_TELEMETRY_URL = "/rest/v1/environments/%s/telemetry";

    private static final String PING_COMMAND = "ping -c 5 %s";
    private static final String SSH_COMMAND = "ssh root@%s date";
    private static final String PREPARE_FILE = "MD5=`dd bs=1024 count=2 </dev/urandom | tee /tmp/tmpfile`";
    private static final String SCP_FILE_COMMAND = "scp /tmp/tmpfile root@%s:/tmp";
    private static final String DELETE_PREPARED_FILE = "rm /tmp/tmpfile";

    private static final Set<String> LINKS_IN_PROGRESS = Sets.newConcurrentHashSet();

    private final Logger log = LoggerFactory.getLogger( getClass() );

    private PeerManager peerManager;
    private ConfigManager configManager;


    EnvironmentTelemetryProcessor( final BazaarManager bazaarManager, final PeerManager peerManager,
                                   final ConfigManager configManager, final RestClient restClient )
    {
        super( bazaarManager, restClient );

        this.peerManager = peerManager;
        this.configManager = configManager;
    }


    @Override
    public void request()
    {
        startProcess();
    }


    private void startProcess()
    {
        Set envs = getEnvIds( format( GET_ENV_URL, configManager.getPeerId() ) );

        for ( Object envId : envs )
        {
            checkEnvironmentHealth( ( String ) envId, "pingssh" );
        }
    }


    private void checkEnvironmentHealth( String envId, String tools )
    {
        JSONObject result = new JSONObject();
        Set<ContainerHost> containerHosts = peerManager.getLocalPeer().findContainersByEnvironmentId( envId );
        EnvironmentDto environmentDto = getEnvironmentPeerDto( format( GET_ENV_CONTAINERS_URL, envId ) );

        Preconditions.checkNotNull( environmentDto );

        List<EnvironmentNodesDto> environmentNodeDtoList = environmentDto.getNodes();

        if ( environmentNodeDtoList == null || environmentNodeDtoList.size() < 2 )
        {
            return;
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
                            executeCheckCommand( "ping", sourceContainer, format( PING_COMMAND, ip ), result,
                                    Common.DEFAULT_EXECUTOR_REQUEST_TIMEOUT_SEC * 2 );
                        }
                        if ( tools.contains( "ssh" ) )
                        {
                            executeCheckCommand( "ssh", sourceContainer, format( SSH_COMMAND, ip ), result,
                                    Common.DEFAULT_EXECUTOR_REQUEST_TIMEOUT_SEC );
                        }
                        if ( tools.contains( "scp" ) )
                        {
                            fileManipulation( sourceContainer, PREPARE_FILE );
                            executeCheckCommand( "scp", sourceContainer, format( SCP_FILE_COMMAND, ip ), result,
                                    Common.DEFAULT_EXECUTOR_REQUEST_TIMEOUT_SEC * 6 );
                            fileManipulation( sourceContainer, DELETE_PREPARED_FILE );
                        }
                    }
                }
            }
        }

        JSONObject healthData = new JSONObject();
        healthData.put( configManager.getPeerId(), result.toString() );
        sendToBazaar( healthData, envId );
    }


    private void fileManipulation( ContainerHost sourceContainer, String cmd )
    {
        if ( Utils.waitTillConnects( sourceContainer, 15 ) )
        {
            try
            {
                sourceContainer.execute( new RequestBuilder( cmd ) );
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
            if ( Utils.waitTillConnects( sourceContainer, 15 ) )
            {
                res = sourceContainer.execute( new RequestBuilder( cmd ).withTimeout( timeout ) );

                if ( res.hasSucceeded() )
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


    private void sendToBazaar( JSONObject healthData, String envId )
    {
        try
        {
            RestResult restResult = restClient.put( format( PUT_ENV_TELEMETRY_URL, envId ), healthData, Object.class );

            if ( restResult.getStatus() != HttpStatus.SC_OK && restResult.getStatus() != 204 )
            {
                log.error( "Error to get environment ids data from Bazaar: HTTP {} - {}", restResult.getStatus(),
                        restResult.getError() );
            }
        }
        catch ( Exception e )
        {
            log.error( "Could not sent  telemetry data to Bazaar", e.getMessage() );
        }
    }


    private JSONObject getTelemetry( String link )
    {
        try
        {
            RestResult<JSONObject> restResult = restClient.get( link, JSONObject.class );

            log.debug( "Response: HTTP {} - {}", restResult.getStatus(), restResult.getReasonPhrase() );

            if ( restResult.getStatus() != HttpStatus.SC_OK )
            {
                log.error( "Error to get telemetry data from Bazaar: HTTP {} - {}", restResult.getStatus(),
                        restResult.getError() );

                return null;
            }

            return restResult.getEntity();
        }
        catch ( Exception e )
        {

            log.error( e.getMessage() );
            return null;
        }
    }


    private Set getEnvIds( String link )
    {
        try
        {
            RestResult<Set> restResult = restClient.get( link, Set.class );

            log.debug( "Response: HTTP {} - {}", restResult.getStatus(), restResult.getReasonPhrase() );

            if ( restResult.getStatus() != HttpStatus.SC_OK && restResult.getStatus() != 204 )
            {
                log.error( "Error to get environment ids data from Bazaar: HTTP {} - {}", restResult.getStatus(),
                        restResult.getError() );

                return Collections.emptySet();
            }

            return restResult.getEntity();
        }
        catch ( Exception e )
        {
            log.error( e.getMessage() );

            return Collections.emptySet();
        }
    }


    private EnvironmentDto getEnvironmentPeerDto( String link )
    {
        try
        {
            RestResult<EnvironmentDto> restResult = restClient.get( link, EnvironmentDto.class );

            log.debug( "Response: HTTP {} - {}", restResult.getStatus(), restResult.getReasonPhrase() );

            if ( restResult.getStatus() != HttpStatus.SC_OK && restResult.getStatus() != 204 )
            {
                log.error( "Error to get environmentPeerDto from Bazaar: HTTP {} - {}", restResult.getStatus(),
                        restResult.getError() );

                return null;
            }

            return restResult.getEntity();
        }
        catch ( Exception e )
        {
            log.error( e.getMessage() );
            return null;
        }
    }


    @Override
    public boolean processStateLinks( Set<String> stateLinks ) throws BazaarManagerException
    {
        for ( String link : stateLinks )
        {
            processStateLink( link );
        }

        return false;
    }


    private void processStateLink( String link ) throws BazaarManagerException
    {
        if ( !link.contains( "telemetry" ) )
        {
            return;
        }

        log.info( "Link process - START: {}", link );

        if ( LINKS_IN_PROGRESS.contains( link ) )
        {
            log.info( "This link is in progress: {}", link );

            return;
        }

        LINKS_IN_PROGRESS.add( link );

        try
        {
            process( link );
        }
        catch ( Exception e )
        {
            throw new BazaarManagerException( e );
        }
        finally
        {
            log.info( "Link process - END: {}", link );

            LINKS_IN_PROGRESS.remove( link );
        }
    }


    private void process( String link )
    {
        JSONObject result = getTelemetry( link );

        Preconditions.checkNotNull( result );

        checkEnvironmentHealth( result.getString( "envId" ), result.getString( "tools" ) );
    }
}
