package io.subutai.core.hubmanager.impl.environment.state.change;


import org.apache.commons.lang3.StringUtils;

import io.subutai.common.network.ProxyLoadBalanceStrategy;
import io.subutai.core.hubmanager.impl.environment.state.Context;
import io.subutai.core.hubmanager.impl.environment.state.StateHandler;
import io.subutai.core.hubmanager.impl.http.RestResult;
import io.subutai.hub.share.dto.environment.EnvironmentDto;
import io.subutai.hub.share.dto.environment.EnvironmentInfoDto;
import io.subutai.hub.share.dto.environment.EnvironmentNodeDto;
import io.subutai.hub.share.dto.environment.EnvironmentNodesDto;
import io.subutai.hub.share.dto.environment.EnvironmentPeerDto;


// TODO refactor
public class DomainStateHandler extends StateHandler
{
    public DomainStateHandler( Context ctx )
    {
        super( ctx, "Domain configuration" );
    }


    /**
     * TODO balanceStrategy should come from Hub
     */
    @Override
    protected Object doHandle( EnvironmentPeerDto peerDto ) throws Exception
    {
        logStart();

        EnvironmentDto envDto =
                ctx.restClient.getStrict( path( "/rest/v1/environments/%s", peerDto ), EnvironmentDto.class );

        EnvironmentInfoDto env = peerDto.getEnvironmentInfo();

        if ( StringUtils.isNotEmpty( env.getDomainName() ) )
        {
            ProxyLoadBalanceStrategy balanceStrategy = ProxyLoadBalanceStrategy.LOAD_BALANCE;

            ctx.localPeer.setVniDomain( env.getVni(), env.getDomainName(), balanceStrategy, env.getSslCertPath() );

            for ( EnvironmentNodesDto nodesDto : envDto.getNodes() )
            {
                if ( nodesDto.getPeerId().equals( ctx.localPeer.getId() ) )
                {
                    for ( EnvironmentNodeDto nodeDto : nodesDto.getNodes() )
                    {
                        try
                        {
                            ctx.localPeer.addIpToVniDomain( nodeDto.getIp().replace( "/24", "" ), env.getVni() );
                        }
                        catch ( Exception e )
                        {
                            log.error( "Could not add container IP to domain: " + nodeDto.getContainerName() );
                        }
                    }
                }
            }
        }
        else
        {
            ctx.localPeer.removeVniDomain( env.getVni() );
        }

        logEnd();

        return peerDto;
    }


    @Override
    protected RestResult<Object> post( EnvironmentPeerDto peerDto, Object body )
    {
        return ctx.restClient.post( path( "/rest/v1/environments/%s/peers/%s/domain", peerDto ), body );
    }


    /**
     * This state is allowed to have duplicated handling.
     */
    @Override
    protected void onSuccess( EnvironmentPeerDto peerDto )
    {
    }
}