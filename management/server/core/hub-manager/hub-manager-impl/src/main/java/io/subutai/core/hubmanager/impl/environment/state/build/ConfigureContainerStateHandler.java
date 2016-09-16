package io.subutai.core.hubmanager.impl.environment.state.build;


import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Maps;

import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.environment.Environment;
import io.subutai.common.environment.HostAddresses;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.EnvironmentId;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.security.SshKeys;
import io.subutai.common.util.PeerUtil;
import io.subutai.core.hubmanager.impl.environment.state.Context;
import io.subutai.core.hubmanager.impl.environment.state.StateHandler;
import io.subutai.core.hubmanager.impl.http.RestResult;
import io.subutai.hub.share.dto.environment.EnvironmentDto;
import io.subutai.hub.share.dto.environment.EnvironmentNodeDto;
import io.subutai.hub.share.dto.environment.EnvironmentNodesDto;
import io.subutai.hub.share.dto.environment.EnvironmentPeerDto;
import io.subutai.hub.share.dto.environment.SSHKeyDto;


public class ConfigureContainerStateHandler extends StateHandler
{
    public ConfigureContainerStateHandler( Context ctx )
    {
        super( ctx, "Containers configuration" );
    }


    @Override
    protected Object doHandle( EnvironmentPeerDto peerDto ) throws Exception
    {
        logStart();

        EnvironmentDto envDto =
                ctx.restClient.getStrict( path( "/rest/v1/environments/%s", peerDto ), EnvironmentDto.class );

        peerDto = configureSsh( peerDto, envDto );

        configureHosts( envDto );

        logEnd();

        return peerDto;
    }


    @Override
    protected RestResult<Object> post( EnvironmentPeerDto peerDto, Object body )
    {
        return ctx.restClient.post( path( "/rest/v1/environments/%s/container", peerDto ), body );
    }


    public EnvironmentPeerDto configureSsh( EnvironmentPeerDto peerDto, EnvironmentDto envDto ) throws Exception
    {
        final SshKeys sshKeys = new SshKeys();
        String[] currentSshKeys = null;
        boolean isSshKeysCleaned = false;

        EnvironmentId envId = new EnvironmentId( envDto.getId() );

        if ( envDto != null )
        {
            currentSshKeys = getCurrnetSshKeys( envDto.getId() );
        }

        for ( EnvironmentNodesDto nodesDto : envDto.getNodes() )
        {
            for ( EnvironmentNodeDto nodeDto : nodesDto.getNodes() )
            {
                if ( nodeDto.getSshKeys() != null )
                {
                    sshKeys.addStringKeys( nodeDto.getSshKeys() );
                    removeKeys( envId, nodeDto.getSshKeys(), currentSshKeys, isSshKeysCleaned );
                }
            }
        }

        final Environment environment = ctx.envManager.loadEnvironment( envDto.getId() );
        if ( environment.getPeerId() != null ) //TODO clarify for check initiator peer ID
        {
            Set<Peer> peers = environment.getPeers();
            PeerUtil<Object> keyUtil = new PeerUtil<>();
            for ( final Peer peer : peers )
            {
                keyUtil.addPeerTask( new PeerUtil.PeerTask<>( peer, new Callable<Object>()
                {
                    @Override
                    public Object call() throws Exception
                    {
                        peer.configureSshInEnvironment( environment.getEnvironmentId(), sshKeys );
                        return null;
                    }
                } ) );
            }
        }
        else
        {
            ctx.localPeer.configureSshInEnvironment( envId, sshKeys );
        }

        for ( SSHKeyDto sshKeyDto : peerDto.getEnvironmentInfo().getSshKeys() )
        {
            sshKeyDto.addConfiguredPeer( ctx.localPeer.getId() );
        }

        return peerDto;
    }


    private void removeKeys( EnvironmentId envId, Set<String> sshKeys, String[] currentSshKeys,
                             boolean isSshKeysCleaned )
    {
        if ( isSshKeysCleaned )
        {
            return;
        }

        for ( String sshKey : currentSshKeys )
        {
            if ( !sshKeys.contains( sshKey.trim() ) )
            {
                try
                {
                    ctx.localPeer.removeFromAuthorizedKeys( envId, sshKey );
                }
                catch ( PeerException e )
                {
                    log.error( e.getMessage() );
                }
            }
        }

        isSshKeysCleaned = true;
    }


    private String[] getCurrnetSshKeys( String envId )
    {
        Set<ContainerHost> containerHosts = ctx.localPeer.findContainersByEnvironmentId( envId );
        String currentKeys = "";

        for ( ContainerHost containerHost : containerHosts )
        {
            try
            {
                CommandResult result =
                        containerHost.execute( new RequestBuilder( "sudo cat /root/.ssh/authorized_keys" ) );
                if ( result.getExitCode() == 0 && !currentKeys.contains( result.getStdOut().trim() ) )
                {
                    currentKeys += result.getStdOut().trim();
                }
            }
            catch ( CommandException e )
            {
                log.error( e.getMessage() );
            }
        }

        if ( !currentKeys.isEmpty() )
        {
            return currentKeys.split( System.getProperty( "line.separator" ) );
        }

        return new String[] {};
    }


    public void configureHosts( EnvironmentDto envDto ) throws Exception
    {
        log.info( "Configuring hosts:" );

        // <hostname, IPs>
        final Map<String, String> hostAddresses = Maps.newHashMap();

        for ( EnvironmentNodesDto nodesDto : envDto.getNodes() )
        {
            for ( EnvironmentNodeDto nodeDto : nodesDto.getNodes() )
            {
                log.info( "- noteDto: containerId={}, containerName={}, hostname={}, state={}",
                        nodeDto.getContainerId(), nodeDto.getContainerName(), nodeDto.getHostName(),
                        nodeDto.getState() );

                // Remove network mask "/24" in IP
                String ip = StringUtils.substringBefore( nodeDto.getIp(), "/" );

                hostAddresses.put( nodeDto.getHostName(), ip );
            }
        }

        ctx.localPeer
                .configureHostsInEnvironment( new EnvironmentId( envDto.getId() ), new HostAddresses( hostAddresses ) );
    }
}