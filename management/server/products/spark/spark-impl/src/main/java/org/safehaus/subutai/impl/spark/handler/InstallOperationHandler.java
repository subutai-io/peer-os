package org.safehaus.subutai.impl.spark.handler;


import com.google.common.base.Strings;
import org.safehaus.subutai.core.commandrunner.api.AgentResult;
import org.safehaus.subutai.core.commandrunner.api.Command;
import org.safehaus.subutai.core.commandrunner.api.CommandCallback;
import org.safehaus.subutai.api.spark.Config;
import org.safehaus.subutai.common.util.CollectionUtil;
import org.safehaus.subutai.common.util.StringUtil;
import org.safehaus.subutai.impl.spark.Commands;
import org.safehaus.subutai.impl.spark.SparkImpl;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.Response;

import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Created by dilshat on 5/7/14.
 */
public class InstallOperationHandler extends AbstractOperationHandler<SparkImpl>
{
    private final ProductOperation po;
    private final Config config;


    public InstallOperationHandler( SparkImpl manager, Config config )
    {
        super( manager, config.getClusterName() );
        this.config = config;
        po = manager.getTracker().createProductOperation( Config.PRODUCT_KEY,
            String.format( "Installing %s", Config.PRODUCT_KEY ) );
    }


    @Override
    public UUID getTrackerId()
    {
        return po.getId();
    }


    @Override
    public void run()
    {
        if ( Strings.isNullOrEmpty( config.getClusterName() ) || CollectionUtil
            .isCollectionEmpty( config.getSlaveNodes() ) || config.getMasterNode() == null )
        {
            po.addLogFailed( "Malformed configuration\nInstallation aborted" );
            return;
        }

        if ( manager.getCluster( config.getClusterName() ) != null )
        {
            po.addLogFailed( String
                .format( "Cluster with name '%s' already exists\nInstallation aborted", config.getClusterName() ) );
            return;
        }

        if ( manager.getAgentManager().getAgentByHostname( config.getMasterNode().getHostname() ) == null )
        {
            po.addLogFailed( "Master node is not connected\nInstallation aborted" );
            return;
        }

        //check if node agent is connected
        for ( Iterator<Agent> it = config.getSlaveNodes().iterator(); it.hasNext(); )
        {
            Agent node = it.next();
            if ( manager.getAgentManager().getAgentByHostname( node.getHostname() ) == null )
            {
                po.addLog( String
                    .format( "Node %s is not connected. Omitting this node from installation", node.getHostname() ) );
                it.remove();
            }
        }

        if ( config.getSlaveNodes().isEmpty() )
        {
            po.addLogFailed( "No nodes eligible for installation\nInstallation aborted" );
            return;
        }

        po.addLog( "Checking prerequisites..." );

        //check installed ksks packages
        Set<Agent> allNodes = config.getAllNodes();
        Command checkInstalledCommand = Commands.getCheckInstalledCommand( allNodes );
        manager.getCommandRunner().runCommand( checkInstalledCommand );

        if ( !checkInstalledCommand.hasCompleted() )
        {
            po.addLogFailed( "Failed to check presence of installed ksks packages\nInstallation aborted" );
            return;
        }
        for ( Iterator<Agent> it = allNodes.iterator(); it.hasNext(); )
        {
            Agent node = it.next();

            AgentResult result = checkInstalledCommand.getResults().get( node.getUuid() );
            if ( result.getStdOut().contains( "ksks-spark" ) )
            {
                po.addLog( String.format( "Node %s already has Spark installed. Omitting this node from installation",
                    node.getHostname() ) );
                config.getSlaveNodes().remove( node );
                it.remove();
            }
            else if ( !result.getStdOut().contains( "ksks-hadoop" ) )
            {
                po.addLog( String.format( "Node %s has no Hadoop installation. Omitting this node from installation",
                    node.getHostname() ) );
                config.getSlaveNodes().remove( node );
                it.remove();
            }
        }

        if ( config.getSlaveNodes().isEmpty() )
        {
            po.addLogFailed( "No nodes eligible for installation\nInstallation aborted" );
            return;
        }
        if ( !allNodes.contains( config.getMasterNode() ) )
        {
            po.addLogFailed( "Master node was omitted\nInstallation aborted" );
            return;
        }

        po.addLog( "Updating db..." );
        //save to db
        if ( manager.getDbManager().saveInfo( Config.PRODUCT_KEY, config.getClusterName(), config ) )
        {
            po.addLog( "Cluster info saved to DB\nInstalling Spark..." );
            //install spark
            Command installCommand = Commands.getInstallCommand( config.getAllNodes() );
            manager.getCommandRunner().runCommand( installCommand );

            if ( installCommand.hasSucceeded() )
            {
                po.addLog( "Installation succeeded\nSetting master IP..." );

                Command setMasterIPCommand = Commands
                    .getSetMasterIPCommand( config.getMasterNode(), config.getAllNodes() );
                manager.getCommandRunner().runCommand( setMasterIPCommand );

                if ( setMasterIPCommand.hasSucceeded() )
                {
                    po.addLog( "Setting master IP succeeded\nRegistering slaves..." );

                    Command addSlavesCommand = Commands
                        .getAddSlavesCommand( config.getSlaveNodes(), config.getMasterNode() );
                    manager.getCommandRunner().runCommand( addSlavesCommand );

                    if ( addSlavesCommand.hasSucceeded() )
                    {
                        po.addLog( "Slaves successfully registered\nStarting cluster..." );

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
                            po.addLogDone( "cluster started successfully\nDone" );
                        }
                        else
                        {
                            po.addLogFailed(
                                String.format( "Failed to start cluster, %s", startNodesCommand.getAllErrors() ) );
                        }

                    }
                    else
                    {
                        po.addLogFailed( String
                            .format( "Failed to register slaves with master, %s", addSlavesCommand.getAllErrors() ) );
                    }
                }
                else
                {
                    po.addLogFailed(
                        String.format( "Setting master IP failed, %s", setMasterIPCommand.getAllErrors() ) );
                }

            }
            else
            {
                po.addLogFailed( String.format( "Installation failed, %s", installCommand.getAllErrors() ) );
            }
        }
        else
        {
            po.addLogFailed( "Could not save cluster info to DB! Please see logs\nInstallation aborted" );
        }
    }
}
