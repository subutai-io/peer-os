package org.safehaus.subutai.plugin.spark.impl.handler;


import com.google.common.collect.Sets;
import org.safehaus.subutai.api.commandrunner.AgentResult;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.commandrunner.CommandCallback;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.Response;
import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;
import org.safehaus.subutai.plugin.spark.impl.Commands;
import org.safehaus.subutai.plugin.spark.impl.SparkImpl;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;


public class AddSlaveNodeOperationHandler extends AbstractOperationHandler<SparkImpl>
{
    private final String lxcHostname;


    public AddSlaveNodeOperationHandler( SparkImpl manager, String clusterName, String lxcHostname )
    {
        super( manager, clusterName );
        this.lxcHostname = lxcHostname;
        productOperation = manager.getTracker().createProductOperation( SparkClusterConfig.PRODUCT_KEY,
            String.format( "Adding node %s to %s", lxcHostname, clusterName ) );
    }


    @Override
    public UUID getTrackerId()
    {
        return productOperation.getId();
    }


    @Override
    public void run()
    {
        SparkClusterConfig config = manager.getCluster( clusterName );
        if ( config == null )
        {
            productOperation.addLogFailed( String.format( "Cluster with name %s does not exist\nOperation aborted", clusterName ) );
            return;
        }

        if ( manager.getAgentManager().getAgentByHostname( config.getMasterNode().getHostname() ) == null )
        {
            productOperation.addLogFailed( String
                    .format( "Master node %s is not connected\nOperation aborted", config.getMasterNode().getHostname() ) );
            return;
        }

        Agent agent = manager.getAgentManager().getAgentByHostname( lxcHostname );
        if ( agent == null )
        {
            productOperation.addLogFailed( String.format( "New node %s is not connected\nOperation aborted", lxcHostname ) );
            return;
        }

        //check if node is in the cluster
        if ( config.getSlaveNodes().contains( agent ) )
        {
            productOperation.addLogFailed(
                    String.format( "Node %s already belongs to this cluster\nOperation aborted", agent.getHostname() ) );
            return;
        }

        productOperation.addLog( "Checking prerequisites..." );

        boolean install = !agent.equals( config.getMasterNode() );

        //check installed ksks packages
        Command checkInstalledCommand = Commands.getCheckInstalledCommand( Sets.newHashSet( agent ) );
        manager.getCommandRunner().runCommand( checkInstalledCommand );

        if ( !checkInstalledCommand.hasCompleted() )
        {
            productOperation.addLogFailed( "Failed to check presence of installed ksks packages\nOperation aborted" );
            return;
        }

        AgentResult result = checkInstalledCommand.getResults().get( agent.getUuid() );

        if ( result.getStdOut().contains( "ksks-spark" ) && install )
        {
            productOperation.addLogFailed( String.format( "Node %s already has Spark installed\nOperation aborted", lxcHostname ) );
            return;
        }
        else if ( !result.getStdOut().contains( "ksks-hadoop" ) )
        {
            productOperation.addLogFailed( String.format( "Node %s has no Hadoop installation\nOperation aborted", lxcHostname ) );
            return;
        }

        config.getSlaveNodes().add( agent );
        productOperation.addLog( "Updating db..." );
        //save to db
        if ( manager.getDbManager().saveInfo( SparkClusterConfig.PRODUCT_KEY, config.getClusterName(), config ) )
        {
            productOperation.addLog( "Cluster info updated in DB" );
            //install spark

            if ( install )
            {
                productOperation.addLog( "Installing Spark..." );
                Command installCommand = Commands.getInstallCommand( Sets.newHashSet( agent ) );
                manager.getCommandRunner().runCommand( installCommand );

                if ( installCommand.hasSucceeded() )
                {
                    productOperation.addLog( "Installation succeeded" );
                }
                else
                {
                    productOperation.addLogFailed( String.format( "Installation failed, %s", installCommand.getAllErrors() ) );
                    return;
                }
            }

            productOperation.addLog( "Setting master IP on slave..." );
            Command setMasterIPCommand = Commands
                .getSetMasterIPCommand( config.getMasterNode(), Sets.newHashSet( agent ) );
            manager.getCommandRunner().runCommand( setMasterIPCommand );

            if ( setMasterIPCommand.hasSucceeded() )
            {
                productOperation.addLog( "Master IP successfully set\nRegistering slave with master..." );

                Command addSlaveCommand = Commands.getAddSlaveCommand( agent, config.getMasterNode() );
                manager.getCommandRunner().runCommand( addSlaveCommand );

                if ( addSlaveCommand.hasSucceeded() )
                {
                    productOperation.addLog( "Registration succeeded\nRestarting master..." );

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
                        productOperation.addLog( "Master restarted successfully\nStarting Spark on new node..." );

                        Command startSlaveCommand = Commands.getStartSlaveCommand( agent );
                        ok.set( false );
                        manager.getCommandRunner().runCommand( startSlaveCommand, new CommandCallback()
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
                            productOperation.addLogDone( "Spark started successfully\nDone" );
                        }
                        else
                        {
                            productOperation.addLogFailed(
                                    String.format( "Failed to start Spark, %s", startSlaveCommand.getAllErrors() ) );
                        }

                    }
                    else
                    {
                        productOperation.addLogFailed(
                                String.format( "Master restart failed, %s", restartMasterCommand.getAllErrors() ) );
                    }

                }
                else
                {
                    productOperation.addLogFailed( String.format( "Registration failed, %s", addSlaveCommand.getAllErrors() ) );
                }
            }
            else
            {
                productOperation.addLogFailed( String.format( "Failed to set master IP, %s", setMasterIPCommand.getAllErrors() ) );
            }

        }
        else
        {
            productOperation.addLogFailed( "Could not update cluster info in DB! Please see logs\nOperation aborted" );
        }
    }
}
