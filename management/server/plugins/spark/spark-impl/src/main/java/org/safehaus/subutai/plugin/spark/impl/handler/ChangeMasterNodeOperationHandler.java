package org.safehaus.subutai.plugin.spark.impl.handler;


import java.util.concurrent.atomic.AtomicInteger;

import org.safehaus.subutai.common.command.AgentResult;
import org.safehaus.subutai.common.command.Command;
import org.safehaus.subutai.common.command.CommandCallback;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.Response;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.common.util.StringUtil;
import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;
import org.safehaus.subutai.plugin.spark.impl.Commands;
import org.safehaus.subutai.plugin.spark.impl.SparkImpl;


public class ChangeMasterNodeOperationHandler extends AbstractOperationHandler<SparkImpl>
{

    private final String newMasterHostname;
    private final boolean keepSlave;


    public ChangeMasterNodeOperationHandler( SparkImpl manager, String clusterName, String newMasterHostname,
                                             boolean keepSlave )
    {
        super( manager, clusterName );
        this.newMasterHostname = newMasterHostname;
        this.keepSlave = keepSlave;
        productOperation = manager.getTracker().createProductOperation( SparkClusterConfig.PRODUCT_KEY,
                String.format( "Changing master to %s in %s", newMasterHostname, clusterName ) );
    }


    @Override
    public void run()
    {
        ProductOperation po = productOperation;
        final SparkClusterConfig config = manager.getCluster( clusterName );
        if ( config == null )
        {
            po.addLogFailed( String.format( "Cluster with name %s does not exist. Operation aborted", clusterName ) );
            return;
        }

        if ( manager.getAgentManager().getAgentByHostname( config.getMasterNode().getHostname() ) == null )
        {
            po.addLogFailed( String.format( "Master node %s is not connected. Operation aborted",
                    config.getMasterNode().getHostname() ) );
            return;
        }

        Agent newMaster = manager.getAgentManager().getAgentByHostname( newMasterHostname );
        if ( newMaster == null )
        {
            po.addLogFailed(
                    String.format( "Agent with hostname %s is not connected. Operation aborted", newMasterHostname ) );
            return;
        }

        if ( newMaster.equals( config.getMasterNode() ) )
        {
            po.addLogFailed(
                    String.format( "Node %s is already a master node. Operation aborted", newMasterHostname ) );
            return;
        }

        //check if node is in the cluster
        if ( !config.getAllNodes().contains( newMaster ) )
        {
            po.addLogFailed(
                    String.format( "Node %s does not belong to this cluster. Operation aborted", newMasterHostname ) );
            return;
        }

        po.addLog( "Stopping all nodes..." );
        //stop all nodes
        Command stopNodesCommand = Commands.getStopAllCommand( config.getMasterNode() );
        manager.getCommandRunner().runCommand( stopNodesCommand );
        if ( stopNodesCommand.hasSucceeded() )
        {
            po.addLog( "All nodes stopped\nClearing slaves on old master..." );
            //clear slaves from old master
            Command clearSlavesCommand = Commands.getClearSlavesCommand( config.getMasterNode() );
            manager.getCommandRunner().runCommand( clearSlavesCommand );
            if ( clearSlavesCommand.hasSucceeded() )
            {
                po.addLog( "Slaves cleared successfully" );
            }
            else
            {
                po.addLog(
                        String.format( "Clearing slaves failed, %s, skipping...", clearSlavesCommand.getAllErrors() ) );
            }
            //add slaves to new master, if keepSlave=true then master node is also added as slave
            config.getSlaveNodes().add( config.getMasterNode() );
            config.setMasterNode( newMaster );
            if ( keepSlave )
            {
                config.getSlaveNodes().add( newMaster );
            }
            else
            {
                config.getSlaveNodes().remove( newMaster );
            }
            po.addLog( "Adding nodes to new master..." );
            Command addSlavesCommand = Commands.getAddSlavesCommand( config.getSlaveNodes(), config.getMasterNode() );
            manager.getCommandRunner().runCommand( addSlavesCommand );
            if ( addSlavesCommand.hasSucceeded() )
            {
                po.addLog( "Nodes added successfully\nSetting new master IP..." );
                //modify master ip on all nodes
                Command setMasterIPCommand =
                        Commands.getSetMasterIPCommand( config.getMasterNode(), config.getAllNodes() );
                manager.getCommandRunner().runCommand( setMasterIPCommand );
                if ( setMasterIPCommand.hasSucceeded() )
                {
                    po.addLog( "Master IP set successfully\nStarting cluster..." );
                    //start master & slaves

                    Command startNodesCommand = Commands.getStartAllCommand( config.getMasterNode() );
                    final AtomicInteger okCount = new AtomicInteger( 0 );
                    manager.getCommandRunner().runCommand( startNodesCommand, new CommandCallback()
                    {

                        @Override
                        public void onResponse( Response response, AgentResult agentResult, Command command )
                        {
                            okCount.set( StringUtil.countNumberOfOccurences( agentResult.getStdOut(), "starting" ) );

                            if ( okCount.get() >= config.getAllNodes().size() )
                            {
                                stop();
                            }
                        }
                    } );

                    if ( okCount.get() >= config.getAllNodes().size() )
                    {
                        po.addLog( "Cluster started successfully" );
                    }
                    else
                    {
                        po.addLog( String.format( "Start of cluster failed, %s, skipping...",
                                startNodesCommand.getAllErrors() ) );
                    }

                    po.addLog( "Updating db..." );
                    //update db
                    manager.getPluginDAO().saveInfo( SparkClusterConfig.PRODUCT_KEY, clusterName, config );
                    po.addLogDone( "Cluster info updated in DB\nDone" );
                }
                else
                {
                    po.addLogFailed( String.format( "Failed to set master IP on all nodes, %s. Operation aborted",
                            setMasterIPCommand.getAllErrors() ) );
                }
            }
            else
            {
                po.addLogFailed( String.format( "Failed to add slaves to new master, %s. Operation aborted",
                        addSlavesCommand.getAllErrors() ) );
            }
        }
        else
        {
            po.addLogFailed( String.format( "Failed to stop all nodes, %s", stopNodesCommand.getAllErrors() ) );
        }
    }
}
