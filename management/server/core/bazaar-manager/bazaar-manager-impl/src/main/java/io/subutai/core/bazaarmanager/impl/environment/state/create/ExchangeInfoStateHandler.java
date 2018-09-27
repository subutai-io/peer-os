package io.subutai.core.bazaarmanager.impl.environment.state.create;


import java.util.Set;

import com.google.common.collect.Sets;

import io.subutai.bazaar.share.dto.UserTokenDto;
import io.subutai.bazaar.share.dto.environment.ContainerStateDto;
import io.subutai.bazaar.share.dto.environment.EnvironmentNodeDto;
import io.subutai.bazaar.share.dto.environment.EnvironmentNodesDto;
import io.subutai.bazaar.share.dto.environment.EnvironmentPeerDto;
import io.subutai.common.environment.Node;
import io.subutai.common.environment.Nodes;
import io.subutai.common.network.UsedNetworkResources;
import io.subutai.common.peer.FitCheckResult;
import io.subutai.common.peer.PeerException;
import io.subutai.core.bazaarmanager.api.exception.BazaarManagerException;
import io.subutai.core.bazaarmanager.impl.environment.state.Context;
import io.subutai.core.bazaarmanager.impl.environment.state.StateHandler;
import io.subutai.core.identity.api.model.User;
import io.subutai.core.identity.api.model.UserToken;


public class ExchangeInfoStateHandler extends StateHandler
{
    private static final String PATH = "/rest/v1/environments/%s/containers";


    public ExchangeInfoStateHandler( Context ctx )
    {
        super( ctx, "Preparing initial data" );
    }


    @Override
    protected Object doHandle( EnvironmentPeerDto peerDto ) throws BazaarManagerException
    {
        try
        {
            logStart();

            checkResources( peerDto );

            EnvironmentPeerDto resultDto = getReservedNetworkResource( peerDto );

            UserToken token =
                    ctx.bazaarManager.getUserToken( peerDto.getEnvironmentInfo().getOwnerId(), peerDto.getPeerId() );

            final User user = ctx.identityManager.getUser( token.getUserId() );

            UserTokenDto userTokenDto = new UserTokenDto();
            userTokenDto.setSsUserId( user.getId() );
            userTokenDto.setEnvId( resultDto.getEnvironmentInfo().getbazaarId() );
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
            throw new BazaarManagerException( e );
        }
    }


    private void checkResources( EnvironmentPeerDto peerDto ) throws BazaarManagerException, PeerException
    {
        EnvironmentNodesDto nodesDto = ctx.restClient.getStrict( path( PATH, peerDto ), EnvironmentNodesDto.class );

        Set<Node> newNodes = Sets.newHashSet();
        Set<String> removedContainers = Sets.newHashSet();

        for ( EnvironmentNodeDto nodeDto : nodesDto.getNodes() )
        {
            if ( nodeDto.getState() == ContainerStateDto.BUILDING )
            {
                newNodes.add( new Node( nodeDto.getHostName(), nodeDto.getContainerName(), nodeDto.getContainerQuota(),
                        ctx.localPeer.getId(), nodeDto.getHostId(), nodeDto.getTemplateId() ) );
            }
            else if ( nodeDto.getState() == ContainerStateDto.ABORTING )
            {
                removedContainers.add( nodeDto.getContainerId() );
            }
        }

        if ( !newNodes.isEmpty() )
        {
            FitCheckResult fitCheckResult =
                    ctx.localPeer.checkResources( new Nodes( newNodes, removedContainers, null ) );

            if ( !fitCheckResult.canFit() )
            {
                throw new BazaarManagerException(
                        String.format( "Peer %s can not accommodate the requested containers:%n%s",
                                ctx.localPeer.getId(), fitCheckResult.getDescription() ) );
            }
        }
    }


    private EnvironmentPeerDto getReservedNetworkResource( EnvironmentPeerDto peerDto ) throws BazaarManagerException
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
            throw new BazaarManagerException( e );
        }
    }


    @Override
    protected String getToken( EnvironmentPeerDto peerDto )
    {
        try
        {
            UserToken userToken = ctx.envUserHelper.getUserTokenFromBazaar( peerDto.getSsUserId() );
            return userToken.getFullToken();
        }
        catch ( Exception e )
        {
            log.error( e.getMessage() );
        }
        return null;
    }
}