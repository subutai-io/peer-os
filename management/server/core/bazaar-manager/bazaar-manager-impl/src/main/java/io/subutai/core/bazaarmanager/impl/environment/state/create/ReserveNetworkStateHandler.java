package io.subutai.core.bazaarmanager.impl.environment.state.create;


import io.subutai.common.network.NetworkResourceImpl;
import io.subutai.common.network.ReservedNetworkResources;
import io.subutai.common.protocol.P2PConfig;
import io.subutai.common.settings.Common;
import io.subutai.core.bazaarmanager.api.exception.BazaarManagerException;
import io.subutai.core.bazaarmanager.impl.model.RhP2PIpEntity;
import io.subutai.core.bazaarmanager.impl.environment.state.Context;
import io.subutai.core.bazaarmanager.impl.environment.state.StateHandler;
import io.subutai.bazaar.share.dto.environment.EnvironmentInfoDto;
import io.subutai.bazaar.share.dto.environment.EnvironmentPeerDto;
import io.subutai.bazaar.share.dto.environment.EnvironmentPeerRHDto;


public class ReserveNetworkStateHandler extends StateHandler
{
    public ReserveNetworkStateHandler( Context ctx )
    {
        super( ctx, "Setting up networking" );
    }


    @Override
    protected Object doHandle( EnvironmentPeerDto peerDto ) throws BazaarManagerException
    {
        try
        {
            logStart();

            reserveNetwork( peerDto );

            EnvironmentPeerDto resultDto = setupP2P( peerDto );

            logEnd();

            return resultDto;
        }
        catch ( BazaarManagerException e )
        {
            throw e;
        }
        catch ( Exception e )
        {
            throw new BazaarManagerException( e );
        }
    }


    private void reserveNetwork( EnvironmentPeerDto peerDto ) throws BazaarManagerException
    {
        try
        {
            EnvironmentInfoDto envInfo = peerDto.getEnvironmentInfo();

            ReservedNetworkResources networkResources = ctx.localPeer.getReservedNetworkResources();

            if ( networkResources.findByEnvironmentId( envInfo.getId() ) != null )
            {
                // Already done. For example, duplicated request.
                return;
            }

            String subnetWithoutMask = envInfo.getSubnetCidr().replace( "/24", "" );

            NetworkResourceImpl networkResource =
                    new NetworkResourceImpl( envInfo.getId(), envInfo.getVni(), envInfo.getP2pSubnet(),
                            subnetWithoutMask, Common.BAZAAR_ID, peerDto.getEnvironmentInfo().getOwnerName(),
                            peerDto.getEnvironmentInfo().getOwnerId() );

            peerDto.setVlan( ctx.localPeer.reserveNetworkResource( networkResource ) );
        }
        catch ( Exception e )
        {
            throw new BazaarManagerException( e );
        }
    }


    private EnvironmentPeerDto setupP2P( EnvironmentPeerDto peerDto ) throws BazaarManagerException
    {
        try
        {
            EnvironmentInfoDto env = peerDto.getEnvironmentInfo();

            P2PConfig p2pConfig = new P2PConfig( peerDto.getPeerId(), env.getId(), env.getP2pHash(), env.getP2pKey(),
                    env.getP2pTTL() );

            log.info( "peerDto.RHs.size: {}", peerDto.getRhs().size() );

            for ( EnvironmentPeerRHDto rhDto : peerDto.getRhs() )
            {
                log.info( "- rhDto: id={}, p2pIp: {}", rhDto.getId(), rhDto.getP2pIp() );

                p2pConfig.addRhP2pIp( new RhP2PIpEntity( rhDto.getId(), rhDto.getP2pIp() ) );
            }

            ctx.localPeer.joinP2PSwarm( p2pConfig );

            return peerDto;
        }
        catch ( Exception e )
        {
            throw new BazaarManagerException( e );
        }
    }
}