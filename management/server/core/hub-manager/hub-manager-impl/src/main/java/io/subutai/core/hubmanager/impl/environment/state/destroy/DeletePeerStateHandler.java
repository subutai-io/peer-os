package io.subutai.core.hubmanager.impl.environment.state.destroy;


import io.subutai.common.peer.EnvironmentId;
import io.subutai.core.hubmanager.impl.environment.state.Context;
import io.subutai.core.hubmanager.impl.environment.state.StateHandler;
import io.subutai.hub.share.dto.environment.EnvironmentPeerDto;


public class DeletePeerStateHandler extends StateHandler
{
    public DeletePeerStateHandler( Context ctx )
    {
        super( ctx );
    }


    @Override
    protected Object doHandle( EnvironmentPeerDto peerDto ) throws Exception
    {
        EnvironmentId envId = new EnvironmentId( peerDto.getEnvironmentInfo().getId() );

        ctx.localPeer.cleanupEnvironment( envId );

        ctx.envUserHelper.handleEnvironmentOwnerDeletion( peerDto );

        ctx.restClient.delete( path( "/rest/v1/environments/%s/peers/%s", peerDto ) );

        return null;
    }


    @Override
    protected void post( EnvironmentPeerDto peerDto, Object body )
    {
    }

}