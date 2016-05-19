package io.subutai.core.hubmanager.impl.environment.state.build;


import java.util.Map;
import java.util.concurrent.Callable;

import com.google.common.collect.Maps;

import io.subutai.common.environment.HostAddresses;
import io.subutai.common.peer.EnvironmentId;
import io.subutai.common.security.SshKeys;
import io.subutai.core.hubmanager.impl.environment.state.Context;
import io.subutai.core.hubmanager.impl.environment.state.StateHandler;
import io.subutai.core.hubmanager.impl.util.AsyncUtil;
import io.subutai.hub.share.dto.environment.EnvironmentDto;
import io.subutai.hub.share.dto.environment.EnvironmentNodeDto;
import io.subutai.hub.share.dto.environment.EnvironmentNodesDto;
import io.subutai.hub.share.dto.environment.EnvironmentPeerDto;
import io.subutai.hub.share.dto.environment.SSHKeyDto;


// todo refactor
public class ConfigureContainerStateHandler extends StateHandler
{
    public ConfigureContainerStateHandler( Context ctx )
    {
        super( ctx );
    }


    @Override
    protected Object doHandle( EnvironmentPeerDto peerDto ) throws Exception
    {
        EnvironmentDto envDto = ctx.restClient.getStrict( path( "/rest/v1/environments/%s", peerDto ), EnvironmentDto.class );

        peerDto = configureSsh( peerDto, envDto );

        configureHosts( envDto );

        return peerDto;
    }


    @Override
    protected void post( EnvironmentPeerDto peerDto, Object body )
    {
        ctx.restClient.post( path( "/rest/v1/environments/%s/container", peerDto ), body );
    }


    public EnvironmentPeerDto configureSsh( EnvironmentPeerDto peerDto, EnvironmentDto envDto ) throws Exception
    {
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

        final EnvironmentId envId = new EnvironmentId( envDto.getId() );

        AsyncUtil.execute( new Callable<Void>()
        {
            public Void call() throws Exception
            {
                ctx.localPeer.configureSshInEnvironment( envId, sshKeys );

                return null;
            }
        } );

        for ( SSHKeyDto sshKeyDto : peerDto.getEnvironmentInfo().getSshKeys() )
        {
            sshKeyDto.addConfiguredPeer( ctx.localPeer.getId() );
        }

        return peerDto;
    }


    public void configureHosts( EnvironmentDto envDto ) throws Exception
    {
        // <hostname, IPs>
        final Map<String, String> hostAddresses = Maps.newHashMap();

        for ( EnvironmentNodesDto nodesDto : envDto.getNodes() )
        {
            for ( EnvironmentNodeDto nodeDto : nodesDto.getNodes() )
            {
                hostAddresses.put( nodeDto.getHostName(), nodeDto.getIp() );
            }
        }

        final EnvironmentId envId = new EnvironmentId( envDto.getId() );

        AsyncUtil.execute( new Callable<Void>()
        {
            public Void call() throws Exception
            {
                ctx.localPeer.configureHostsInEnvironment( envId, new HostAddresses( hostAddresses ) );

                return null;
            }
        } );
    }
}