package io.subutai.core.strategy.cli;


import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import io.subutai.common.environment.Node;
import io.subutai.common.environment.Topology;
import io.subutai.common.peer.ContainerSize;
import io.subutai.common.peer.PeerException;
import io.subutai.common.quota.ContainerQuota;
import io.subutai.common.resource.PeerGroupResources;
import io.subutai.common.util.JsonUtil;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.core.lxc.quota.api.QuotaManager;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.strategy.api.ContainerPlacementStrategy;
import io.subutai.core.strategy.api.StrategyManager;
import io.subutai.core.strategy.api.StrategyNotFoundException;


@Command( scope = "strategy", name = "distribute" )
public class DistributeCommand extends SubutaiShellCommandSupport
{
    private static final Logger LOGGER = LoggerFactory.getLogger( DistributeCommand.class );
    private StrategyManager strategyManager;
    private PeerManager peerManager;
    private QuotaManager quotaManager;

    @Argument( index = 0, name = "strategyId", required = true, multiValued = false,
            description = "Identifier of distribution strategy" )
    private String strategyId;

    @Argument( index = 1, name = "peers", required = true, multiValued = true,
            description = "List of peers which are will participate on distribution." )
    private List<String> peers;


    public DistributeCommand( final StrategyManager strategyManager, final PeerManager peerManager,
                              final QuotaManager quotaManager )
    {
        this.strategyManager = strategyManager;
        this.peerManager = peerManager;
        this.quotaManager = quotaManager;
    }


    public void setStrategyId( final String strategyId )
    {
        this.strategyId = strategyId;
    }


    public void setPeers( final List<String> peers )
    {
        this.peers = peers;
    }


    @Override
    protected Object doExecute() throws Exception
    {

        try
        {
            ContainerPlacementStrategy strategy = strategyManager.findStrategyById( strategyId );
            PeerGroupResources groupResources = peerManager.getPeerGroupResources();


            final Map<ContainerSize, ContainerQuota> quotas = quotaManager.getDefaultQuotas();
            Topology topology = strategy.distribute( "Test", groupResources, quotas );
            System.out.println( topology.getEnvironmentName() );
            for ( String peer : topology.getNodeGroupPlacement().keySet() )
            {
                for ( Node ng : topology.getNodeGroupPlacement().get( peer ) )
                {
                    System.out.println(
                            String.format( "%s\t%s\t\t%s\t\t%s\t%s", ng.getPeerId(), ng.getHostId(), ng.getName(),
                                    ng.getTemplateName(), ng.getType() ) );
                }
            }

            LOGGER.debug( JsonUtil.toJson( topology ) );
        }
        catch ( StrategyNotFoundException e )
        {
            System.out.println( "Container placement strategy not found by name: " + strategyId );
        }
        catch ( PeerException e )
        {
            System.out.println( "Could not retrieve peer limits. Error message: " + e.getMessage() );
        }

        return null;
    }
}
