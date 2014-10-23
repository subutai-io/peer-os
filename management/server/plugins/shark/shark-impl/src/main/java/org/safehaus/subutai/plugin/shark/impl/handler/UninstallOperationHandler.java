package org.safehaus.subutai.plugin.shark.impl.handler;


import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.command.api.command.AgentResult;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.plugin.shark.api.SharkClusterConfig;
import org.safehaus.subutai.plugin.shark.impl.SharkImpl;


public class UninstallOperationHandler extends AbstractOperationHandler<SharkImpl>
{

    public UninstallOperationHandler( SharkImpl manager, String clusterName )
    {
        super( manager, clusterName );
        this.trackerOperation = manager.getTracker().createTrackerOperation( SharkClusterConfig.PRODUCT_KEY,
                String.format( "Destroying cluster %s", clusterName ) );
    }


    @Override
    public void run()
    {
        SharkClusterConfig config = manager.getCluster( clusterName );
        if ( config == null )
        {
            trackerOperation.addLogFailed(
                    String.format( "Cluster with name %s does not exist\nOperation aborted", clusterName ) );
            return;
        }

        for ( Agent node : config.getNodes() )
        {
            if ( manager.getAgentManager().getAgentByHostname( node.getHostname() ) == null )
            {
                trackerOperation.addLogFailed(
                        String.format( "Node %s is not connected\nOperation aborted", node.getHostname() ) );
                return;
            }
        }

        trackerOperation.addLog( "Uninstalling Shark..." );

        Command uninstallCommand = manager.getCommands().getUninstallCommand( config.getNodes() );
        manager.getCommandRunner().runCommand( uninstallCommand );

        if ( uninstallCommand.hasCompleted() )
        {
            for ( Agent a : config.getNodes() )
            {
                AgentResult result = uninstallCommand.getResults().get( a.getUuid() );
                if ( result.getExitCode() != null && result.getExitCode() == 0 )
                {
                    trackerOperation.addLog( String.format( "Shark removed from %s", a.getHostname() ) );
                }
                else
                {
                    trackerOperation
                            .addLog( String.format( "Error on node %s: %s", a.getHostname(), result.getStdErr() ) );
                }
            }
            trackerOperation.addLog( "Updating db..." );
            try
            {
                manager.getPluginDao().deleteInfo( SharkClusterConfig.PRODUCT_KEY, config.getClusterName() );
                trackerOperation.addLogDone( "Cluster info deleted from DB\nDone" );
            }
            catch ( Exception ex )
            {
                trackerOperation.addLogFailed( "Failed to update cluster info: " + ex.getMessage() );
            }
        }
        else
        {
            trackerOperation.addLogFailed( "Uninstallation failed, command timed out" );
        }
    }
}

