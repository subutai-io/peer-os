package org.safehaus.subutai.plugin.pig.impl.handler;


import com.google.common.collect.Sets;
import org.safehaus.subutai.api.commandrunner.AgentResult;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.plugin.pig.api.Config;
import org.safehaus.subutai.plugin.pig.impl.PigImpl;


public class DestroyNodeOperationHandler extends AbstractOperationHandler<PigImpl>
{
    private final String lxcHostname;


    public DestroyNodeOperationHandler( PigImpl manager, String clusterName, String lxcHostname )
    {
        super( manager, clusterName );
        this.lxcHostname = lxcHostname;
        productOperation = manager.getTracker().createProductOperation( Config.PRODUCT_KEY,
            String.format( "Destroying %s in %s", lxcHostname, clusterName ) );
    }


    @Override
    public void run()
    {
        Config config = manager.getCluster( clusterName );
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

        productOperation.addLog( "Uninstalling Pig..." );
        Command uninstallCommand = manager.getCommands().getUninstallCommand( Sets.newHashSet( agent ) );
        manager.getCommandRunner().runCommand( uninstallCommand );

        if ( uninstallCommand.hasCompleted() )
        {
            AgentResult result = uninstallCommand.getResults().get( agent.getUuid() );
            if ( result.getExitCode() != null && result.getExitCode() == 0 )
            {
                if ( result.getStdOut().contains(
                    String.format( "Package %s is not installed, so not removed", Config.PRODUCT_PACKAGE ) ) )
                {
                    productOperation.addLog(
                        String.format( "Pig is not installed, so not removed on node %s", agent.getHostname() ) );
                }
                else
                {
                    productOperation.addLog( String.format( "Pig is removed from node %s", agent.getHostname() ) );
                }
            }
            else
            {
                productOperation
                    .addLog( String.format( "Error %s on node %s", result.getStdErr(), agent.getHostname() ) );
            }

            config.getNodes().remove( agent );
            productOperation.addLog( "Updating db..." );

            if ( config.getNodes().isEmpty() ?
                manager.getDbManager().deleteInfo( Config.PRODUCT_KEY, config.getClusterName() ) :
                manager.getDbManager().saveInfo( Config.PRODUCT_KEY, config.getClusterName(), config ) )
            {
                productOperation.addLogDone( "Cluster info update in DB\nDone" );
            }
            else
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
