package io.subutai.core.hubmanager.impl.environment.state.change;


import org.apache.commons.lang3.StringUtils;

import io.subutai.common.network.ProxyLoadBalanceStrategy;
import io.subutai.core.hubmanager.api.exception.HubManagerException;
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
    protected Object doHandle( EnvironmentPeerDto peerDto ) throws HubManagerException
    {
        try
        {
            logStart();

            EnvironmentDto envDto =
                    ctx.restClient.getStrict( path( "/rest/v1/environments/%s", peerDto ), EnvironmentDto.class );

            EnvironmentInfoDto env = peerDto.getEnvironmentInfo();

            if ( StringUtils.isNotEmpty( env.getDomainName() ) )
            {
                ProxyLoadBalanceStrategy balanceStrategy = ProxyLoadBalanceStrategy.LOAD_BALANCE;

                String existingDomain = ctx.localPeer.getVniDomain( env.getVni() );

                if ( existingDomain != null )
                {
                    if ( !existingDomain.trim().equalsIgnoreCase( env.getDomainName().trim() ) )
                    {
                        ctx.localPeer.removeVniDomain( env.getVni() );
                        ctx.localPeer.setVniDomain( env.getVni(), env.getDomainName().trim(), balanceStrategy,
                                env.getSslCertPath() );
                    }
                }
                else
                {
                    ctx.localPeer
                            .setVniDomain( env.getVni(), env.getDomainName(), balanceStrategy, env.getSslCertPath() );
                }

                for ( EnvironmentNodesDto nodesDto : envDto.getNodes() )
                {
                    if ( nodesDto.getPeerId().equals( ctx.localPeer.getId() ) )
                    {
                        for ( EnvironmentNodeDto nodeDto : nodesDto.getNodes() )
                        {
                            try
                            {
                                String ip = nodeDto.getIp().replace( "/24", "" );

                                if ( !ctx.localPeer.isIpInVniDomain( ip, env.getVni() ) )
                                {
                                    ctx.localPeer.addIpToVniDomain( ip, env.getVni() );
                                }
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
        catch ( Exception e )
        {
            throw new HubManagerException( e );
        }
    }


    @Override
    protected RestResult<Object> post( EnvironmentPeerDto peerDto, Object body )
    {
        return ctx.restClient.post( path( "/rest/v1/environments/%s/peers/%s/domain", peerDto ), body );
    }
}