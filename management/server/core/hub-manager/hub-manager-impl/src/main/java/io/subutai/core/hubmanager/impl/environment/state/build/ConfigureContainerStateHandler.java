package io.subutai.core.hubmanager.impl.environment.state.build;


import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Maps;

import io.subutai.common.environment.Environment;
import io.subutai.common.environment.EnvironmentNotFoundException;
import io.subutai.common.environment.HostAddresses;
import io.subutai.common.peer.EnvironmentContainerHost;
import io.subutai.common.peer.EnvironmentId;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.security.SshKey;
import io.subutai.common.security.SshKeys;
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
        Set<String> currentSshKeysSet = new HashSet<>();
        boolean isSshKeysCleaned = false;

        EnvironmentId envId = new EnvironmentId( envDto.getId() );

        if ( envDto != null )
        {
            currentSshKeys = getCurrentSshKeys( envDto.getId() );
            currentSshKeysSet = new HashSet<>( Arrays.asList( currentSshKeys ) );
        }

        Set<String> keys = new HashSet<>();
        for ( EnvironmentNodesDto nodesDto : envDto.getNodes() )
        {
            for ( EnvironmentNodeDto nodeDto : nodesDto.getNodes() )
            {
                if ( nodeDto.getSshKeys() != null )
                {
                    Set<String> existingKeys = new HashSet<>();
                    keys.addAll( nodeDto.getSshKeys() );
                    for ( String sshKey : nodeDto.getSshKeys() )
                    {
                        if ( currentSshKeysSet.contains( sshKey ) )
                        {
                            existingKeys.add( sshKey );
                        }
                    }
                    keys.removeAll( existingKeys );
                    removeKeys( envId, nodeDto.getSshKeys(), currentSshKeys, isSshKeysCleaned );
                }
            }
        }

        if ( keys.isEmpty() )
        {
            return peerDto;
        }
        sshKeys.addStringKeys( keys );

        Environment environment = null;
        try
        {
            environment = ctx.envManager.loadEnvironment( envDto.getId() );
        }
        catch ( EnvironmentNotFoundException e )
        {
            log.info( e.getMessage() );
        }

        if ( environment != null && !environment.getPeerId().equals( "hub" ) )
        {
            Set<Peer> peers = environment.getPeers();
            for ( final Peer peer : peers )
            {
                if ( peer.isOnline() )
                {
                    peer.configureSshInEnvironment( environment.getEnvironmentId(), sshKeys );
                }
            }

            for ( SshKey sshKey : sshKeys.getKeys() )
            {
                ctx.envManager.addSshKeyToEnvironmentEntity( environment.getId(), sshKey.getPublicKey() );
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


    private String[] getCurrentSshKeys( String envId )
    {
        String currentKeys = "";
        try
        {
            Environment environment = ctx.envManager.loadEnvironment( envId );

            for ( EnvironmentContainerHost containerHost : environment.getContainerHosts() )
            {
                SshKeys sshKeys = containerHost.getAuthorizedKeys();
                for ( SshKey sshKey : sshKeys.getKeys() )
                {
                    if ( !currentKeys.contains( sshKey.getPublicKey() ) )
                    {
                        currentKeys += sshKey.getPublicKey();
                    }
                }
            }
        }
        catch ( EnvironmentNotFoundException | PeerException e )
        {
            log.info( e.getMessage() );
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

        try
        {
            ctx.localPeer.configureHostsInEnvironment( new EnvironmentId( envDto.getId() ),
                    new HostAddresses( hostAddresses ) );
        }
        catch ( Exception e )
        {
            log.error( e.getMessage() );
        }
    }
}