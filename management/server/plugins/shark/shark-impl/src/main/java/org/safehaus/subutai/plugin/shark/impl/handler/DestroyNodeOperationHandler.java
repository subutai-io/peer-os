package org.safehaus.subutai.plugin.shark.impl.handler;


import java.util.Arrays;
import java.util.HashSet;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.plugin.shark.api.SharkClusterConfig;
import org.safehaus.subutai.plugin.shark.impl.Commands;
import org.safehaus.subutai.plugin.shark.impl.SharkImpl;


public class DestroyNodeOperationHandler extends AbstractOperationHandler<SharkImpl>
{
    private final String hostname;


    public DestroyNodeOperationHandler( SharkImpl manager, String clusterName, String hostname )
    {
        super( manager, clusterName );
        this.hostname = hostname;
        this.productOperation = manager.getTracker().createProductOperation(
                SharkClusterConfig.PRODUCT_KEY, String.format( "Destroying %s in %s", hostname, clusterName ) );
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

        Agent agent = manager.getAgentManager().getAgentByHostname( hostname );
        if ( agent == null )
        {
            productOperation.addLogFailed(
                    String.format( "Agent with hostname %s is not connected\nOperation aborted", hostname ) );
            return;
        }

        if ( !config.getNodes().contains( agent ) )
        {
            productOperation.addLogFailed(
                    String.format( "Agent with hostname %s does not belong to cluster %s", hostname, clusterName ) );
            return;
        }

        if ( config.getNodes().size() == 1 )
        {
            productOperation.addLogFailed(
                    "This is the last node in the cluster. Please, destroy cluster instead\nOperation aborted" );
            return;
        }
        productOperation.addLog( "Uninstalling Shark..." );
        Command uninstallCommand = Commands.getUninstallCommand( new HashSet<>( Arrays.asList( agent ) ) );
        manager.getCommandRunner().runCommand( uninstallCommand );

        if ( uninstallCommand.hasCompleted() )
        {
            if ( uninstallCommand.hasSucceeded() )
            {
                productOperation.addLog( String.format( "Shark is removed from node %s", agent.getHostname() ) );
            }
            else
            {
                productOperation.addLog( "Failed to remove Shark: " + uninstallCommand.getAllErrors() );
            }

            config.getNodes().remove( agent );

            productOperation.addLog( "Updating db..." );
            try
            {
                manager.getPluginDao().saveInfo( SharkClusterConfig.PRODUCT_KEY, config.getClusterName(), config );
                productOperation.addLogDone( "Cluster info update in DB\nDone" );
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

