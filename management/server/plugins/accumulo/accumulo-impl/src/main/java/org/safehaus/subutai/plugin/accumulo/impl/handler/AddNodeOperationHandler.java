package org.safehaus.subutai.plugin.accumulo.impl.handler;


import java.util.UUID;

import org.safehaus.subutai.api.commandrunner.AgentResult;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.plugin.accumulo.api.AccumuloClusterConfig;
import org.safehaus.subutai.plugin.accumulo.api.NodeType;
import org.safehaus.subutai.plugin.accumulo.impl.AccumuloImpl;
import org.safehaus.subutai.plugin.accumulo.impl.Commands;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.Util;

import com.google.common.base.Strings;


/**
 * Created by dilshat on 5/6/14.
 */
public class AddNodeOperationHandler extends AbstractOperationHandler<AccumuloImpl> {
    private final ProductOperation po;
    private final String lxcHostname;
    private final NodeType nodeType;


    public AddNodeOperationHandler( AccumuloImpl manager, String clusterName, String lxcHostname, NodeType nodeType ) {
        super( manager, clusterName );
        this.lxcHostname = lxcHostname;
        this.nodeType = nodeType;
        po = manager.getTracker().createProductOperation( AccumuloClusterConfig.PRODUCT_KEY,
                String.format( "Adding node %s of type %s to %s", lxcHostname, nodeType, clusterName ) );
    }


    @Override
    public UUID getTrackerId() {
        return po.getId();
    }


