package org.safehaus.subutai.impl.spark.handler;


import org.safehaus.subutai.api.commandrunner.AgentResult;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.commandrunner.CommandCallback;
import org.safehaus.subutai.api.spark.Config;
import org.safehaus.subutai.common.StringUtil;
import org.safehaus.subutai.impl.spark.Commands;
import org.safehaus.subutai.impl.spark.SparkImpl;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.Response;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Created by dilshat on 5/7/14.
 */
public class ChangeMasterNodeOperationHandler extends AbstractOperationHandler<SparkImpl>
{
    private final ProductOperation po;
    private final String newMasterHostname;
    private final boolean keepSlave;


    public ChangeMasterNodeOperationHandler( SparkImpl manager, String clusterName, String newMasterHostname,
        boolean keepSlave )
    {
        super( manager, clusterName );
        this.newMasterHostname = newMasterHostname;
        this.keepSlave = keepSlave;
        po = manager.getTracker().createProductOperation( Config.PRODUCT_KEY,
            String.format( "Changing master to %s in %s", newMasterHostname, clusterName ) );
    }


    @Override
    public UUID getTrackerId()
    {
        return po.getId();
    }


    @Override
    public void run()
    {
        final Config config = manager.getCluster( clusterName );
        if ( config == null )
        {
            po.addLogFailed( String.format( "Cluster with name %s does not exist\nOperation aborted", clusterName ) );
            return;
        }

        if ( manager.getAgentManager().getAgentByHostname( config.getMasterNode().getHostname() ) == null )
        {
            po.addLogFailed( String
                .format( "Master node %s is not connected\nOperation aborted", config.getMasterNode().getHostname() ) );
            return;
        }

        Agent newMaster = manager.getAgentManager().getAgentByHostname( newMasterHostname );
        if ( newMaster == null )
        {
            po.addLogFailed(
                String.format( "Agent with hostname %s is not connected\nOperation aborted", newMasterHostname ) );
            return;
        }

        if ( newMaster.equals( config.getMasterNode() ) )
        {
            po.addLogFailed(
                String.format( "Node %s is already a master node\nOperation aborted", newMasterHostname ) );
            return;
        }

        //check if node is in the cluster
        if ( !config.getAllNodes().contains( newMaster ) )
        {
            po.addLogFailed(
                String.format( "Node %s does not belong to this cluster\nOperation aborted", newMasterHostname ) );
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
                Command setMasterIPCommand = Commands
                    .getSetMasterIPCommand( config.getMasterNode(), config.getAllNodes() );
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
                        po.addLog( String
                            .format( "Start of cluster failed, %s, skipping...", startNodesCommand.getAllErrors() ) );
                    }

                    po.addLog( "Updating db..." );
                    //update db
                    if ( manager.getDbManager().saveInfo( Config.PRODUCT_KEY, clusterName, config ) )
                    {
                        po.addLogDone( "Cluster info updated in DB\nDone" );
                    }
                    else
                    {
                        po.addLogFailed( "Error while updating cluster info in DB. Check logs.\nFailed" );
                    }
                }
                else
                {
                    po.addLogFailed( String.format( "Failed to set master IP on all nodes, %s\nOperation aborted",
                        setMasterIPCommand.getAllErrors() ) );
                }
            }
            else
            {
                po.addLogFailed( String.format( "Failed to add slaves to new master, %s\nOperation aborted",
                    addSlavesCommand.getAllErrors() ) );
            }

        }
        else
        {
            po.addLogFailed( String.format( "Failed to stop all nodes, %s", stopNodesCommand.getAllErrors() ) );
        }
    }
}
