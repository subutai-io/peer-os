package org.safehaus.subutai.plugin.zookeeper.impl.handler;


import java.util.UUID;

import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;
import org.safehaus.subutai.plugin.zookeeper.impl.ZookeeperImpl;


/**
 * Handles check node status operation
 */
public class CheckNodeOperationHandler extends AbstractOperationHandler<ZookeeperImpl>
{
    private final String lxcHostname;


    public CheckNodeOperationHandler( ZookeeperImpl manager, String clusterName, String lxcHostname )
    {
        super( manager, clusterName );
        this.lxcHostname = lxcHostname;
        trackerOperation = manager.getTracker().createTrackerOperation( ZookeeperClusterConfig.PRODUCT_KEY,
                String.format( "Checking node %s in %s", lxcHostname, clusterName ) );
    }


    @Override
    public UUID getTrackerId()
    {
        return trackerOperation.getId();
    }


    @Override
    public void run()
    {
        ZookeeperClusterConfig config = manager.getCluster( clusterName );
        if ( config == null )
        {
            trackerOperation.addLogFailed(
                    String.format( "Cluster with name %s does not exist\nOperation aborted", clusterName ) );
            return;
        }

        final Agent node = manager.getAgentManager().getAgentByHostname( lxcHostname );
        if ( node == null )
        {
            trackerOperation.addLogFailed(
                    String.format( "Agent with hostname %s is not connected\nOperation aborted", lxcHostname ) );
            return;
        }
        if ( !config.getNodes().contains( node ) )
        {
            trackerOperation.addLogFailed(
                    String.format( "Agent with hostname %s does not belong to cluster %s", lxcHostname, clusterName ) );
            return;
        }

        Command checkCommand = manager.getCommands().getStatusCommand( node );
        manager.getCommandRunner().runCommand( checkCommand );

        if ( checkCommand.hasCompleted() )
        {
            trackerOperation
                    .addLogDone( String.format( "%s", checkCommand.getResults().get( node.getUuid() ).getStdOut() ) );
        }
        else
        {
            trackerOperation.addLogFailed( String.format( "Faied to check status, %s", checkCommand.getAllErrors() ) );
        }
    }
}