    @Override
    public void run() {
        if ( Strings.isNullOrEmpty( clusterName ) || Strings.isNullOrEmpty( lxcHostname ) || nodeType == null ) {
            po.addLogFailed( "Malformed arguments passed" );
            return;
        }
        if ( !( nodeType == NodeType.TRACER || nodeType.isSlave() ) ) {
            po.addLogFailed( "Only tracer or slave node can be added" );
            return;
        }
        AccumuloClusterConfig accumuloClusterConfig = manager.getCluster( clusterName );
        if ( accumuloClusterConfig == null ) {
            po.addLogFailed( String.format( "Cluster with name %s does not exist\nOperation aborted", clusterName ) );
            return;
        }

        Agent lxcAgent = manager.getAgentManager().getAgentByHostname( lxcHostname );
        if ( lxcAgent == null ) {
            po.addLogFailed( String.format( "Agent %s is not connected\nOperation aborted", lxcHostname ) );
            return;
        }

        if ( nodeType == NodeType.TRACER && accumuloClusterConfig.getTracers().contains( lxcAgent ) ) {
            po.addLogFailed( String.format( "Agent %s already belongs to tracers\nOperation aborted", lxcHostname ) );
            return;
        }
        else if ( nodeType.isSlave() && accumuloClusterConfig.getSlaves().contains( lxcAgent ) ) {
            po.addLogFailed( String.format( "Agent %s already belongs to slaves\nOperation aborted", lxcHostname ) );
            return;
        }

        //check installed ksks packages
        Command checkInstalledCommand = Commands.getCheckInstalledCommand( Util.wrapAgentToSet( lxcAgent ) );
        manager.getCommandRunner().runCommand( checkInstalledCommand );

        if ( !checkInstalledCommand.hasCompleted() ) {
            po.addLogFailed( "Failed to check presence of installed ksks packages\nInstallation aborted" );
            return;
        }

        AgentResult result = checkInstalledCommand.getResults().get( lxcAgent.getUuid() );

        if ( !result.getStdOut().contains( "ksks-hadoop" ) ) {
            po.addLogFailed( String.format( "Node %s has no Hadoop installation. Installation aborted",
                    lxcAgent.getHostname() ) );
            return;
        }
        else if ( !result.getStdOut().contains( "ksks-zookeeper" ) ) {
            po.addLogFailed( String.format( "Node %s has no Zookeeper installation. Installation aborted",
                    lxcAgent.getHostname() ) );
            return;
        }

        boolean install = !result.getStdOut().contains( "ksks-accumulo" );

        HadoopClusterConfig hadoopConfig =
                manager.getHadoopManager().getCluster( accumuloClusterConfig.getClusterName() );

        if ( hadoopConfig == null ) {
            po.addLogFailed( String.format( "Hadoop cluster with name '%s' not found\nInstallation aborted",
                    accumuloClusterConfig.getClusterName() ) );
            return;
        }

        if ( !hadoopConfig.getAllNodes().contains( lxcAgent ) ) {
            po.addLogFailed( String.format( "Node '%s' does not belong to Hadoop cluster %s\nInstallation aborted",
                    lxcAgent.getHostname(), accumuloClusterConfig.getClusterName() ) );
            return;
        }

        ZookeeperClusterConfig zkConfig = manager.getZkManager().getCluster( accumuloClusterConfig.getClusterName() );

        if ( zkConfig == null ) {
            po.addLogFailed( String.format( "Zookeeper cluster with name '%s' not found\nInstallation aborted",
                    accumuloClusterConfig.getClusterName() ) );
            return;
        }

        if ( !zkConfig.getNodes().contains( lxcAgent ) ) {
            po.addLogFailed( String.format( "Node '%s' does not belong to Zookeeper cluster %s\nInstallation aborted",
                    lxcAgent.getHostname(), accumuloClusterConfig.getClusterName() ) );
            return;
        }


        if ( nodeType.isSlave() ) {
            accumuloClusterConfig.getSlaves().add( lxcAgent );
        }
        else {
            accumuloClusterConfig.getTracers().add( lxcAgent );
        }

        po.addLog( "Updating DB..." );
        if ( manager.getDbManager().saveInfo( AccumuloClusterConfig.PRODUCT_KEY, accumuloClusterConfig.getClusterName(),
                accumuloClusterConfig ) ) {

            po.addLog( "Cluster info updated in DB" );

            if ( install ) {
                po.addLog( String.format( "Installing %s on %s node...", AccumuloClusterConfig.PRODUCT_KEY,
                        lxcAgent.getHostname() ) );

                Command installCommand = Commands.getInstallCommand( Util.wrapAgentToSet( lxcAgent ) );
                manager.getCommandRunner().runCommand( installCommand );

                if ( installCommand.hasSucceeded() ) {
                    po.addLog( "Installation succeeded" );
                }
                else {
                    po.addLogFailed( String.format( "Installation failed, %s\nOperation aborted",
                            installCommand.getAllErrors() ) );
                    return;
                }
            }
            po.addLog( "Registering node with cluster..." );

            Command addNodeCommand;
            if ( nodeType.isSlave() ) {
                addNodeCommand = Commands.getAddSlavesCommand( accumuloClusterConfig.getAllNodes(),
                        accumuloClusterConfig.getSlaves() );
            }
            else {
                addNodeCommand = Commands.getAddTracersCommand( accumuloClusterConfig.getAllNodes(),
                        accumuloClusterConfig.getTracers() );
            }
            manager.getCommandRunner().runCommand( addNodeCommand );

            if ( addNodeCommand.hasSucceeded() ) {
                po.addLog( "Node registration succeeded\nSetting master node..." );

                Command setMasterNodeCommand = Commands.getAddMasterCommand( Util.wrapAgentToSet( lxcAgent ),
                        accumuloClusterConfig.getMasterNode() );
                manager.getCommandRunner().runCommand( setMasterNodeCommand );

                if ( setMasterNodeCommand.hasSucceeded() ) {

                    po.addLog( "Setting master node succeeded\nSetting GC node..." );

                    Command setGcNodeCommand = Commands.getAddGCCommand( Util.wrapAgentToSet( lxcAgent ),
                            accumuloClusterConfig.getGcNode() );
                    manager.getCommandRunner().runCommand( setGcNodeCommand );

                    if ( setGcNodeCommand.hasSucceeded() ) {

                        po.addLog( "Setting GC node succeeded\nSetting monitor node..." );

                        Command setMonitorCommand = Commands.getAddMonitorCommand( Util.wrapAgentToSet( lxcAgent ),
                                accumuloClusterConfig.getMonitor() );
                        manager.getCommandRunner().runCommand( setMonitorCommand );

                        if ( setMonitorCommand.hasSucceeded() ) {

                            po.addLog( "Setting monitor node succeeded\nSetting tracers/slaves..." );

                            Command setTracersSlavesCommand = nodeType.isSlave() ? Commands.getAddTracersCommand(
                                    Util.wrapAgentToSet( lxcAgent ), accumuloClusterConfig.getTracers() ) :
                                                              Commands.getAddSlavesCommand(
                                                                      Util.wrapAgentToSet( lxcAgent ),
                                                                      accumuloClusterConfig.getSlaves() );

                            manager.getCommandRunner().runCommand( setTracersSlavesCommand );

                            if ( setTracersSlavesCommand.hasSucceeded() ) {

                                po.addLog( "Setting tracers/slaves succeeded\nSetting Zk cluster..." );

                                Command setZkClusterCommand =
                                        Commands.getBindZKClusterCommand( Util.wrapAgentToSet( lxcAgent ),
                                                zkConfig.getNodes() );
                                manager.getCommandRunner().runCommand( setZkClusterCommand );

                                if ( setZkClusterCommand.hasSucceeded() ) {
                                    po.addLog( "Setting ZK cluster succeeded\nRestarting cluster..." );

                                    Command restartClusterCommand =
                                            Commands.getRestartCommand( accumuloClusterConfig.getMasterNode() );
                                    manager.getCommandRunner().runCommand( restartClusterCommand );

                                    if ( restartClusterCommand.hasSucceeded() ) {
                                        po.addLogDone( "Cluster restarted successfully\nDone" );
                                    }
                                    else {
                                        po.addLogFailed( String.format( "Cluster restart failed, %s",
                                                restartClusterCommand.getAllErrors() ) );
                                    }
                                }
                                else {
                                    po.addLogFailed( String.format( "Setting ZK cluster failed, %s",
                                            setZkClusterCommand.getAllErrors() ) );
                                }
                            }
                            else {
                                po.addLogFailed( String.format( "Setting tracers/slaves failed, %s",
                                        setTracersSlavesCommand.getAllErrors() ) );
                            }
                        }
                        else {
                            po.addLogFailed( String.format( "Setting monitor node failed, %s",
                                    setMonitorCommand.getAllErrors() ) );
                        }
                    }
                    else {
                        po.addLogFailed(
                                String.format( "Setting GC node failed, %s", setGcNodeCommand.getAllErrors() ) );
                    }
                }
                else {
                    po.addLogFailed(
                            String.format( "Setting master node failed, %s", setMasterNodeCommand.getAllErrors() ) );
                }
            }
            else {
                po.addLogFailed( String.format( "Node registration failed, %s", addNodeCommand.getAllErrors() ) );
            }
        }
        else {
            po.addLogFailed(
                    "Error while updating cluster info in DB. Check logs. Use Terminal Module to cleanup\nFailed" );
        }
    }
}
