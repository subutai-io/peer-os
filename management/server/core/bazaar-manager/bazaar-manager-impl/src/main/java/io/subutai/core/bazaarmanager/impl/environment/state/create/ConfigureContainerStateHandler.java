package io.subutai.core.bazaarmanager.impl.environment.state.create;


import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Maps;

import io.subutai.common.environment.Environment;
import io.subutai.common.environment.EnvironmentNotFoundException;
import io.subutai.common.environment.HostAddresses;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.EnvironmentId;
import io.subutai.common.peer.HostNotFoundException;
import io.subutai.common.peer.Peer;
import io.subutai.common.security.SshKey;
import io.subutai.common.security.SshKeys;
import io.subutai.common.settings.Common;
import io.subutai.core.bazaarmanager.api.RestResult;
import io.subutai.core.bazaarmanager.api.exception.BazaarManagerException;
import io.subutai.core.bazaarmanager.impl.environment.state.Context;
import io.subutai.core.bazaarmanager.impl.environment.state.StateHandler;
import io.subutai.bazaar.share.dto.environment.EnvironmentDto;
import io.subutai.bazaar.share.dto.environment.EnvironmentNodeDto;
import io.subutai.bazaar.share.dto.environment.EnvironmentNodesDto;
import io.subutai.bazaar.share.dto.environment.EnvironmentPeerDto;
import io.subutai.bazaar.share.dto.environment.SSHKeyDto;


public class ConfigureContainerStateHandler extends StateHandler
{
    public ConfigureContainerStateHandler( Context ctx )
    {
        super( ctx, "Containers configuration" );
    }


    @Override
    protected Object doHandle( EnvironmentPeerDto peerDto ) throws BazaarManagerException
    {
        try
        {
            logStart();

            EnvironmentDto envDto =ctx.restClient.getStrict( path( "/rest/v1/environments/%s", peerDto ), EnvironmentDto.class );

            peerDto = configureSsh( peerDto, envDto );

            configureHosts( envDto );

            changeHostNames( envDto );

            setQuotas( envDto );

            logEnd();

            return peerDto;
        }
        catch ( BazaarManagerException e )
        {
            throw e;
        }
        catch ( Exception e )
        {
            throw new BazaarManagerException( e );
        }
    }


    @Override
    protected RestResult<Object> post( EnvironmentPeerDto peerDto, Object body )
    {
        return ctx.restClient.post( path( "/rest/v1/environments/%s/container", peerDto ), body );
    }


