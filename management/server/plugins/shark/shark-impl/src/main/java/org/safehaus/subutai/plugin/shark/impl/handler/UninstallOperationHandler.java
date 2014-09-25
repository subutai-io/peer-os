package org.safehaus.subutai.plugin.shark.impl.handler;


import org.safehaus.subutai.common.command.AgentResult;
import org.safehaus.subutai.common.command.Command;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.plugin.shark.api.SharkClusterConfig;
import org.safehaus.subutai.plugin.shark.impl.Commands;
import org.safehaus.subutai.plugin.shark.impl.SharkImpl;


public class UninstallOperationHandler extends AbstractOperationHandler<SharkImpl>
{

    public UninstallOperationHandler( SharkImpl manager, String clusterName )
    {
        super( manager, clusterName );
        this.productOperation = manager.getTracker().createProductOperation(
                SharkClusterConfig.PRODUCT_KEY, String.format( "Destroying cluster %s", clusterName ) );
    }


    @Override
    public void run()
    {
        SharkClusterConfig config = manager.getCluster( clusterName );
        if ( config == null )
        {
            productOperation.addLogFailed(
                    String.format( "Cluster with name %s does not exist\nOperation aborted", clusterName ) );
            return;
        }

        for ( Agent node : config.getNodes() )
        {
            if ( manager.getAgentManager().getAgentByHostname( node.getHostname() ) == null )
            {
                productOperation.addLogFailed(
                        String.format( "Node %s is not connected\nOperation aborted", node.getHostname() ) );
                return;
            }
        }

        productOperation.addLog( "Uninstalling Shark..." );

        Command uninstallCommand = Commands.getUninstallCommand( config.getNodes() );
        manager.getCommandRunner().runCommand( uninstallCommand );

        if ( uninstallCommand.hasCompleted() )
        {
            for ( Agent a : config.getNodes() )
            {
                AgentResult result = uninstallCommand.getResults().get( a.getUuid() );
                if ( result.getExitCode() != null && result.getExitCode() == 0 )
                {
                    productOperation.addLog( String.format( "Shark removed from %s", a.getHostname() ) );
                }
                else
                {
                    productOperation.addLog(
                            String.format( "Error on node %s: %s", a.getHostname(), result.getStdErr() ) );
                }
            }
            productOperation.addLog( "Updating db..." );
            try
            {
                manager.getPluginDao().deleteInfo( SharkClusterConfig.PRODUCT_KEY, config.getClusterName() );
                productOperation.addLogDone( "Cluster info deleted from DB\nDone" );
            }
            catch ( Exception ex )
            {
                productOperation.addLogFailed( "Failed to update cluster info: " + ex.getMessage() );
            }
        }
        else
        {
            productOperation.addLogFailed( "Uninstallation failed, command timed out" );
        }
    }


}

