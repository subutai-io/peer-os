package io.subutai.core.strategy.rest;


import java.util.Map;

import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.environment.Blueprint;
import io.subutai.common.environment.Topology;
import io.subutai.hub.share.quota.ContainerSize;
import io.subutai.core.lxc.quota.api.QuotaManager;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.strategy.api.ContainerPlacementStrategy;
import io.subutai.core.strategy.api.StrategyManager;
import io.subutai.hub.share.quota.ContainerQuota;
import io.subutai.hub.share.resource.PeerGroupResources;


/**
 * REST endpoint implementation of strategy manager
 *
 * TODO seems obsolete, check and delete
 */
public class RestServiceImpl implements RestService
{
    private static Logger LOG = LoggerFactory.getLogger( RestServiceImpl.class );

    private StrategyManager strategyManager;
    private PeerManager peerManager;
    private QuotaManager quotaManager;


    public RestServiceImpl()
    {
    }


    public void setStrategyManager( final StrategyManager strategyManager )
    {
        this.strategyManager = strategyManager;
    }


    public void setPeerManager( final PeerManager peerManager )
    {
        this.peerManager = peerManager;
    }


    public void setQuotaManager( final QuotaManager quotaManager )
    {
        this.quotaManager = quotaManager;
    }


    @Override
    public Response distribute( final String strategyId, final Blueprint blueprint )
    {
        try
        {
            final ContainerPlacementStrategy strategy = strategyManager.findStrategyById( strategyId );

            final PeerGroupResources peerGroupResources = peerManager.getPeerGroupResources();
            final Map<ContainerSize, ContainerQuota> quotas = quotaManager.getDefaultQuotas();
            final Topology topology =
                    strategy.distribute( blueprint.getName(), blueprint.getNodes(), peerGroupResources, quotas );
            return Response.ok( topology ).build();
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            return Response.serverError().build();
        }
    }
}
