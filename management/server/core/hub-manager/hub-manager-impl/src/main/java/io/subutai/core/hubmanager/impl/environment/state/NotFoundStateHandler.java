package io.subutai.core.hubmanager.impl.environment.state;


import io.subutai.hub.share.dto.environment.EnvironmentPeerDto;


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

        peerDto.setError( "Wrong peer state: " + peerDto.getState() );

        log.error( peerDto.getMessage() );

        logEnd();

        return peerDto;
    }
}
