package io.subutai.core.hubmanager.impl.environment.state.change;


import io.subutai.common.peer.ContainerId;
import io.subutai.common.peer.EnvironmentId;
import io.subutai.common.peer.PeerId;
import io.subutai.core.hubmanager.api.exception.HubManagerException;
import io.subutai.core.hubmanager.impl.environment.state.Context;
import io.subutai.core.hubmanager.impl.environment.state.StateHandler;
import io.subutai.core.hubmanager.api.RestResult;
import io.subutai.hub.share.dto.environment.ContainerStateDto;
import io.subutai.hub.share.dto.environment.EnvironmentDto;
import io.subutai.hub.share.dto.environment.EnvironmentNodeDto;
import io.subutai.hub.share.dto.environment.EnvironmentNodesDto;
import io.subutai.hub.share.dto.environment.EnvironmentPeerDto;


// todo refactor
public class ContainerStateHandler extends StateHandler
{
    public ContainerStateHandler( Context ctx )
    {
        super( ctx, "Container state" );
    }


    @Override
    protected Object doHandle( EnvironmentPeerDto peerDto ) throws HubManagerException
    {
        try
        {
            logStart();

            EnvironmentDto envDto =
                    ctx.restClient.getStrict( path( "/rest/v1/environments/%s", peerDto ), EnvironmentDto.class );

            EnvironmentNodesDto resultDto = null;

            for ( EnvironmentNodesDto nodesDto : envDto.getNodes() )
            {
                PeerId peerId = new PeerId( nodesDto.getPeerId() );

                EnvironmentId envId = new EnvironmentId( nodesDto.getEnvironmentId() );

                if ( nodesDto.getPeerId().equals( ctx.localPeer.getId() ) )
                {
                    for ( EnvironmentNodeDto nodeDto : nodesDto.getNodes() )
                    {
                        ContainerId containerId =
                                new ContainerId( nodeDto.getContainerId(), nodeDto.getHostName(), peerId, envId,
                                        nodeDto.getContainerName() );

                        if ( nodeDto.getState().equals( ContainerStateDto.STOPPING ) )
                        {
                            ctx.localPeer.stopContainer( containerId );

                            nodeDto.setState( ContainerStateDto.STOPPED );
                        }

                        if ( nodeDto.getState().equals( ContainerStateDto.STARTING ) )
                        {
                            ctx.localPeer.startContainer( containerId );

                            nodeDto.setState( ContainerStateDto.RUNNING );
                        }

                        if ( nodeDto.getState().equals( ContainerStateDto.ABORTING ) )
                        {
                            ctx.localPeer.destroyContainer( containerId );

                            nodeDto.setState( ContainerStateDto.FROZEN );
                        }
                    }

                    resultDto = nodesDto;

                    break;
                }
            }

            logEnd();

            return resultDto;
        }
        catch ( Exception e )
        {
            throw new HubManagerException( e );
        }
    }


    @Override
    protected RestResult<Object> post( EnvironmentPeerDto peerDto, Object body )
    {
        return ctx.restClient.post( path( "/rest/v1/environments/%s/peers/%s/container", peerDto ), body );
    }
}