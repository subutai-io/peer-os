package io.subutai.core.hubmanager.impl.environment.state;


import io.subutai.hub.share.dto.environment.EnvironmentPeerDto;


/**
 *
 */
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

        String message = "Wrong peer state in environment: " + peerDto.getState();

        log.error( message );

        peerDto.setError( message );

        logEnd();

        return peerDto;
    }
}
