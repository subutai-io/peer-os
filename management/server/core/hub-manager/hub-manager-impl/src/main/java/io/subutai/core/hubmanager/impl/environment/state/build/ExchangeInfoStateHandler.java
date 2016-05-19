package io.subutai.core.hubmanager.impl.environment.state.build;


import java.util.Date;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.time.DateUtils;

import io.subutai.common.network.UsedNetworkResources;
import io.subutai.common.security.objects.TokenType;
import io.subutai.core.hubmanager.impl.environment.state.Context;
import io.subutai.core.hubmanager.impl.environment.state.StateHandler;
import io.subutai.core.hubmanager.impl.util.AsyncUtil;
import io.subutai.core.identity.api.model.User;
import io.subutai.core.identity.api.model.UserToken;
import io.subutai.hub.share.dto.environment.EnvironmentPeerDto;


public class ExchangeInfoStateHandler extends StateHandler
{
    public ExchangeInfoStateHandler( Context ctx )
    {
        super( ctx );
    }


    @Override
    protected Object doHandle( EnvironmentPeerDto peerDto ) throws Exception
    {
        EnvironmentPeerDto resultDto = getReservedNetworkResource( peerDto );

        resultDto.setEnvOwnerToken( getEnvironmentOwnerToken( peerDto ) );

        return resultDto;
    }


    public EnvironmentPeerDto getReservedNetworkResource( EnvironmentPeerDto peerDto ) throws Exception
    {
        UsedNetworkResources usedNetworkResources = AsyncUtil.execute( new Callable<UsedNetworkResources>()
        {
            @Override
            public UsedNetworkResources call() throws Exception
            {
                return ctx.localPeer.getUsedNetworkResources();
            }
        } );

        peerDto.setVnis( usedNetworkResources.getVnis() );

        peerDto.setContainerSubnets( usedNetworkResources.getContainerSubnets() );

        peerDto.setP2pSubnets( usedNetworkResources.getP2pSubnets() );

        return peerDto;
    }


    private String getEnvironmentOwnerToken( EnvironmentPeerDto peerDto )
    {
        User user = ctx.envUserHelper.handleEnvironmentOwnerCreation( peerDto );

        Date validDate = DateUtils.addYears( new Date(), 3 );

        UserToken token = ctx.identityManager.createUserToken( user, null, null, null, TokenType.Permanent.getId(), validDate );

        return token.getFullToken();
    }

}
