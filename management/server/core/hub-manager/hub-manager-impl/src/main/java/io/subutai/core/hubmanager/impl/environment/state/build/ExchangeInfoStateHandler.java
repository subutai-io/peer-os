package io.subutai.core.hubmanager.impl.environment.state.build;


import io.subutai.common.network.UsedNetworkResources;
import io.subutai.core.hubmanager.api.exception.HubManagerException;
import io.subutai.core.hubmanager.impl.environment.state.Context;
import io.subutai.core.hubmanager.impl.environment.state.StateHandler;
import io.subutai.core.identity.api.model.User;
import io.subutai.core.identity.api.model.UserToken;
import io.subutai.hub.share.dto.UserTokenDto;
import io.subutai.hub.share.dto.environment.EnvironmentPeerDto;


public class ExchangeInfoStateHandler extends StateHandler
{
    private static final String PATH = "/rest/v1/environments/%s/containers";


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

            UserToken token =
                    ctx.hubManager.getUserToken( peerDto.getEnvironmentInfo().getOwnerId(), peerDto.getPeerId() );

            final User user = ctx.identityManager.getUser( token.getUserId() );

            //            User user = ctx.envUserHelper.handleEnvironmentOwnerCreation( peerDto );
            //            UserToken token = ctx.identityManager.getUserToken( user.getId() );
            //            if ( token == null )
            //            {
            //                token = ctx.identityManager.createUserToken( user, null, null, null, TokenType.SESSION
            // .getId(), null );
            //            }

            UserTokenDto userTokenDto = new UserTokenDto();
            userTokenDto.setSsUserId( user.getId() );
            userTokenDto.setEnvId( resultDto.getEnvironmentInfo().getHubId() );
            userTokenDto.setAuthId( user.getAuthId() );
            userTokenDto.setToken( token.getFullToken() );
            userTokenDto.setTokenId( token.getTokenId() );
            userTokenDto.setValidDate( token.getValidDate() );
            userTokenDto.setType( UserTokenDto.Type.ENV_USER );
            userTokenDto.setState( UserTokenDto.State.READY );
            resultDto.setUserToken( userTokenDto );

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


    @Override
    protected String getToken( EnvironmentPeerDto peerDto )
    {
        try
        {
            UserToken userToken = ctx.envUserHelper.getUserTokenFromHub( peerDto.getSsUserId() );
            return userToken.getFullToken();
        }
        catch ( Exception e )
        {
            log.error( e.getMessage() );
        }
        return null;
    }
}
