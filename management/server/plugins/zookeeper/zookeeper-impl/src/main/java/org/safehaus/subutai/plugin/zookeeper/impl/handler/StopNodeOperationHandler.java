package org.safehaus.subutai.plugin.zookeeper.impl.handler;


import java.util.UUID;

import org.safehaus.subutai.common.enums.NodeState;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.command.api.command.AgentResult;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;
import org.safehaus.subutai.plugin.zookeeper.impl.ZookeeperImpl;


/**
 * Handles stop node operation
 */
public class StopNodeOperationHandler extends AbstractOperationHandler<ZookeeperImpl>
{
    private final String lxcHostname;


    public StopNodeOperationHandler( ZookeeperImpl manager, String clusterName, String lxcHostname )
    {
        super( manager, clusterName );
        this.lxcHostname = lxcHostname;
        productOperation = manager.getTracker().createProductOperation( ZookeeperClusterConfig.PRODUCT_KEY,
                String.format( "Stopping node %s in %s", lxcHostname, clusterName ) );
    }


    @Override
    public UUID getTrackerId()
    {
        return productOperation.getId();
    }


    @Override
    public void run()
    {
        ZookeeperClusterConfig config = manager.getCluster( clusterName );
        if ( config == null )
        {
            productOperation.addLogFailed(
                    String.format( "Cluster with name %s does not exist\nOperation aborted", clusterName ) );
            return;
        }

        final Agent node = manager.getAgentManager().getAgentByHostname( lxcHostname );
        if ( node == null )
        {
            productOperation.addLogFailed(
                    String.format( "Agent with hostname %s is not connected\nOperation aborted", lxcHostname ) );
            return;
        }
        if ( !config.getNodes().contains( node ) )
        {
            productOperation.addLogFailed(
                    String.format( "Agent with hostname %s does not belong to cluster %s", lxcHostname, clusterName ) );
            return;
        }
        productOperation.addLog( "Stopping node..." );

        Command stopCommand = manager.getCommands().getStopCommand( node );
        manager.getCommandRunner().runCommand( stopCommand );
        NodeState state = NodeState.UNKNOWN;
        if ( stopCommand.hasCompleted() )
        {
            AgentResult result = stopCommand.getResults().get( node.getUuid() );
            if ( result.getStdOut().contains( "STOPPED" ) )
            {
                state = NodeState.STOPPED;
            }
        }

        if ( NodeState.STOPPED.equals( state ) )
        {
            productOperation.addLogDone( String.format( "Node on %s stopped", lxcHostname ) );
        }
        else
        {
            productOperation.addLogFailed(
                    String.format( "Failed to stop node %s, %s", lxcHostname, stopCommand.getAllErrors() ) );
        }
    }
}
