package io.subutai.core.hubmanager.impl.environment.state.build;


import io.subutai.common.network.UsedNetworkResources;
import io.subutai.common.security.objects.TokenType;
import io.subutai.core.hubmanager.api.exception.HubManagerException;
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
    protected Object doHandle( EnvironmentPeerDto peerDto ) throws HubManagerException
    {
        try
        {
            logStart();


            EnvironmentPeerDto resultDto = getReservedNetworkResource( peerDto );

            User user = ctx.envUserHelper.handleEnvironmentOwnerCreation( peerDto );
            UserToken token = ctx.identityManager.getUserToken( user.getId() );
            if ( token == null )
            {
                //TODO review to make this temporary renewable token
                token = ctx.identityManager
                        .createUserToken( user, null, null, null, TokenType.PERMANENT.getId(), null );
            }
            resultDto.setEnvOwnerToken( token.getFullToken() );
            resultDto.setEnvOwnerTokenId( user.getAuthId() );

            logEnd();

            return resultDto;
        }
        catch ( Exception e )
        {
            throw new HubManagerException( e );
        }
    }


    private EnvironmentPeerDto getReservedNetworkResource( EnvironmentPeerDto peerDto ) throws HubManagerException
    {
        try
        {
            UsedNetworkResources usedNetworkResources = ctx.localPeer.getUsedNetworkResources();

            peerDto.setVnis( usedNetworkResources.getVnis() );

            peerDto.setContainerSubnets( usedNetworkResources.getContainerSubnets() );

            peerDto.setP2pSubnets( usedNetworkResources.getP2pSubnets() );

            return peerDto;
        }
        catch ( Exception e )
        {
            throw new HubManagerException( e );
        }
    }
}
