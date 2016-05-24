package io.subutai.core.hubmanager.impl.environment.state;


import io.subutai.hub.share.dto.environment.EnvironmentPeerDto;

import static io.subutai.hub.share.dto.environment.EnvironmentPeerDto.PeerState.ERROR;
import static io.subutai.hub.share.dto.environment.EnvironmentPeerDto.PeerState.READY;
import static io.subutai.hub.share.dto.environment.EnvironmentPeerDto.PeerState.WAIT;


public class NotFoundStateHandler extends StateHandler
{
    public NotFoundStateHandler( Context ctx )
    {
        super( ctx, "Not found state" );
    }


    @Override
    protected Object doHandle( EnvironmentPeerDto peerDto ) throws Exception
    {
        logStart();

        if ( peerDto.getState() == WAIT || peerDto.getState() == READY )
        {
            log.error( "Ignoring peer state: " + peerDto.getState() );
        }
        else
        {
            peerDto.setError( "Wrong peer state: " + peerDto.getState() );

            log.error( peerDto.getMessage() );
        }

        logEnd();

        return peerDto;
    }


    @Override
    protected void post( EnvironmentPeerDto peerDto, Object body )
    {
        if ( peerDto.getState() == ERROR )
        {
            super.post( peerDto, body );
        }
    }
}
