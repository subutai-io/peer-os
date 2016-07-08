package io.subutai.core.hubmanager.impl;


import java.util.Set;

import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.http.HttpStatus;

import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.peer.ContainerHost;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.hubmanager.api.HubManager;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.hub.share.dto.environment.EnvironmentPeerDto;
import io.subutai.hub.share.json.JsonUtil;

import static java.lang.String.format;


public class EnvironmentTelemetryProcessor implements Runnable
{
    private final Logger log = LoggerFactory.getLogger( getClass() );

    private static final String GET_ENV_URL = "/rest/v1/peers/%s/environments";
    private static final String GET_ENV_DTO_URL = "/rest/v1/environments/%s/peers/%s";

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
            log.info( "---- tick -----" );
            log.debug( "---- tock -----" );
            log.error( "---------" );
            startProccess();
        }
    }


    private void startProccess()
    {

        Set<String> envs = getEnvIds( format( GET_ENV_URL, configManager.getPeerId() ), configManager );

        for ( String envId : envs )
        {
            log.error( envId );
            log.error( " -- --- -- -- " );

            //            if ( envManager.getEnvironments() != null )
            //            {
            //                log.error( "envs: " + envManager.getEnvironments().size() );
            //            }
            //            else
            //            {
            //                log.error( "No envs" );
            //            }


            checkPing( envId );
        }
    }


    private String checkPing( String envId )
    {
        log.info( "1" );

        Set<ContainerHost> containerHosts = peerManager.getLocalPeer().findContainersByEnvironmentId( envId );

        log.info( "containers size:", containerHosts.size() );

        for ( ContainerHost sourceContainer : containerHosts )
        {
            for ( ContainerHost targetContainer : containerHosts )
            {
                if ( !sourceContainer.getId().equals( targetContainer.getId() ) )
                {
                    CommandResult res = null;
                    try
                    {
                        res = sourceContainer.execute(
                                new RequestBuilder( format( "ping -c 5 -i 0.2 -w 5 %s", targetContainer.getIp() ) ) );
                    }
                    catch ( CommandException e )
                    {
                        log.error( e.getMessage() );
                        e.printStackTrace();
                    }
                    log.error( res.getStdOut() );
                }
            }
        }

        return null;
    }


    private Set<String> getEnvIds( String link, ConfigManager configManager )
    {
        try
        {
            WebClient client = configManager.getTrustedWebClientWithAuth( link, configManager.getHubIp() );
            Response res = client.get();

            log.debug( "Response: HTTP {} - {}", res.getStatus(), res.getStatusInfo().getReasonPhrase() );

            if ( res.getStatus() != HttpStatus.SC_OK )
            {
                log.error( "Error to get  environment  ids data from Hub: HTTP {} - {}", res.getStatus(),
                        res.getStatusInfo().getReasonPhrase() );

                return null;
            }

            byte[] encryptedContent = configManager.readContent( res );

            byte[] plainContent = configManager.getMessenger().consume( encryptedContent );

            return JsonUtil.fromCbor( plainContent, Set.class );
        }
        catch ( Exception e )
        {
            log.error( e.getMessage() );
            return null;
        }
    }


    private EnvironmentPeerDto getEnvironmentPeerDto( String link, ConfigManager configManager )
    {
        try
        {
            WebClient client = configManager.getTrustedWebClientWithAuth( link, configManager.getHubIp() );
            Response res = client.get();

            log.debug( "Response: HTTP {} - {}", res.getStatus(), res.getStatusInfo().getReasonPhrase() );

            if ( res.getStatus() != HttpStatus.SC_OK )
            {
                log.error( "Error to get environmentPeerDto from Hub: HTTP {} - {}", res.getStatus(),
                        res.getStatusInfo().getReasonPhrase() );

                return null;
            }

            byte[] encryptedContent = configManager.readContent( res );

            byte[] plainContent = configManager.getMessenger().consume( encryptedContent );

            return JsonUtil.fromCbor( plainContent, EnvironmentPeerDto.class );
        }
        catch ( Exception e )
        {
            log.error( e.getMessage() );
            return null;
        }
    }
}
