package io.subutai.core.hubmanager.impl.environment.state.change;


import io.subutai.common.peer.ContainerId;
import io.subutai.common.peer.EnvironmentId;
import io.subutai.common.peer.PeerId;
import io.subutai.core.hubmanager.api.exception.HubManagerException;
import io.subutai.core.hubmanager.impl.environment.state.Context;
import io.subutai.core.hubmanager.impl.environment.state.StateHandler;
import io.subutai.core.hubmanager.api.RestResult;
import io.subutai.core.hubmanager.impl.processor.port_map.DestroyPortMap;
import io.subutai.hub.share.dto.domain.ContainerPortMapDto;
import io.subutai.hub.share.dto.domain.PortMapDto;
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
                            cleanPortMap( nodesDto.getEnvironmentId(), nodeDto.getContainerId() );

                            if ( envDto.isCreatedOnSS() )
                            {
                                ctx.envManager
                                        .destroyContainer( nodeDto.getEnvironmentId(), nodeDto.getContainerId(), true );
                            }
                            else
                            {
                                ctx.localPeer.destroyContainer( containerId );
                            }


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


    private void cleanPortMap( String envId, String contId )
    {
        String url = "/rest/v1/environments/%s/containers/%s/ports/map";
        try
        {
            RestResult<ContainerPortMapDto> result =
                    ctx.restClient.get( String.format( url, envId, contId ), ContainerPortMapDto.class );

            DestroyPortMap destroyPortMap = new DestroyPortMap( ctx );

            for ( PortMapDto portMapDto : result.getEntity().getContainerPorts() )
            {
                destroyPortMap.deleteMap( portMapDto );
            }
        }
        catch ( Exception e )
        {
            log.error( e.getMessage() );
        }
    }


    @Override
    protected RestResult<Object> post( EnvironmentPeerDto peerDto, Object body )
    {
        return ctx.restClient.post( path( "/rest/v1/environments/%s/peers/%s/container", peerDto ), body );
    }
}