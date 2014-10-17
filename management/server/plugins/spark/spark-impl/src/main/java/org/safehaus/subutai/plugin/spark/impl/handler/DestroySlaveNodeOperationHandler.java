package org.safehaus.subutai.plugin.spark.impl.handler;


import java.util.concurrent.atomic.AtomicBoolean;

import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.Response;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.core.command.api.command.AgentResult;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.core.command.api.command.CommandCallback;
import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;
import org.safehaus.subutai.plugin.spark.impl.SparkImpl;

import com.google.common.collect.Sets;


public class DestroySlaveNodeOperationHandler extends AbstractOperationHandler<SparkImpl>
{

    private final String lxcHostname;


    public DestroySlaveNodeOperationHandler( SparkImpl manager, String clusterName, String lxcHostname )
    {
        super( manager, clusterName );
        this.lxcHostname = lxcHostname;
        productOperation = manager.getTracker().createProductOperation( SparkClusterConfig.PRODUCT_KEY,
                String.format( "Destroying %s in %s", lxcHostname, clusterName ) );
    }


    @Override
    public void run()
    {
        ProductOperation po = productOperation;
        final SparkClusterConfig config = manager.getCluster( clusterName );
        if ( config == null )
        {
            po.addLogFailed( String.format( "Cluster with name %s does not exist\nOperation aborted", clusterName ) );
            return;
        }

        Agent agent = manager.getAgentManager().getAgentByHostname( lxcHostname );
        if ( agent == null )
        {
            po.addLogFailed(
                    String.format( "Agent with hostname %s is not connected\nOperation aborted", lxcHostname ) );
            return;
        }

        if ( config.getSlaveNodes().size() == 1 )
        {
            po.addLogFailed(
                    "This is the last slave node in the cluster. Please, destroy cluster instead\nOperation aborted" );
            return;
        }

        //check if node is in the cluster
        if ( !config.getSlaveNodes().contains( agent ) )
        {
            po.addLogFailed( String.format( "Node %s does not belong to this cluster\nOperation aborted",
                    agent.getHostname() ) );
            return;
        }

        po.addLog( "Unregistering slave from master..." );

        if ( manager.getAgentManager().getAgentByHostname( config.getMasterNode().getHostname() ) != null )
        {

            Command clearSlavesCommand = manager.getCommands().getClearSlaveCommand( agent, config.getMasterNode() );
            manager.getCommandRunner().runCommand( clearSlavesCommand );

            if ( clearSlavesCommand.hasSucceeded() )
            {
                po.addLog( "Successfully unregistered slave from master\nRestarting master..." );

                Command restartMasterCommand = manager.getCommands().getRestartMasterCommand( config.getMasterNode() );
                final AtomicBoolean ok = new AtomicBoolean();
                manager.getCommandRunner().runCommand( restartMasterCommand, new CommandCallback()
                {

                    @Override
                    public void onResponse( Response response, AgentResult agentResult, Command command )
                    {
                        if ( agentResult.getStdOut().contains( "starting" ) )
                        {
                            ok.set( true );
                            stop();
                        }
                    }
                } );

                if ( ok.get() )
                {
                    po.addLog( "Master restarted successfully" );
                }
                else
                {
                    po.addLog( String.format( "Master restart failed, %s, skipping...",
                            restartMasterCommand.getAllErrors() ) );
                }
            }
            else
            {
                po.addLog( String.format( "Failed to unregister slave from master: %s, skipping...",
                        clearSlavesCommand.getAllErrors() ) );
            }
        }
        else
        {
            po.addLog( "Failed to unregister slave from master: Master is not connected, skipping..." );
        }

        boolean uninstall = !agent.equals( config.getMasterNode() );

        if ( uninstall )
        {
            po.addLog( "Uninstalling Spark..." );

            Command uninstallCommand = manager.getCommands().getUninstallCommand( Sets.newHashSet( agent ) );
            manager.getCommandRunner().runCommand( uninstallCommand );

            if ( uninstallCommand.hasCompleted() )
            {
                AgentResult result = uninstallCommand.getResults().get( agent.getUuid() );
                if ( result.getExitCode() != null && result.getExitCode() == 0 )
                {
                    if ( result.getStdOut().contains( "Package subutai-spark is not installed, so not removed" ) )
                    {
                        po.addLog( String.format( "Spark is not installed, so not removed on node %s",
                                agent.getHostname() ) );
                    }
                    else
                    {
                        po.addLog( String.format( "Spark is removed from node %s", agent.getHostname() ) );
                    }
                }
                else
                {
                    po.addLog( String.format( "Error %s on node %s", result.getStdErr(), agent.getHostname() ) );
                }
            }
            else
            {
                po.addLogFailed( String.format( "Uninstallation failed, %s", uninstallCommand.getAllErrors() ) );
                return;
            }
        }
        else
        {
            po.addLog( "Stopping slave..." );

            Command stopSlaveCommand = manager.getCommands().getStopSlaveCommand( agent );
            manager.getCommandRunner().runCommand( stopSlaveCommand );

            if ( stopSlaveCommand.hasSucceeded() )
            {
                po.addLog( "Slave stopped successfully" );
            }
            else
            {
                po.addLog( String.format( "Failed to stop slave, %s, skipping...", stopSlaveCommand.getAllErrors() ) );
            }
        }

        config.getSlaveNodes().remove( agent );
        po.addLog( "Updating db..." );

        manager.getPluginDAO().saveInfo( SparkClusterConfig.PRODUCT_KEY, config.getClusterName(), config );
        po.addLogDone( "Cluster info updated in DB\nDone" );
    }
}
