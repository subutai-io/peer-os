package io.subutai.core.bazaarmanager.impl.environment.state;


import io.subutai.core.bazaarmanager.api.exception.BazaarManagerException;
import io.subutai.bazaar.share.dto.environment.EnvironmentPeerDto;


public class NotFoundStateHandler extends StateHandler
{
    public NotFoundStateHandler( Context ctx )
    {
        super( ctx, "Not found state" );
    }


    @Override
    protected Object doHandle( EnvironmentPeerDto peerDto ) throws BazaarManagerException
    {
        logStart();

        peerDto.setError( "Wrong peer state: " + peerDto.getState() );

        log.error( peerDto.getMessage() );

        logEnd();

        return peerDto;
    }
}
