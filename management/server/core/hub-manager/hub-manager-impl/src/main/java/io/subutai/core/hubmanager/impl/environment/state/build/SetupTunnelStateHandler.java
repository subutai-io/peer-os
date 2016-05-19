package io.subutai.core.hubmanager.impl.environment.state.build;


import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

import io.subutai.common.environment.RhP2pIp;
import io.subutai.common.peer.EnvironmentId;
import io.subutai.common.protocol.P2pIps;
import io.subutai.core.hubmanager.api.HubPluginException;
import io.subutai.core.hubmanager.impl.entity.RhP2PIpEntity;
import io.subutai.core.hubmanager.impl.environment.state.Context;
import io.subutai.core.hubmanager.impl.environment.state.StateHandler;
import io.subutai.core.hubmanager.impl.http.RestResult;
import io.subutai.core.hubmanager.impl.util.AsyncUtil;
import io.subutai.hub.share.dto.environment.EnvironmentDto;
import io.subutai.hub.share.dto.environment.EnvironmentPeerDto;
import io.subutai.hub.share.dto.environment.EnvironmentPeerRHDto;


public class SetupTunnelStateHandler extends StateHandler
{
    public SetupTunnelStateHandler( Context ctx )
    {
        super( ctx );
    }


    @Override
    protected Object doHandle( EnvironmentPeerDto peerDto ) throws Exception
    {
        EnvironmentDto envDto = ctx.restClient.getStrict( path( "/rest/v1/environments/%s", peerDto ), EnvironmentDto.class );

        return setupTunnel( peerDto, envDto );
    }


    public EnvironmentPeerDto setupTunnel( final EnvironmentPeerDto peerDto, final EnvironmentDto envDto ) throws Exception
    {
        Set<RhP2pIp> setOfP2PIps = new HashSet<>();

        for ( EnvironmentPeerDto peerDt : envDto.getPeers() )
        {
            for ( EnvironmentPeerRHDto rhDto : peerDt.getRhs() )
            {
                setOfP2PIps.add( new RhP2PIpEntity( rhDto.getId(), rhDto.getP2pIp() ) );
            }
        }

        final P2pIps p2pIps = new P2pIps();

        p2pIps.addP2pIps( setOfP2PIps );

        peerDto.setSetupTunnel( false );

        AsyncUtil.execute( new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                ctx.localPeer.setupTunnels( p2pIps, new EnvironmentId( envDto.getId() ) );

                peerDto.setSetupTunnel( true );

                return null;
            }
        } );

        return peerDto;
    }
}