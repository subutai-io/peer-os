package org.safehaus.subutai.plugin.spark.impl.handler;


import com.google.common.collect.Sets;
import org.safehaus.subutai.core.command.api.AgentResult;
import org.safehaus.subutai.core.command.api.Command;
import org.safehaus.subutai.core.command.api.CommandCallback;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.Response;
import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;
import org.safehaus.subutai.plugin.spark.impl.Commands;
import org.safehaus.subutai.plugin.spark.impl.SparkImpl;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;


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
    public UUID getTrackerId()
    {
        return productOperation.getId();
    }


    @Override
    public void run()
    {
        final SparkClusterConfig config = manager.getCluster( clusterName );
        if ( config == null )
        {
            productOperation.addLogFailed( String.format( "Cluster with name %s does not exist\nOperation aborted", clusterName ) );
            return;
        }

        Agent agent = manager.getAgentManager().getAgentByHostname( lxcHostname );
        if ( agent == null )
        {
            productOperation.addLogFailed(
                    String.format( "Agent with hostname %s is not connected\nOperation aborted", lxcHostname ) );
            return;
        }

        if ( config.getSlaveNodes().size() == 1 )
        {
            productOperation.addLogFailed(
                    "This is the last slave node in the cluster. Please, destroy cluster instead\nOperation aborted" );
            return;
        }

        //check if node is in the cluster
        if ( !config.getSlaveNodes().contains( agent ) )
        {
            productOperation.addLogFailed(
                    String.format( "Node %s does not belong to this cluster\nOperation aborted", agent.getHostname() ) );
            return;
        }

        productOperation.addLog( "Unregistering slave from master..." );

        if ( manager.getAgentManager().getAgentByHostname( config.getMasterNode().getHostname() ) != null )
        {

            Command clearSlavesCommand = Commands.getClearSlaveCommand( agent, config.getMasterNode() );
            manager.getCommandRunner().runCommand( clearSlavesCommand );

            if ( clearSlavesCommand.hasSucceeded() )
            {
                productOperation.addLog( "Successfully unregistered slave from master\nRestarting master..." );

                Command restartMasterCommand = Commands.getRestartMasterCommand( config.getMasterNode() );
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
                    productOperation.addLog( "Master restarted successfully" );
                }
                else
                {
                    productOperation.addLog( String
                            .format( "Master restart failed, %s, skipping...", restartMasterCommand.getAllErrors() ) );
                }
            }
            else
            {
                productOperation.addLog( String.format( "Failed to unregister slave from master: %s, skipping...",
                        clearSlavesCommand.getAllErrors() ) );
            }
        }
        else
        {
            productOperation.addLog( "Failed to unregister slave from master: Master is not connected, skipping..." );
        }

        boolean uninstall = !agent.equals( config.getMasterNode() );

        if ( uninstall )
        {
            productOperation.addLog( "Uninstalling Spark..." );

            Command uninstallCommand = Commands.getUninstallCommand( Sets.newHashSet( agent ) );
            manager.getCommandRunner().runCommand( uninstallCommand );

            if ( uninstallCommand.hasCompleted() )
            {
                AgentResult result = uninstallCommand.getResults().get( agent.getUuid() );
                if ( result.getExitCode() != null && result.getExitCode() == 0 )
                {
                    if ( result.getStdOut().contains( "Package ksks-spark is not installed, so not removed" ) )
                    {
                        productOperation.addLog( String.format( "Spark is not installed, so not removed on node %s",
                                agent.getHostname() ) );
                    }
                    else
                    {
                        productOperation.addLog( String.format( "Spark is removed from node %s",
                                agent.getHostname() ) );
                    }
                }
                else
                {
                    productOperation.addLog( String.format( "Error %s on node %s", result.getStdErr(),
                            agent.getHostname() ) );
                }

            }
            else
            {
                productOperation.addLogFailed( String.format( "Uninstallation failed, %s", uninstallCommand.getAllErrors() ) );
                return;
            }
        }
        else
        {
            productOperation.addLog( "Stopping slave..." );

            Command stopSlaveCommand = Commands.getStopSlaveCommand( agent );
            manager.getCommandRunner().runCommand( stopSlaveCommand );

            if ( stopSlaveCommand.hasSucceeded() )
            {
                productOperation.addLog( "Slave stopped successfully" );
            }
            else
            {
                productOperation.addLog( String.format( "Failed to stop slave, %s, skipping...", stopSlaveCommand.getAllErrors() ) );
            }
        }

        config.getSlaveNodes().remove( agent );
        productOperation.addLog( "Updating db..." );

        if ( manager.getDbManager().saveInfo( SparkClusterConfig.PRODUCT_KEY, config.getClusterName(), config ) )
        {
            productOperation.addLogDone( "Cluster info updated in DB\nDone" );
        }
        else
        {
            productOperation.addLogFailed( "Error while updating cluster info in DB. Check logs.\nFailed" );
        }
    }
}
