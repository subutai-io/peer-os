package io.subutai.core.hubmanager.impl.environment.state.destroy;


import io.subutai.common.peer.EnvironmentId;
import io.subutai.core.hubmanager.impl.environment.state.Context;
import io.subutai.core.hubmanager.impl.environment.state.StateHandler;
import io.subutai.hub.share.dto.environment.EnvironmentPeerDto;


public class DeletePeerStateHandler extends StateHandler
{
    public DeletePeerStateHandler( Context ctx )
    {
        super( ctx, "Deleting peer" );
    }


    @Override
    protected Object doHandle( EnvironmentPeerDto peerDto ) throws Exception
    {
        logStart();

        EnvironmentId envId = new EnvironmentId( peerDto.getEnvironmentInfo().getId() );

        ctx.localPeer.cleanupEnvironment( envId );

        ctx.envUserHelper.handleEnvironmentOwnerDeletion( peerDto );

        logEnd();

        return null;
    }


    /**
     * Instead of environment owner token, a peer token is needed to remove a Hub user.
     */
    @Override
    protected String getToken( EnvironmentPeerDto peerDto )
    {
        return peerDto.getPeerToken();
    }


    @Override
    protected void post( EnvironmentPeerDto peerDto, Object body )
    {
        ctx.restClient.delete( path( "/rest/v1/environments/%s/peers/%s", peerDto ) );
    }
}