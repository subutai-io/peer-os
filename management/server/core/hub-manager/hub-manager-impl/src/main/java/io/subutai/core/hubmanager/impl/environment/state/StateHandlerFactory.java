package io.subutai.core.hubmanager.impl.environment.state;


import com.google.common.base.Preconditions;

import io.subutai.core.hubmanager.impl.environment.state.build.ExchangeInfoStateHandler;
import io.subutai.hub.share.dto.environment.EnvironmentPeerDto.PeerState;

import static io.subutai.hub.share.dto.environment.EnvironmentPeerDto.PeerState.EXCHANGE_INFO;


public class StateHandlerFactory
{
    private final StateHandler exchangeInfoStateHandler;


    public StateHandlerFactory( Context ctx )
    {
        exchangeInfoStateHandler = new ExchangeInfoStateHandler( ctx );
    }


    public StateHandler getHandler( PeerState state )
    {
        StateHandler handler = null;

        if ( state == EXCHANGE_INFO )
        {
            handler = exchangeInfoStateHandler;
        }

        Preconditions.checkState( handler != null, "No state handler found for environment state context" );

        return handler;
    }
}
