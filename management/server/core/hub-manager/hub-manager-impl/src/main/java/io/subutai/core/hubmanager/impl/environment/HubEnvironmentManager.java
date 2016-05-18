package io.subutai.core.hubmanager.impl.environment;


import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.http.HttpStatus;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;

import io.subutai.common.command.CommandUtil;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.environment.CreateEnvironmentContainersRequest;
import io.subutai.common.environment.CreateEnvironmentContainersResponse;
import io.subutai.common.environment.HostAddresses;
import io.subutai.common.environment.Node;
import io.subutai.common.environment.PrepareTemplatesResponse;
import io.subutai.common.host.HostArchitecture;
import io.subutai.common.peer.ContainerSize;
import io.subutai.common.peer.EnvironmentId;
import io.subutai.common.peer.Host;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.Peer;
import io.subutai.common.security.SshKeys;
import io.subutai.common.settings.Common;
import io.subutai.common.task.CloneRequest;
import io.subutai.common.task.CloneResponse;
import io.subutai.core.environment.api.exception.EnvironmentCreationException;
import io.subutai.core.environment.api.exception.EnvironmentManagerException;
import io.subutai.core.hubmanager.impl.ConfigManager;
import io.subutai.core.hubmanager.impl.CreatePeerTemplatePrepareTask;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.hub.share.dto.environment.ContainerStateDto;
import io.subutai.hub.share.dto.environment.EnvironmentDto;
import io.subutai.hub.share.dto.environment.EnvironmentInfoDto;
import io.subutai.hub.share.dto.environment.EnvironmentNodeDto;
import io.subutai.hub.share.dto.environment.EnvironmentNodesDto;
import io.subutai.hub.share.dto.environment.EnvironmentPeerDto;
import io.subutai.hub.share.dto.environment.EnvironmentPeerLogDto;
import io.subutai.hub.share.dto.environment.SSHKeyDto;
import io.subutai.hub.share.json.JsonUtil;


// TODO: Replace WebClient with HubRestClient.
public class HubEnvironmentManager
{
    private final Logger log = LoggerFactory.getLogger( getClass() );

    private PeerManager peerManager;

    private ConfigManager configManager;


    public HubEnvironmentManager( ConfigManager configManager, PeerManager peerManager )
    {
        this.configManager = configManager;
        this.peerManager = peerManager;
    }


    public EnvironmentPeerDto configureSsh( EnvironmentPeerDto peerDto, EnvironmentDto envDto )
            throws EnvironmentManagerException, EnvironmentCreationException
    {

        final EnvironmentInfoDto env = peerDto.getEnvironmentInfo();
        final LocalPeer localPeer = peerManager.getLocalPeer();
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        ExecutorCompletionService<Peer> completionService = new ExecutorCompletionService<>( executorService );

        final EnvironmentId environmentId = new EnvironmentId( env.getId() );
        final SshKeys sshKeys = new SshKeys();
        for ( EnvironmentNodesDto nodesDto : envDto.getNodes() )
        {
            for ( EnvironmentNodeDto nodeDto : nodesDto.getNodes() )
            {
                if ( nodeDto.getSshKeys() != null )
                {
                    sshKeys.addStringKeys( nodeDto.getSshKeys() );
                }
            }
        }

        completionService.submit( new Callable<Peer>()
        {
            @Override
            public Peer call() throws Exception
            {

                localPeer.configureSshInEnvironment( environmentId, sshKeys );
                return localPeer;
            }
        } );

        try
        {
            Future<Peer> f = completionService.take();
            f.get();

            for ( SSHKeyDto sshKeyDto : env.getSshKeys() )
            {
                sshKeyDto.addConfiguredPeer( localPeer.getId() );
            }
        }
        catch ( Exception e )
        {
            String msg = "Failed to register ssh keys on peer: " + localPeer.getId();
            sendLogToHub( peerDto, msg, e.getMessage(), EnvironmentPeerLogDto.LogEvent.SUBUTAI,
                    EnvironmentPeerLogDto.LogType.ERROR, null );
            log.error( msg, e );
            throw new EnvironmentCreationException( msg );
        }

        return peerDto;
    }


    public void configureHash( EnvironmentPeerDto peerDto, EnvironmentDto envDto )
            throws EnvironmentManagerException, EnvironmentCreationException
    {
        final LocalPeer localPeer = peerManager.getLocalPeer();

        final EnvironmentId environmentId = new EnvironmentId( envDto.getId() );
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        ExecutorCompletionService<Peer> completionService = new ExecutorCompletionService<>( executorService );

        final Map<String, String> hostAddresses = Maps.newHashMap();

        for ( EnvironmentNodesDto nodesDto : envDto.getNodes() )
        {
            for ( EnvironmentNodeDto nodeDto : nodesDto.getNodes() )
            {
                hostAddresses.put( nodeDto.getHostName(), nodeDto.getIp() );
            }
        }
        completionService.submit( new Callable<Peer>()
        {
            @Override
            public Peer call() throws Exception
            {
                localPeer.configureHostsInEnvironment( environmentId, new HostAddresses( hostAddresses ) );
                return localPeer;
            }
        } );

        try
        {
            Future<Peer> f = completionService.take();
            f.get();
        }
        catch ( Exception e )
        {
            String msg = "Problems registering hosts in peer: " + localPeer.getId();
            sendLogToHub( peerDto, msg, e.getMessage(), EnvironmentPeerLogDto.LogEvent.SUBUTAI,
                    EnvironmentPeerLogDto.LogType.ERROR, null );
            log.error( msg, e );
            throw new EnvironmentCreationException( msg );
        }
    }


    public void sendLogToHub( EnvironmentPeerDto peerDto, String msg, String exMsg, EnvironmentPeerLogDto.LogEvent logE,
                              EnvironmentPeerLogDto.LogType logType, String contId )
    {
        try
        {
            String envPeerLogPath =
                    String.format( "/rest/v1/environments/%s/peers/%s/log", peerDto.getEnvironmentInfo().getId(),
                            peerManager.getLocalPeer().getId() );
            WebClient client = configManager.getTrustedWebClientWithAuth( envPeerLogPath, configManager.getHubIp() );

            EnvironmentPeerLogDto peerLogDto = new EnvironmentPeerLogDto( peerDto.getPeerId(), peerDto.getState(),
                    peerDto.getEnvironmentInfo().getId(), logType );
            peerLogDto.setMessage( msg );
            peerLogDto.setExceptionMessage( exMsg );
            peerLogDto.setLogEvent( logE );
            peerLogDto.setContainerId( contId );
            peerLogDto.setLogCode( null );

            byte[] cborData = JsonUtil.toCbor( peerLogDto );
            byte[] encryptedData = configManager.getMessenger().produce( cborData );
            Response r = client.post( encryptedData );
            if ( r.getStatus() == HttpStatus.SC_OK )
            {
                log.debug( "Environment peer log successfully sent to hub" );
            }
        }
        catch ( Exception e )
        {
            log.error( "Could not sent environment peer log to hub.", e.getMessage() );
        }
    }
}
