package org.safehaus.subutai.plugin.shark.impl.handler;


import com.google.common.collect.Sets;
import org.safehaus.subutai.common.command.AgentResult;
import org.safehaus.subutai.common.command.Command;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.plugin.shark.api.SharkClusterConfig;
import org.safehaus.subutai.plugin.shark.impl.Commands;
import org.safehaus.subutai.plugin.shark.impl.SharkImpl;


public class DestroyNodeOperationHandler extends AbstractOperationHandler<SharkImpl>
{
    private final String lxcHostname;


    public DestroyNodeOperationHandler( SharkImpl manager, String clusterName, String lxcHostname )
    {
        super( manager, clusterName );
        this.lxcHostname = lxcHostname;
        this.productOperation = manager.getTracker().createProductOperation(
                SharkClusterConfig.PRODUCT_KEY, String.format( "Destroying %s in %s", lxcHostname, clusterName ) );
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

        Agent agent = manager.getAgentManager().getAgentByHostname( lxcHostname );
        if ( agent == null )
        {
            productOperation.addLogFailed(
                    String.format( "Agent with hostname %s is not connected\nOperation aborted", lxcHostname ) );
            return;
        }

        if ( !config.getNodes().contains( agent ) )
        {
            productOperation.addLogFailed(
                    String.format( "Agent with hostname %s does not belong to cluster %s", lxcHostname, clusterName ) );
            return;
        }

        if ( config.getNodes().size() == 1 )
        {
            productOperation.addLogFailed(
                    "This is the last node in the cluster. Please, destroy cluster instead\nOperation aborted" );
            return;
        }
        productOperation.addLog( "Uninstalling Shark..." );
        Command uninstallCommand = Commands.getUninstallCommand( Sets.newHashSet( agent ) );
        manager.getCommandRunner().runCommand( uninstallCommand );

        if ( uninstallCommand.hasCompleted() )
        {
            AgentResult result = uninstallCommand.getResults().get( agent.getUuid() );
            if ( result.getExitCode() != null && result.getExitCode() == 0 )
            {
                if ( result.getStdOut().contains( "not installed" ) )
                {
                    productOperation.addLog(
                            String.format( "Shark is not installed, so not removed on node %s", agent.getHostname() ) );
                }
                else
                {
                    productOperation.addLog( String.format( "Shark is removed from node %s", agent.getHostname() ) );
                }
            }
            else
            {
                productOperation
                        .addLog( String.format( "Error %s on node %s", result.getStdErr(), agent.getHostname() ) );
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
                productOperation.addLogFailed( "Error while updating cluster info in DB. Check logs.\nFailed" );
            }
        }
        else
        {
            productOperation.addLogFailed( "Uninstallation failed, command timed out" );
        }
    }


}

