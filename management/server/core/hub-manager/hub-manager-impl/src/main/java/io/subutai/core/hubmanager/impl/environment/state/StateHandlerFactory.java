package io.subutai.core.hubmanager.impl.environment.state;


import com.google.common.base.Preconditions;

import io.subutai.core.hubmanager.impl.environment.state.build.BuildContainerStateHandler;
import io.subutai.core.hubmanager.impl.environment.state.build.ConfigureContainerStateHandler;
import io.subutai.core.hubmanager.impl.environment.state.build.ExchangeInfoStateHandler;
import io.subutai.core.hubmanager.impl.environment.state.build.ReserveNetworkStateHandler;
import io.subutai.core.hubmanager.impl.environment.state.build.SetupTunnelStateHandler;
import io.subutai.core.hubmanager.impl.environment.state.change.ContainerStateHandler;
import io.subutai.core.hubmanager.impl.environment.state.destroy.DeletePeerStateHandler;
import io.subutai.hub.share.dto.environment.EnvironmentPeerDto.PeerState;

import static io.subutai.hub.share.dto.environment.EnvironmentPeerDto.PeerState.BUILD_CONTAINER;
import static io.subutai.hub.share.dto.environment.EnvironmentPeerDto.PeerState.CHANGE_CONTAINER_STATE;
import static io.subutai.hub.share.dto.environment.EnvironmentPeerDto.PeerState.CONFIGURE_CONTAINER;
import static io.subutai.hub.share.dto.environment.EnvironmentPeerDto.PeerState.DELETE_PEER;
import static io.subutai.hub.share.dto.environment.EnvironmentPeerDto.PeerState.EXCHANGE_INFO;
import static io.subutai.hub.share.dto.environment.EnvironmentPeerDto.PeerState.RESERVE_NETWORK;
import static io.subutai.hub.share.dto.environment.EnvironmentPeerDto.PeerState.SETUP_TUNNEL;


public class StateHandlerFactory
{
    private final StateHandler exchangeInfoStateHandler;

    private final StateHandler reserveNetworkStateHandler;

    private final StateHandler setupTunnelStateHandler;

    private final StateHandler buildContainerStateHandler;

    private final StateHandler configureContainerStateHandler;

    private final StateHandler deletePeerStateHandler;

    private final StateHandler containerStateHandler;


    public StateHandlerFactory( Context ctx )
    {
        exchangeInfoStateHandler = new ExchangeInfoStateHandler( ctx );

        reserveNetworkStateHandler = new ReserveNetworkStateHandler( ctx );

        setupTunnelStateHandler = new SetupTunnelStateHandler( ctx );

        buildContainerStateHandler = new BuildContainerStateHandler( ctx );

        configureContainerStateHandler = new ConfigureContainerStateHandler( ctx );

        deletePeerStateHandler = new DeletePeerStateHandler( ctx );

        containerStateHandler = new ContainerStateHandler( ctx );
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
        else if ( state == BUILD_CONTAINER )
        {
            handler = buildContainerStateHandler;
        }
        else if ( state == CONFIGURE_CONTAINER )
        {
            handler = configureContainerStateHandler;
        }
        else if ( state == DELETE_PEER )
        {
            handler = deletePeerStateHandler;
        }
        else if ( state == CHANGE_CONTAINER_STATE )
        {
            handler = containerStateHandler;
        }

        Preconditions.checkState( handler != null, "No proper state handler found for environment state context" );

        return handler;
    }
}
