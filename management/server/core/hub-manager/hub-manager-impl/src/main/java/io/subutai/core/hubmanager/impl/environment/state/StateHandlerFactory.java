package io.subutai.core.hubmanager.impl.environment.state;


import com.google.common.base.Preconditions;

import io.subutai.core.hubmanager.impl.ConfigManager;
import io.subutai.core.hubmanager.impl.environment.state.build.ExchangeInfoStateHandler;
import io.subutai.core.hubmanager.impl.processor.EnvironmentUserHelper;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.hub.share.dto.environment.EnvironmentPeerDto.PeerState;

import static io.subutai.hub.share.dto.environment.EnvironmentPeerDto.PeerState.EXCHANGE_INFO;


public class StateHandlerFactory
{
    private final StateHandler exchangeInfoStateHandler;


    public StateHandlerFactory( IdentityManager identityManager, EnvironmentUserHelper envUserHelper, ConfigManager configManager )
    {
        exchangeInfoStateHandler = new ExchangeInfoStateHandler( identityManager, envUserHelper, configManager );
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
