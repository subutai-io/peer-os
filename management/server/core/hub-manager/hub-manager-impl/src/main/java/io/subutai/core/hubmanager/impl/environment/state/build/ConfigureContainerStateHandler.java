package io.subutai.core.hubmanager.impl.environment.state.build;


import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Maps;

import io.subutai.common.environment.HostAddresses;
import io.subutai.common.peer.EnvironmentId;
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
        super( ctx, "Containers configuration");
    }


    @Override
    protected Object doHandle( EnvironmentPeerDto peerDto ) throws Exception
    {
        logStart();

        EnvironmentDto envDto = ctx.restClient.getStrict( path( "/rest/v1/environments/%s", peerDto ), EnvironmentDto.class );

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
        SshKeys sshKeys = new SshKeys();

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

        ctx.localPeer.configureSshInEnvironment( new EnvironmentId( envDto.getId() ), sshKeys );

        for ( SSHKeyDto sshKeyDto : peerDto.getEnvironmentInfo().getSshKeys() )
        {
            sshKeyDto.addConfiguredPeer( ctx.localPeer.getId() );
        }

        return peerDto;
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
                        nodeDto.getContainerId(), nodeDto.getContainerName(), nodeDto.getHostName(), nodeDto.getState() );

                // Remove network mask "/24" in IP
                String ip = StringUtils.substringBefore( nodeDto.getIp(), "/" );

                hostAddresses.put( nodeDto.getHostName(), ip );
            }
        }

        ctx.localPeer.configureHostsInEnvironment( new EnvironmentId( envDto.getId() ), new HostAddresses( hostAddresses ) );
    }
}