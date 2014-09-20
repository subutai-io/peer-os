package org.safehaus.subutai.plugin.accumulo.impl.handler;


import java.util.UUID;

import org.safehaus.subutai.common.exception.ClusterConfigurationException;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.plugin.accumulo.api.AccumuloClusterConfig;
import org.safehaus.subutai.plugin.accumulo.api.NodeType;
import org.safehaus.subutai.plugin.accumulo.impl.AccumuloImpl;
import org.safehaus.subutai.plugin.accumulo.impl.ClusterConfiguration;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


/**
 * Handles destroy node operation.
 */
public class DestroyNodeOperationHandler extends AbstractOperationHandler<AccumuloImpl>
{
    private final String lxcHostname;
    private final NodeType nodeType;


    public DestroyNodeOperationHandler( AccumuloImpl manager, String clusterName, String lxcHostname,
                                        NodeType nodeType )
    {
        super( manager, clusterName );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( lxcHostname ), "Lxc hostname is null or empty" );
        Preconditions.checkNotNull( nodeType, "Node type is null" );
        this.lxcHostname = lxcHostname;
        this.nodeType = nodeType;
        productOperation = manager.getTracker().createProductOperation( AccumuloClusterConfig.PRODUCT_KEY,
                String.format( "Destroying %s on %s", nodeType, lxcHostname ) );
    }


    @Override
    public UUID getTrackerId()
    {
        return productOperation.getId();
    }


    @Override
    public void run()
    {
        //check of node type is allowed for removal
        if ( !( nodeType == NodeType.TRACER || nodeType.isSlave() ) )
        {
            productOperation.addLogFailed( "Only tracer or slave node can be destroyed" );
            return;
        }

        //check if cluster exists
        final AccumuloClusterConfig accumuloClusterConfig = manager.getCluster( clusterName );
        if ( accumuloClusterConfig == null )
        {
            productOperation.addLogFailed( String.format( "Cluster with name %s does not exist", clusterName ) );
            return;
        }

        //check if node's agent is connected
        Agent agent = manager.getAgentManager().getAgentByHostname( lxcHostname );
        if ( agent == null )
        {
            productOperation.addLogFailed( String.format( "Agent with hostname %s is not connected", lxcHostname ) );
            return;
        }

        //check if node belongs to cluster
        if ( !accumuloClusterConfig.getAllNodes().contains( agent ) )
        {
            productOperation.addLogFailed(
                    String.format( "Agent with hostname %s does not belong to cluster %s", lxcHostname, clusterName ) );
            return;
        }
        //check if master node is connected
        if ( manager.getAgentManager().getAgentByHostname( accumuloClusterConfig.getMasterNode().getHostname() )
                == null )
        {
            productOperation.addLogFailed( String.format( "Master node %s is not connected",
                    accumuloClusterConfig.getMasterNode().getHostname() ) );
            return;
        }

        //check if node is the last node of its role
        if ( nodeType == NodeType.TRACER )
        {
            if ( accumuloClusterConfig.getTracers().size() == 1 )
            {
                productOperation.addLogFailed( "This is the last tracer in the cluster, destroy cluster instead" );
                return;
            }
        }
        else
        {
            if ( accumuloClusterConfig.getSlaves().size() == 1 )
            {
                productOperation.addLogFailed( "This is the last slave in the cluster, destroy cluster instead" );
                return;
            }
        }

        //remove node
        try
        {
            new ClusterConfiguration( productOperation, manager ).removeNode( accumuloClusterConfig, agent, nodeType );
            productOperation.addLogDone( "Node removed successfully" );
        }
        catch ( ClusterConfigurationException e )
        {
            productOperation.addLogFailed( String.format( "Node removal failed, %s", e.getMessage() ) );
        }
    }
}
