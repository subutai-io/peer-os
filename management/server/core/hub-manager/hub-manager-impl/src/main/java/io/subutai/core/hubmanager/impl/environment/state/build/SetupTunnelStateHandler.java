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
        EnvironmentDto envDto = getEnvironmentDto( peerDto );

        return setupTunnel( peerDto, envDto );
    }


    private EnvironmentDto getEnvironmentDto( EnvironmentPeerDto peerDto ) throws HubPluginException
    {
        String path = String.format( "/rest/v1/environments/%s", peerDto.getEnvironmentInfo().getId() );

        RestResult<EnvironmentDto> restResult = ctx.restClient.get( path, EnvironmentDto.class );

        if ( !restResult.isSuccess() )
        {
            throw new HubPluginException( restResult.getError() );
        }

        return restResult.getEntity();
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
                ctx.peerManager.getLocalPeer().setupTunnels( p2pIps, new EnvironmentId( envDto.getId() ) );

                peerDto.setSetupTunnel( true );

                return null;
            }
        } );

        return peerDto;
    }
}