    private EnvironmentPeerDto configureSsh( EnvironmentPeerDto peerDto, EnvironmentDto envDto )
            throws BazaarManagerException
    {
        try
        {
            EnvironmentId envId = new EnvironmentId( envDto.getId() );

            Environment environment = null;

            try
            {
                environment = ctx.envManager.loadEnvironment( envDto.getId() );
            }
            catch ( EnvironmentNotFoundException e )
            {
                log.info( e.getMessage() );
            }

            boolean isSsEnv = environment != null && !Common.BAZAAR_ID.equals( environment.getPeerId() );

            Set<String> peerSshKeys = getCurrentSshKeys( envId, isSsEnv );

            Set<String> bzrSshKeys = new HashSet<>();

            for ( EnvironmentNodesDto nodesDto : envDto.getNodes() )
            {
                for ( EnvironmentNodeDto nodeDto : nodesDto.getNodes() )
                {
                    if ( nodeDto.getSshKeys() != null )
                    {
                        bzrSshKeys.addAll( trim( nodeDto.getSshKeys() ) );
                    }
                }
            }

            //remove obsolete keys
            Set<String> obsoleteKeys = new HashSet<>();

            obsoleteKeys.addAll( peerSshKeys );

            obsoleteKeys.removeAll( bzrSshKeys );

            removeKeys( envId, obsoleteKeys, isSsEnv );

            //add new keys
            Set<String> newKeys = new HashSet<>();

            if ( isSsEnv )
            {
                //for SS env no need to duplicate keys, it will add to Environment entity
                bzrSshKeys.removeAll( peerSshKeys );
            }

            newKeys.addAll( bzrSshKeys );

            if ( newKeys.isEmpty() )
            {
                return peerDto;
            }

            final SshKeys sshKeys = new SshKeys();

            sshKeys.addStringKeys( newKeys );

            if ( isSsEnv )
            {
                Set<Peer> peers = environment.getPeers();

                for ( final Peer peer : peers )
                {
                    if ( peer.isOnline() )
                    {
                        peer.configureSshInEnvironment( environment.getEnvironmentId(), sshKeys );

                        //add peer to dto
                        for ( SSHKeyDto sshKeyDto : peerDto.getEnvironmentInfo().getSshKeys() )
                        {
                            sshKeyDto.addConfiguredPeer( peer.getId() );
                        }
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

                //add peer to dto
                for ( SSHKeyDto sshKeyDto : peerDto.getEnvironmentInfo().getSshKeys() )
                {
                    sshKeyDto.addConfiguredPeer( ctx.localPeer.getId() );
                }
            }

            return peerDto;
        }
        catch ( Exception e )
        {
            throw new BazaarManagerException( e );
        }
    }


    private Set<String> trim( final Set<String> sshKeys )
    {
        Set<String> trimmed = new HashSet<>();

        if ( sshKeys != null && !sshKeys.isEmpty() )
        {
            for ( String sshKey : sshKeys )
            {
                trimmed.add( sshKey.trim() );
            }
        }

        return trimmed;
    }


    private void removeKeys( EnvironmentId envId, Set<String> obsoleteKeys, boolean isSsEnv )
    {
        try
        {
            for ( String obsoleteKey : obsoleteKeys )
            {
                if ( isSsEnv )
                {
                    ctx.envManager.removeSshKey( envId.getId(), obsoleteKey, false );
                }
                else
                {
                    ctx.localPeer.removeFromAuthorizedKeys( envId, obsoleteKey );
                }
            }
        }
        catch ( Exception e )
        {
            log.error( "Error removing ssh key: {}", e.getMessage() );
        }
    }


    private Set<String> getCurrentSshKeys( EnvironmentId envId, boolean isSsEnv )
    {
        Set<String> currentKeys = new HashSet<>();

        try
        {
            Set<ContainerHost> containers = new HashSet<>();

            if ( isSsEnv )
            {
                Environment environment = ctx.envManager.loadEnvironment( envId.getId() );

                containers.addAll( environment.getContainerHosts() );
            }
            else
            {
                containers.addAll( ctx.localPeer.findContainersByEnvironmentId( envId.getId() ) );
            }

            for ( ContainerHost containerHost : containers )
            {
                SshKeys sshKeys = containerHost.getPeer().getContainerAuthorizedKeys( containerHost.getContainerId() );

                for ( SshKey sshKey : sshKeys.getKeys() )
                {
                    currentKeys.add( sshKey.getPublicKey() );
                }
            }
        }
        catch ( Exception e )
        {
            log.error( "Error getting env ssh keys: {}", e.getMessage() );
        }

        return currentKeys;
    }


    private void configureHosts( EnvironmentDto envDto )
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
            log.error( "Error configuring hosts: {}", e.getMessage() );
        }
    }


    private void changeHostNames( EnvironmentDto envDto )
    {
        for ( EnvironmentNodesDto nodesDto : envDto.getNodes() )
        {
            for ( EnvironmentNodeDto nodeDto : nodesDto.getNodes() )
            {
                try
                {
                    ContainerHost ch = ctx.localPeer.getContainerHostById( nodeDto.getContainerId() );

                    if ( !ch.getHostname().equals( nodeDto.getHostName() ) )
                    {
                        ctx.localPeer.setContainerHostname( ch.getContainerId(), nodeDto.getHostName() );
                    }
                }
                catch ( HostNotFoundException ignore )
                {
                    //this is a remote container
                    //no-op
                }
                catch ( Exception e )
                {
                    log.error( "Error configuring hostnames: {}", e.getMessage() );
                }
            }
        }
    }


    private void setQuotas( EnvironmentDto envDto )
    {
        for ( EnvironmentNodesDto nodesDto : envDto.getNodes() )
        {
            for ( EnvironmentNodeDto nodeDto : nodesDto.getNodes() )
            {
                try
                {
                    ContainerHost ch = ctx.localPeer.getContainerHostById( nodeDto.getContainerId() );

                    ctx.localPeer.setQuota( ch.getContainerId(), nodeDto.getContainerQuota() );
                }
                catch ( HostNotFoundException ignore )
                {
                    //this is a remote container
                    //no-op
                }
                catch ( Exception e )
                {
                    log.error( "Error setting quotas: {}", e.getMessage() );
                }
            }
        }
    }
}