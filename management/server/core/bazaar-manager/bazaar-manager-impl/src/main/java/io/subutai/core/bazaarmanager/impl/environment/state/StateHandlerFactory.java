package io.subutai.core.bazaarmanager.impl.environment.state;


import io.subutai.core.bazaarmanager.impl.environment.state.create.BuildContainerStateHandler;
import io.subutai.core.bazaarmanager.impl.environment.state.create.CheckNetworkStateHandler;
import io.subutai.core.bazaarmanager.impl.environment.state.create.ConfigureContainerStateHandler;
import io.subutai.core.bazaarmanager.impl.environment.state.create.ConfigureEnvironmentStateHandler;
import io.subutai.core.bazaarmanager.impl.environment.state.create.ExchangeInfoStateHandler;
import io.subutai.core.bazaarmanager.impl.environment.state.create.ReserveNetworkStateHandler;
import io.subutai.core.bazaarmanager.impl.environment.state.create.SetupTunnelStateHandler;
import io.subutai.core.bazaarmanager.impl.environment.state.change.ContainerStateHandler;
import io.subutai.core.bazaarmanager.impl.environment.state.change.DomainStateHandler;
import io.subutai.core.bazaarmanager.impl.environment.state.destroy.DeletePeerStateHandler;
import io.subutai.bazaar.share.dto.environment.EnvironmentPeerDto.PeerState;

import static io.subutai.bazaar.share.dto.environment.EnvironmentPeerDto.PeerState.BUILD_CONTAINER;
import static io.subutai.bazaar.share.dto.environment.EnvironmentPeerDto.PeerState.CHANGE_CONTAINER_STATE;
import static io.subutai.bazaar.share.dto.environment.EnvironmentPeerDto.PeerState.CHECK_NETWORK;
import static io.subutai.bazaar.share.dto.environment.EnvironmentPeerDto.PeerState.CONFIGURE_CONTAINER;
import static io.subutai.bazaar.share.dto.environment.EnvironmentPeerDto.PeerState.CONFIGURE_DOMAIN;
import static io.subutai.bazaar.share.dto.environment.EnvironmentPeerDto.PeerState.CONFIGURE_ENVIRONMENT;
import static io.subutai.bazaar.share.dto.environment.EnvironmentPeerDto.PeerState.DELETE_PEER;
import static io.subutai.bazaar.share.dto.environment.EnvironmentPeerDto.PeerState.EXCHANGE_INFO;
import static io.subutai.bazaar.share.dto.environment.EnvironmentPeerDto.PeerState.RESERVE_NETWORK;
import static io.subutai.bazaar.share.dto.environment.EnvironmentPeerDto.PeerState.SETUP_TUNNEL;


public class StateHandlerFactory
{
    private final StateHandler exchangeInfoStateHandler;

    private final StateHandler reserveNetworkStateHandler;

    private final StateHandler setupTunnelStateHandler;

    private final StateHandler buildContainerStateHandler;

    private final StateHandler configureContainerStateHandler;

    private final StateHandler deletePeerStateHandler;

    private final StateHandler containerStateHandler;

    private final StateHandler domainStateHandler;

    private final StateHandler notFoundStateHandler;

    private final StateHandler configureEnvironmentStateHandler;

    private final StateHandler checkNetworkStateHandler;


    public StateHandlerFactory( Context ctx )
    {
        exchangeInfoStateHandler = new ExchangeInfoStateHandler( ctx );

        reserveNetworkStateHandler = new ReserveNetworkStateHandler( ctx );

        setupTunnelStateHandler = new SetupTunnelStateHandler( ctx );

        buildContainerStateHandler = new BuildContainerStateHandler( ctx );

        configureContainerStateHandler = new ConfigureContainerStateHandler( ctx );

        deletePeerStateHandler = new DeletePeerStateHandler( ctx );

        containerStateHandler = new ContainerStateHandler( ctx );

        domainStateHandler = new DomainStateHandler( ctx );

        notFoundStateHandler = new NotFoundStateHandler( ctx );

        configureEnvironmentStateHandler = new ConfigureEnvironmentStateHandler( ctx );

        checkNetworkStateHandler = new CheckNetworkStateHandler( ctx );
    }


    public StateHandler getHandler( PeerState state )
    {
        StateHandler handler = notFoundStateHandler;

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
        else if ( state == CHANGE_CONTAINER_STATE )
        {
            handler = containerStateHandler;
        }
        else if ( state == CONFIGURE_DOMAIN )
        {
            handler = domainStateHandler;
        }
        else if ( state == DELETE_PEER )
        {
            handler = deletePeerStateHandler;
        }
        else if ( state == CONFIGURE_ENVIRONMENT )
        {
            handler = configureEnvironmentStateHandler;
        }
        else if ( state == CHECK_NETWORK )
        {
            handler = checkNetworkStateHandler;
        }

        return handler;
    }
}
