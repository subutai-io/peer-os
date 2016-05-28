package io.subutai.core.hubmanager.impl.environment.state.destroy;


import java.security.PrivilegedAction;

import javax.security.auth.Subject;

import io.subutai.common.peer.EnvironmentId;
import io.subutai.core.hubmanager.impl.environment.state.Context;
import io.subutai.core.hubmanager.impl.environment.state.StateHandler;
import io.subutai.core.hubmanager.impl.http.RestResult;
import io.subutai.core.identity.api.model.Session;
import io.subutai.hub.share.dto.environment.EnvironmentPeerDto;


public class DeletePeerStateHandler extends StateHandler
{
    public DeletePeerStateHandler( Context ctx )
    {
        super( ctx, "Deleting peer" );
    }


    @Override
    protected Object doHandle( final EnvironmentPeerDto peerDto ) throws Exception
    {
        logStart();

        EnvironmentId envId = new EnvironmentId( peerDto.getEnvironmentInfo().getId() );

        ctx.localPeer.cleanupEnvironment( envId );

        Session session = ctx.identityManager.login( "token", peerDto.getPeerToken() );

        Subject.doAs( session.getSubject(), new PrivilegedAction<Void>()
        {
            @Override
            public Void run()
            {
                ctx.envUserHelper.handleEnvironmentOwnerDeletion( peerDto );
                return null;
            }
        } );

        logEnd();

        return null;
    }


    @Override
    protected RestResult<Object> post( EnvironmentPeerDto peerDto, Object body )
    {
        return ctx.restClient.delete( path( "/rest/v1/environments/%s/peers/%s", peerDto ) );
    }
}