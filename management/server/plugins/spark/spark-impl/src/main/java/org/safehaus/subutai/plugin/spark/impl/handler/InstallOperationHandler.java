package org.safehaus.subutai.plugin.spark.impl.handler;


import com.google.common.base.Strings;
import org.safehaus.subutai.api.commandrunner.AgentResult;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.commandrunner.CommandCallback;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.Response;
import org.safehaus.subutai.common.util.CollectionUtil;
import org.safehaus.subutai.common.util.StringUtil;
import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;
import org.safehaus.subutai.plugin.spark.impl.Commands;
import org.safehaus.subutai.plugin.spark.impl.SparkImpl;

import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class InstallOperationHandler extends AbstractOperationHandler<SparkImpl>
{
    private final SparkClusterConfig config;


    public InstallOperationHandler( SparkImpl manager, SparkClusterConfig config )
    {
        super( manager, config.getClusterName() );
        this.config = config;
        productOperation = manager.getTracker().createProductOperation( SparkClusterConfig.PRODUCT_KEY,
            String.format( "Installing %s", SparkClusterConfig.PRODUCT_KEY ) );
    }


    @Override
    public UUID getTrackerId()
    {
        return productOperation.getId();
    }


    @Override
    public void run()
    {
        if ( Strings.isNullOrEmpty( config.getClusterName() ) || CollectionUtil
            .isCollectionEmpty( config.getSlaveNodes() ) || config.getMasterNode() == null )
        {
            productOperation.addLogFailed( "Malformed configuration\nInstallation aborted" );
            return;
        }

        if ( manager.getCluster( config.getClusterName() ) != null )
        {
            productOperation.addLogFailed( String
                    .format( "Cluster with name '%s' already exists\nInstallation aborted", config.getClusterName() ) );
            return;
        }

        if ( manager.getAgentManager().getAgentByHostname( config.getMasterNode().getHostname() ) == null )
        {
            productOperation.addLogFailed( "Master node is not connected\nInstallation aborted" );
            return;
        }

        //check if node agent is connected
        for ( Iterator<Agent> it = config.getSlaveNodes().iterator(); it.hasNext(); )
        {
            Agent node = it.next();
            if ( manager.getAgentManager().getAgentByHostname( node.getHostname() ) == null )
            {
                productOperation.addLog( String
                        .format( "Node %s is not connected. Omitting this node from installation", node.getHostname() ) );
                it.remove();
            }
        }

        if ( config.getSlaveNodes().isEmpty() )
        {
            productOperation.addLogFailed( "No nodes eligible for installation\nInstallation aborted" );
            return;
        }

        productOperation.addLog( "Checking prerequisites..." );

        //check installed ksks packages
        Set<Agent> allNodes = config.getAllNodes();
        Command checkInstalledCommand = Commands.getCheckInstalledCommand( allNodes );
        manager.getCommandRunner().runCommand( checkInstalledCommand );

        if ( !checkInstalledCommand.hasCompleted() )
        {
            productOperation.addLogFailed( "Failed to check presence of installed ksks packages\nInstallation aborted" );
            return;
        }
        for ( Iterator<Agent> it = allNodes.iterator(); it.hasNext(); )
        {
            Agent node = it.next();

            AgentResult result = checkInstalledCommand.getResults().get( node.getUuid() );
            if ( result.getStdOut().contains( "ksks-spark" ) )
            {
                productOperation.addLog( String.format( "Node %s already has Spark installed. Omitting this node from installation",
                        node.getHostname() ) );
                config.getSlaveNodes().remove( node );
                it.remove();
            }
            else if ( !result.getStdOut().contains( "ksks-hadoop" ) )
            {
                productOperation.addLog( String.format( "Node %s has no Hadoop installation. Omitting this node from installation",
                        node.getHostname() ) );
                config.getSlaveNodes().remove( node );
                it.remove();
            }
        }

        if ( config.getSlaveNodes().isEmpty() )
        {
            productOperation.addLogFailed( "No nodes eligible for installation\nInstallation aborted" );
            return;
        }
        if ( !allNodes.contains( config.getMasterNode() ) )
        {
            productOperation.addLogFailed( "Master node was omitted\nInstallation aborted" );
            return;
        }

        productOperation.addLog( "Updating db..." );
        //save to db
        if ( manager.getDbManager().saveInfo( SparkClusterConfig.PRODUCT_KEY, config.getClusterName(), config ) )
        {
            productOperation.addLog( "Cluster info saved to DB\nInstalling Spark..." );
            //install spark
            Command installCommand = Commands.getInstallCommand( config.getAllNodes() );
            manager.getCommandRunner().runCommand( installCommand );

            if ( installCommand.hasSucceeded() )
            {
                productOperation.addLog( "Installation succeeded\nSetting master IP..." );

                Command setMasterIPCommand = Commands
                    .getSetMasterIPCommand( config.getMasterNode(), config.getAllNodes() );
                manager.getCommandRunner().runCommand( setMasterIPCommand );

                if ( setMasterIPCommand.hasSucceeded() )
                {
                    productOperation.addLog( "Setting master IP succeeded\nRegistering slaves..." );

                    Command addSlavesCommand = Commands
                        .getAddSlavesCommand( config.getSlaveNodes(), config.getMasterNode() );
                    manager.getCommandRunner().runCommand( addSlavesCommand );

                    if ( addSlavesCommand.hasSucceeded() )
                    {
                        productOperation.addLog( "Slaves successfully registered\nStarting cluster..." );

                        Command startNodesCommand = Commands.getStartAllCommand( config.getMasterNode() );
                        final AtomicInteger okCount = new AtomicInteger( 0 );
                        manager.getCommandRunner().runCommand( startNodesCommand, new CommandCallback()
                        {

                            @Override
                            public void onResponse( Response response, AgentResult agentResult, Command command )
                            {
                                okCount
                                    .set( StringUtil.countNumberOfOccurences( agentResult.getStdOut(), "starting" ) );

                                if ( okCount.get() >= config.getAllNodes().size() )
                                {
                                    stop();
                                }
                            }

                        } );

                        if ( okCount.get() >= config.getAllNodes().size() )
                        {
                            productOperation.addLogDone( "cluster started successfully\nDone" );
                        }
                        else
                        {
                            productOperation.addLogFailed(
                                    String.format( "Failed to start cluster, %s", startNodesCommand.getAllErrors() ) );
                        }

                    }
                    else
                    {
                        productOperation.addLogFailed( String
                                .format( "Failed to register slaves with master, %s", addSlavesCommand.getAllErrors() ) );
                    }
                }
                else
                {
                    productOperation.addLogFailed(
                            String.format( "Setting master IP failed, %s", setMasterIPCommand.getAllErrors() ) );
                }

            }
            else
            {
                productOperation.addLogFailed( String.format( "Installation failed, %s", installCommand.getAllErrors() ) );
            }
        }
        else
        {
            productOperation.addLogFailed( "Could not save cluster info to DB! Please see logs\nInstallation aborted" );
        }
    }
}
