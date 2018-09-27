package io.subutai.core.bazaarmanager.impl.environment.state.create;


import java.util.HashSet;
import java.util.Set;

import io.subutai.common.environment.RhP2pIp;
import io.subutai.common.peer.EnvironmentId;
import io.subutai.common.protocol.P2pIps;
import io.subutai.core.bazaarmanager.api.exception.BazaarManagerException;
import io.subutai.core.bazaarmanager.impl.environment.state.Context;
import io.subutai.core.bazaarmanager.impl.environment.state.StateHandler;
import io.subutai.core.bazaarmanager.impl.model.RhP2PIpEntity;
import io.subutai.bazaar.share.dto.environment.EnvironmentDto;
import io.subutai.bazaar.share.dto.environment.EnvironmentPeerDto;
import io.subutai.bazaar.share.dto.environment.EnvironmentPeerRHDto;


public class SetupTunnelStateHandler extends StateHandler
{
    public SetupTunnelStateHandler( Context ctx )
    {
        super( ctx, "Setting up tunnel" );
    }


    @Override
    protected Object doHandle( EnvironmentPeerDto peerDto ) throws BazaarManagerException
    {
        try
        {
            logStart();

            EnvironmentDto envDto =
                    ctx.restClient.getStrict( path( "/rest/v1/environments/%s", peerDto ), EnvironmentDto.class );

            EnvironmentPeerDto resultDto = setupTunnel( peerDto, envDto );

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


    public EnvironmentPeerDto setupTunnel( final EnvironmentPeerDto peerDto, final EnvironmentDto envDto )
            throws BazaarManagerException
    {
        try
        {
            Set<RhP2pIp> setOfP2PIps = new HashSet<>();

            for ( EnvironmentPeerDto peerDt : envDto.getPeers() )
            {
                for ( EnvironmentPeerRHDto rhDto : peerDt.getRhs() )
                {
                    setOfP2PIps.add( new RhP2PIpEntity( rhDto.getId(), rhDto.getP2pIp() ) );
                }
            }

            P2pIps p2pIps = new P2pIps();

            p2pIps.addP2pIps( setOfP2PIps );

            ctx.localPeer.setupTunnels( p2pIps, new EnvironmentId( envDto.getId() ) );

            peerDto.setSetupTunnel( true );

            return peerDto;
        }
        catch ( Exception e )
        {
            throw new BazaarManagerException( e );
        }
    }
}