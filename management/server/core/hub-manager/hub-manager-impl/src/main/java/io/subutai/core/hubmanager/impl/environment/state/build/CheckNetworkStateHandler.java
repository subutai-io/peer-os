package io.subutai.core.hubmanager.impl.environment.state.build;


import java.util.HashSet;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.subutai.common.command.CommandResult;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.HostNotFoundException;
import io.subutai.common.peer.ResourceHost;
import io.subutai.core.hubmanager.api.exception.HubManagerException;
import io.subutai.core.hubmanager.impl.environment.state.Context;
import io.subutai.core.hubmanager.impl.environment.state.StateHandler;
import io.subutai.hub.share.dto.environment.EnvironmentInfoDto;
import io.subutai.hub.share.dto.environment.EnvironmentNodeDto;
import io.subutai.hub.share.dto.environment.EnvironmentNodesDto;
import io.subutai.hub.share.dto.environment.EnvironmentPeerDto;
import io.subutai.hub.share.dto.environment.EnvironmentTelemetryDto;
import io.subutai.hub.share.dto.environment.EnvironmentTelemetryOperation;
import io.subutai.hub.share.dto.environment.P2PStatusDto;


public class CheckNetworkStateHandler extends StateHandler
{
    private static final String PATH = "/rest/v1/environments/%s/containers";


    public CheckNetworkStateHandler( Context ctx )
    {
        super( ctx, "Check networking" );
    }


    @Override
    protected Object doHandle( EnvironmentPeerDto peerDto ) throws HubManagerException
    {
        logStart();

        checkP2PConnections( peerDto );

        checkHostConnections( peerDto );

        logEnd();

        return peerDto;
    }


    private void checkHostConnections( EnvironmentPeerDto peerDto )
    {

        ContainerHost containerHost = null;

        RequestBuilder command = null;

        CommandResult result = null;

        EnvironmentTelemetryDto etDto = peerDto.getEnvironmentTelemetryDto();

        for ( EnvironmentTelemetryOperation eto : etDto.getOperations() )
        {
            containerHost = getContainerHost( eto );

            if ( containerHost == null )
            {
                continue;
            }

            command = getCommand( eto );

            try
            {
                result = containerHost.execute( command );
            }
            catch ( Exception e )
            {
                log.error( e.getMessage() );
            }

            updateResult( eto, result );
        }
    }


    private void checkP2PConnections( final EnvironmentPeerDto peerDto )
    {
        CommandResult result = null;

        EnvironmentNodesDto nodesDto = null;

        try
        {
            nodesDto = ctx.restClient.getStrict( path( PATH, peerDto ), EnvironmentNodesDto.class );
        }
        catch ( HubManagerException e )
        {
            log.error( e.getMessage() );
        }

        EnvironmentInfoDto environmentInfoDto = peerDto.getEnvironmentInfo();

        List<EnvironmentNodeDto> nodes = nodesDto.getNodes();

        try
        {
            ResourceHost host = null;

            for ( EnvironmentNodeDto node : nodes )
            {
                host = ctx.localPeer.getResourceHostById( node.getHostId() );

                result = host.execute(
                        new RequestBuilder( "subutai p2p" ).withCmdArgs( "-s", environmentInfoDto.getP2pHash() ) );

                putResults( result, peerDto );
            }
        }
        catch ( Exception e )
        {
            log.error( e.getMessage() );
        }
    }


    private void putResults( CommandResult result, EnvironmentPeerDto peerDto )
    {
        ObjectMapper mapper = new ObjectMapper();

        if ( !result.hasSucceeded() )
        {
            log.error( result.getStdErr() );
        }

        JSONArray array = new JSONArray( result.getStdOut() );

        for ( int i = 0; i < array.length(); i++ )
        {
            JSONObject json = array.getJSONObject( i );

            P2PStatusDto dto = null;

            try
            {
                dto = mapper.readValue( json.toString(), P2PStatusDto.class );
                if ( peerDto.getP2pSubnets() == null )
                {
                    peerDto.setP2pStatuses( new HashSet<P2PStatusDto>() );
                }
                peerDto.getP2pStatuses().add( dto );
            }
            catch ( Exception e )
            {
                log.error( e.getMessage() );
            }
        }
    }


    private void updateResult( EnvironmentTelemetryOperation eto, CommandResult result )
    {
        if ( result == null || eto == null )
        {
            return;
        }

        if ( result.hasSucceeded() )
        {
            eto.setState( EnvironmentTelemetryOperation.State.SUCCESS );
            eto.setLogs( result.getStdOut() );
        }
        else
        {
            eto.setState( EnvironmentTelemetryOperation.State.FAILED );
            eto.setLogs( result.getStdErr() );
        }
    }


    private RequestBuilder getCommand( EnvironmentTelemetryOperation eto )
    {
        String cmd = String.format( eto.getCommand(), eto.getTargetHost() );

        return new RequestBuilder( cmd ).withTimeout( eto.getTimeout() );
    }


    private ContainerHost getContainerHost( EnvironmentTelemetryOperation eto )
    {
        try
        {
            return ctx.localPeer.getContainerHostById( eto.getContainerId() );
        }
        catch ( HostNotFoundException e )
        {
            log.error( e.getMessage() );
            return null;
        }
    }
}
