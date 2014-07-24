package org.safehaus.subutai.plugin.accumulo.impl.handler;


import java.util.UUID;

import org.safehaus.subutai.api.commandrunner.AgentResult;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.plugin.accumulo.api.AccumuloClusterConfig;
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
public class InstallOperationHandler extends AbstractOperationHandler<AccumuloImpl> {
    private final AccumuloClusterConfig accumuloClusterConfig;
    private final ProductOperation po;


    public InstallOperationHandler( AccumuloImpl manager, AccumuloClusterConfig accumuloClusterConfig ) {
        super( manager, accumuloClusterConfig.getClusterName() );
        this.accumuloClusterConfig = accumuloClusterConfig;
        po = manager.getTracker().createProductOperation( AccumuloClusterConfig.PRODUCT_KEY,
                String.format( "Installing %s", AccumuloClusterConfig.PRODUCT_KEY ) );
    }


    @Override
    public UUID getTrackerId() {
        return po.getId();
    }


    @Override
    public void run() {
        if ( accumuloClusterConfig.getMasterNode() == null || accumuloClusterConfig.getGcNode() == null
                || accumuloClusterConfig.getMonitor() == null || Strings
                .isNullOrEmpty( accumuloClusterConfig.getClusterName() ) || Util
                .isCollectionEmpty( accumuloClusterConfig.getTracers() ) || Util
                .isCollectionEmpty( accumuloClusterConfig.getSlaves() ) ) {
            po.addLogFailed( "Malformed configuration\nInstallation aborted" );
            return;
        }

        if ( manager.getCluster( accumuloClusterConfig.getClusterName() ) != null ) {
            po.addLogFailed( String.format( "Cluster with name '%s' already exists\nInstallation aborted",
                    accumuloClusterConfig.getClusterName() ) );
            return;
        }

        HadoopClusterConfig hadoopConfig =
                manager.getHadoopManager().getCluster( accumuloClusterConfig.getClusterName() );

        if ( hadoopConfig == null ) {
            po.addLogFailed( String.format( "Hadoop cluster with name '%s' not found\nInstallation aborted",
                    accumuloClusterConfig.getClusterName() ) );
            return;
        }

        if ( !hadoopConfig.getAllNodes().containsAll( accumuloClusterConfig.getAllNodes() ) ) {
            po.addLogFailed( String.format( "Not all supplied nodes belong to Hadoop cluster %s \nInstallation aborted",
                    accumuloClusterConfig.getClusterName() ) );
            return;
        }

        ZookeeperClusterConfig zkConfig = manager.getZkManager().getCluster( accumuloClusterConfig.getClusterName() );

        if ( zkConfig == null ) {
            po.addLogFailed( String.format( "Zookeeper cluster with name '%s' not found\nInstallation aborted",
                    accumuloClusterConfig.getClusterName() ) );
            return;
        }

        if ( !zkConfig.getNodes().containsAll( accumuloClusterConfig.getAllNodes() ) ) {
            po.addLogFailed(
                    String.format( "Not all supplied nodes belong to Zookeeper cluster %s \nInstallation aborted",
                            accumuloClusterConfig.getClusterName() ) );
            return;
        }


        po.addLog( "Checking prerequisites..." );

        //check installed ksks packages
        Command checkInstalledCommand = Commands.getCheckInstalledCommand( accumuloClusterConfig.getAllNodes() );
        manager.getCommandRunner().runCommand( checkInstalledCommand );

        if ( !checkInstalledCommand.hasCompleted() ) {
            po.addLogFailed( "Failed to check presence of installed ksks packages\nInstallation aborted" );
            return;
        }

        for ( Agent node : accumuloClusterConfig.getAllNodes() ) {
            AgentResult result = checkInstalledCommand.getResults().get( node.getUuid() );

            if ( result.getStdOut().contains( "ksks-accumulo" ) ) {
                po.addLogFailed( String.format( "Node %s already has Accumulo installed. Installation aborted",
                        node.getHostname() ) );
                return;
            }
            else if ( !result.getStdOut().contains( "ksks-hadoop" ) ) {
                po.addLogFailed( String.format( "Node %s has no Hadoop installation. Installation aborted",
                        node.getHostname() ) );
                return;
            }
            else if ( !result.getStdOut().contains( "ksks-zookeeper" ) ) {
                po.addLogFailed( String.format( "Node %s has no Zookeeper installation. Installation aborted",
                        node.getHostname() ) );
                return;
            }
        }

        po.addLog( "Updating db..." );
        if ( manager.getDbManager().saveInfo( AccumuloClusterConfig.PRODUCT_KEY, accumuloClusterConfig.getClusterName(),
                accumuloClusterConfig ) ) {

            po.addLog( "Cluster info saved to DB\nInstalling Accumulo..." );

            //install
            Command installCommand = Commands.getInstallCommand( accumuloClusterConfig.getAllNodes() );
            manager.getCommandRunner().runCommand( installCommand );

            if ( installCommand.hasSucceeded() ) {
                po.addLog( "Installation succeeded\nSetting master node..." );

                Command setMasterCommand = Commands.getAddMasterCommand( accumuloClusterConfig.getAllNodes(),
                        accumuloClusterConfig.getMasterNode() );
                manager.getCommandRunner().runCommand( setMasterCommand );

                if ( setMasterCommand.hasSucceeded() ) {
                    po.addLog( "Setting master node succeeded\nSetting GC node..." );
                    Command setGCNodeCommand = Commands.getAddGCCommand( accumuloClusterConfig.getAllNodes(),
                            accumuloClusterConfig.getGcNode() );
                    manager.getCommandRunner().runCommand( setGCNodeCommand );
                    if ( setGCNodeCommand.hasSucceeded() ) {
                        po.addLog( "Setting GC node succeeded\nSetting monitor node..." );

                        Command setMonitorCommand = Commands.getAddMonitorCommand( accumuloClusterConfig.getAllNodes(),
                                accumuloClusterConfig.getMonitor() );
                        manager.getCommandRunner().runCommand( setMonitorCommand );

                        if ( setMonitorCommand.hasSucceeded() ) {
                            po.addLog( "Setting monitor node succeeded\nSetting tracers..." );

                            Command setTracersCommand =
                                    Commands.getAddTracersCommand( accumuloClusterConfig.getAllNodes(),
                                            accumuloClusterConfig.getTracers() );
                            manager.getCommandRunner().runCommand( setTracersCommand );

                            if ( setTracersCommand.hasSucceeded() ) {
                                po.addLog( "Setting tracers succeeded\nSetting slaves..." );

                                Command setSlavesCommand =
                                        Commands.getAddSlavesCommand( accumuloClusterConfig.getAllNodes(),
                                                accumuloClusterConfig.getSlaves() );
                                manager.getCommandRunner().runCommand( setSlavesCommand );

                                if ( setSlavesCommand.hasSucceeded() ) {
                                    po.addLog( "Setting slaves succeeded\nSetting ZK cluster..." );

                                    Command setZkClusterCommand =
                                            Commands.getBindZKClusterCommand( accumuloClusterConfig.getAllNodes(),
                                                    zkConfig.getNodes() );
                                    manager.getCommandRunner().runCommand( setZkClusterCommand );

                                    if ( setZkClusterCommand.hasSucceeded() ) {
                                        po.addLog( "Setting ZK cluster succeeded\nInitializing cluster with HDFS..." );

                                        Command initCommand =
                                                Commands.getInitCommand( accumuloClusterConfig.getInstanceName(),
                                                        accumuloClusterConfig.getPassword(),
                                                        accumuloClusterConfig.getMasterNode() );
                                        manager.getCommandRunner().runCommand( initCommand );

                                        if ( initCommand.hasSucceeded() ) {
                                            po.addLog( "Initialization succeeded\nStarting cluster..." );

                                            Command startClusterCommand =
                                                    Commands.getStartCommand( accumuloClusterConfig.getMasterNode() );
                                            manager.getCommandRunner().runCommand( startClusterCommand );

                                            if ( startClusterCommand.hasSucceeded() ) {
                                                po.addLogDone( "Cluster started successfully\nDone" );
                                            }
                                            else {
                                                po.addLogFailed( String.format( "Starting cluster failed, %s",
                                                        startClusterCommand.getAllErrors() ) );
                                            }
                                        }
                                        else {
                                            po.addLogFailed( String.format( "Initialization failed, %s",
                                                    initCommand.getAllErrors() ) );
                                        }
                                    }
                                    else {
                                        po.addLogFailed( String.format( "Setting ZK cluster failed, %s",
                                                setZkClusterCommand.getAllErrors() ) );
                                    }
                                }
                                else {
                                    po.addLogFailed( String.format( "Setting slaves failed, %s",
                                            setSlavesCommand.getAllErrors() ) );
                                }
                            }
                            else {
                                po.addLogFailed( String.format( "Setting tracers failed, %s",
                                        setTracersCommand.getAllErrors() ) );
                            }
                        }
                        else {
                            po.addLogFailed(
                                    String.format( "Setting monitor failed, %s", setMonitorCommand.getAllErrors() ) );
                        }
                    }
                    else {
                        po.addLogFailed(
                                String.format( "Setting gc node failed, %s", setGCNodeCommand.getAllErrors() ) );
                    }
                }
                else {
                    po.addLogFailed(
                            String.format( "Setting master node failed, %s", setMasterCommand.getAllErrors() ) );
                }
            }
            else {
                po.addLogFailed( String.format( "Installation failed, %s", installCommand.getAllErrors() ) );
            }
        }
        else {
            po.addLogFailed( "Could not save cluster info to DB! Please see logs\nInstallation aborted" );
        }
    }
}
