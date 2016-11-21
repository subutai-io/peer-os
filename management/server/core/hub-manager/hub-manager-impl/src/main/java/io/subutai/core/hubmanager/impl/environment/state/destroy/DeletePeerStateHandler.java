package io.subutai.core.hubmanager.impl.environment.state.destroy;


import io.subutai.common.environment.Environment;
import io.subutai.common.environment.EnvironmentNotFoundException;
import io.subutai.common.peer.EnvironmentId;
import io.subutai.common.peer.PeerException;
import io.subutai.common.settings.Common;
import io.subutai.core.environment.api.exception.EnvironmentDestructionException;
import io.subutai.core.hubmanager.api.exception.HubManagerException;
import io.subutai.core.hubmanager.impl.environment.state.Context;
import io.subutai.core.hubmanager.impl.environment.state.StateHandler;
import io.subutai.core.hubmanager.impl.http.RestResult;
import io.subutai.hub.share.dto.environment.EnvironmentPeerDto;


public class DeletePeerStateHandler extends StateHandler
{
    public DeletePeerStateHandler( Context ctx )
    {
        super( ctx, "Deleting peer" );
    }


    @Override
    protected Object doHandle( EnvironmentPeerDto peerDto ) throws HubManagerException
    {
        try
        {
            logStart();

            Environment env = getEnvironment( peerDto );

            log.info( "env: {}", env );

            boolean isHubEnvironment = env == null || Common.HUB_PEER_ID.equals( env.getPeerId() );

            if ( isHubEnvironment )
            {
                deleteHubEnvironment( peerDto );
            }
            else
            {
                deleteLocalEnvironment( env );
            }

            logEnd();

            return null;
        }
        catch ( Exception e )
        {
            throw new HubManagerException( e );
        }
    }


    private void deleteLocalEnvironment( Environment env )
            throws EnvironmentDestructionException, EnvironmentNotFoundException
    {
        ctx.envManager.destroyEnvironment( env.getId(), false );
    }


    private Environment getEnvironment( EnvironmentPeerDto peerDto )
    {
        String envId = peerDto.getEnvironmentInfo().getId();

        for ( Environment env : ctx.envManager.getEnvironments() )
        {
            if ( envId.equals( env.getId() ) )
            {
                return env;
            }
        }

        return null;
    }


    private void deleteHubEnvironment( EnvironmentPeerDto peerDto ) throws PeerException
    {
        EnvironmentId envId = new EnvironmentId( peerDto.getEnvironmentInfo().getId() );

        ctx.localPeer.cleanupEnvironment( envId );

        ctx.envManager.notifyOnEnvironmentDestroyed( envId.getId() );

        ctx.envUserHelper.handleEnvironmentOwnerDeletion( peerDto );
    }


    @Override
    protected String getToken( EnvironmentPeerDto peerDto )
    {
        return peerDto.getPeerToken();
    }


    @Override
    protected RestResult<Object> post( EnvironmentPeerDto peerDto, Object body )
    {
        return ctx.restClient.delete( path( "/rest/v1/environments/%s/peers/%s", peerDto ) );
    }
}