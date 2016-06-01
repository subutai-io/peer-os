package io.subutai.core.hubmanager.impl.environment.state.build;


import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;

import io.subutai.common.network.UsedNetworkResources;
import io.subutai.common.security.objects.TokenType;
import io.subutai.core.hubmanager.impl.environment.state.Context;
import io.subutai.core.hubmanager.impl.environment.state.StateHandler;
import io.subutai.core.identity.api.model.User;
import io.subutai.core.identity.api.model.UserToken;
import io.subutai.hub.share.dto.environment.EnvironmentPeerDto;


public class ExchangeInfoStateHandler extends StateHandler
{
    public ExchangeInfoStateHandler( Context ctx )
    {
        super( ctx, "Preparing initial data" );
    }


    @Override
    protected Object doHandle( EnvironmentPeerDto peerDto ) throws Exception
    {
        logStart();

        EnvironmentPeerDto resultDto = getReservedNetworkResource( peerDto );

        User user = ctx.envUserHelper.handleEnvironmentOwnerCreation( peerDto );
        UserToken token = ctx.identityManager.getUserToken( user.getId() );
        if ( token == null )
        {
            Date validDate = DateUtils.addYears( new Date(), 3 );
            token = ctx.identityManager
                    .createUserToken( user, null, null, null, TokenType.Permanent.getId(), validDate );
        }
        resultDto.setEnvOwnerToken( token.getFullToken() );
        resultDto.setEnvOwnerTokenId( user.getAuthId() );

        logEnd();

        return resultDto;
    }


    public EnvironmentPeerDto getReservedNetworkResource( EnvironmentPeerDto peerDto ) throws Exception
    {
        UsedNetworkResources usedNetworkResources = ctx.localPeer.getUsedNetworkResources();

        peerDto.setVnis( usedNetworkResources.getVnis() );

        peerDto.setContainerSubnets( usedNetworkResources.getContainerSubnets() );

        peerDto.setP2pSubnets( usedNetworkResources.getP2pSubnets() );

        return peerDto;
    }
}
