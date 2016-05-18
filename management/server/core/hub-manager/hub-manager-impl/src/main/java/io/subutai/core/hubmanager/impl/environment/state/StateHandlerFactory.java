package io.subutai.core.hubmanager.impl.environment.state;


import com.google.common.base.Preconditions;

import io.subutai.core.hubmanager.impl.environment.state.build.ExchangeInfoStateHandler;
import io.subutai.core.hubmanager.impl.environment.state.build.ReserveNetworkStateHandler;
import io.subutai.core.hubmanager.impl.environment.state.build.SetupTunnelStateHandler;
import io.subutai.hub.share.dto.environment.EnvironmentPeerDto.PeerState;

import static io.subutai.hub.share.dto.environment.EnvironmentPeerDto.PeerState.EXCHANGE_INFO;
import static io.subutai.hub.share.dto.environment.EnvironmentPeerDto.PeerState.RESERVE_NETWORK;
import static io.subutai.hub.share.dto.environment.EnvironmentPeerDto.PeerState.SETUP_TUNNEL;


public class StateHandlerFactory
{
    private final StateHandler exchangeInfoStateHandler;

    private final StateHandler reserveNetworkStateHandler;

    private final StateHandler setupTunnelStateHandler;


    public StateHandlerFactory( Context ctx )
    {
        exchangeInfoStateHandler = new ExchangeInfoStateHandler( ctx );

        reserveNetworkStateHandler = new ReserveNetworkStateHandler( ctx );

        setupTunnelStateHandler = new SetupTunnelStateHandler( ctx );
    }


    public StateHandler getHandler( PeerState state )
    {
        StateHandler handler = null;

        if ( state == EXCHANGE_INFO )
        {
            handler = exchangeInfoStateHandler;
        }
        else if ( state == RESERVE_NETWORK )
        {
            handler = reserveNetworkStateHandler;
        }
        else if ( state == SETUP_TUNNEL )
        {
            handler = setupTunnelStateHandler;
        }

        Preconditions.checkState( handler != null, "No state handler found for environment state context" );

        return handler;
    }
}
