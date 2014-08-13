package org.safehaus.subutai.impl.accumulo.handler;


import java.util.UUID;

import org.safehaus.subutai.api.accumulo.Config;
import org.safehaus.subutai.api.commandrunner.AgentResult;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.dbmanager.DBException;
import org.safehaus.subutai.impl.accumulo.AccumuloImpl;
import org.safehaus.subutai.impl.accumulo.Commands;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.Util;

import com.google.common.base.Strings;


/**
 * Created by dilshat on 5/6/14.
 */
public class InstallOperationHandler extends AbstractOperationHandler<AccumuloImpl> {
    private final Config config;
    private final ProductOperation po;


    public InstallOperationHandler( AccumuloImpl manager, Config config ) {
        super( manager, config.getClusterName() );
        this.config = config;
        po = manager.getTracker()
                    .createProductOperation( Config.PRODUCT_KEY, String.format( "Installing %s", Config.PRODUCT_KEY ) );
    }


    @Override
    public UUID getTrackerId() {
        return po.getId();
    }


    @Override
    public void run() {
        if ( config.getMasterNode() == null || config.getGcNode() == null || config.getMonitor() == null || Strings
                .isNullOrEmpty( config.getClusterName() ) || Util.isCollectionEmpty( config.getTracers() ) || Util
                .isCollectionEmpty( config.getSlaves() ) ) {
            po.addLogFailed( "Malformed configuration\nInstallation aborted" );
            return;
        }

        if ( config.getMasterNode().equals( config.getGcNode() ) ) {
            po.addLogFailed( "Master and CG can not be the same node" );
            return;
        }

        if ( manager.getCluster( config.getClusterName() ) != null ) {
            po.addLogFailed( String.format( "Cluster with name '%s' already exists\nInstallation aborted",
                    config.getClusterName() ) );
            return;
        }

        org.safehaus.subutai.api.hadoop.Config hadoopConfig =
                manager.getHadoopManager().getCluster( config.getClusterName() );

        if ( hadoopConfig == null ) {
            po.addLogFailed( String.format( "Hadoop cluster with name '%s' not found\nInstallation aborted",
                    config.getClusterName() ) );
            return;
        }

        if ( !hadoopConfig.getAllNodes().containsAll( config.getAllNodes() ) ) {
            po.addLogFailed( String.format( "Not all supplied nodes belong to Hadoop cluster %s \nInstallation aborted",
                    config.getClusterName() ) );
            return;
        }

        org.safehaus.subutai.api.zookeeper.Config zkConfig =
                manager.getZkManager().getCluster( config.getClusterName() );

        if ( zkConfig == null ) {
            po.addLogFailed( String.format( "Zookeeper cluster with name '%s' not found\nInstallation aborted",
                    config.getClusterName() ) );
            return;
        }

        if ( !zkConfig.getNodes().containsAll( config.getAllNodes() ) ) {
            po.addLogFailed(
                    String.format( "Not all supplied nodes belong to Zookeeper cluster %s \nInstallation aborted",
                            config.getClusterName() ) );
            return;
        }


        po.addLog( "Checking prerequisites..." );

        //check installed ksks packages
        Command checkInstalledCommand = Commands.getCheckInstalledCommand( config.getAllNodes() );
        manager.getCommandRunner().runCommand( checkInstalledCommand );

        if ( !checkInstalledCommand.hasCompleted() ) {
            po.addLogFailed( "Failed to check presence of installed ksks packages\nInstallation aborted" );
            return;
        }

        for ( Agent node : config.getAllNodes() ) {
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

        //install
        Command installCommand = Commands.getInstallCommand( config.getAllNodes() );
        manager.getCommandRunner().runCommand( installCommand );

        if ( installCommand.hasSucceeded() ) {
            po.addLog( "Installation succeeded\nSetting master node..." );

            Command setMasterCommand = Commands.getAddMasterCommand( config.getAllNodes(), config.getMasterNode() );
            manager.getCommandRunner().runCommand( setMasterCommand );

            if ( setMasterCommand.hasSucceeded() ) {
                po.addLog( "Setting master node succeeded\nSetting GC node..." );
                Command setGCNodeCommand = Commands.getAddGCCommand( config.getAllNodes(), config.getGcNode() );
                manager.getCommandRunner().runCommand( setGCNodeCommand );
                if ( setGCNodeCommand.hasSucceeded() ) {
                    po.addLog( "Setting GC node succeeded\nSetting monitor node..." );

                    Command setMonitorCommand =
                            Commands.getAddMonitorCommand( config.getAllNodes(), config.getMonitor() );
                    manager.getCommandRunner().runCommand( setMonitorCommand );

                    if ( setMonitorCommand.hasSucceeded() ) {
                        po.addLog( "Setting monitor node succeeded\nSetting tracers..." );

                        Command setTracersCommand =
                                Commands.getAddTracersCommand( config.getAllNodes(), config.getTracers() );
                        manager.getCommandRunner().runCommand( setTracersCommand );

                        if ( setTracersCommand.hasSucceeded() ) {
                            po.addLog( "Setting tracers succeeded\nSetting slaves..." );

                            Command setSlavesCommand =
                                    Commands.getAddSlavesCommand( config.getAllNodes(), config.getSlaves() );
                            manager.getCommandRunner().runCommand( setSlavesCommand );

                            if ( setSlavesCommand.hasSucceeded() ) {
                                po.addLog( "Setting slaves succeeded\nSetting ZK cluster..." );

                                Command setZkClusterCommand =
                                        Commands.getBindZKClusterCommand( config.getAllNodes(), zkConfig.getNodes() );
                                manager.getCommandRunner().runCommand( setZkClusterCommand );

                                if ( setZkClusterCommand.hasSucceeded() ) {
                                    po.addLog( "Setting ZK cluster succeeded\nInitializing cluster with HDFS..." );

                                    Command initCommand =
                                            Commands.getInitCommand( config.getInstanceName(), config.getPassword(),
                                                    config.getMasterNode() );
                                    manager.getCommandRunner().runCommand( initCommand );

                                    if ( initCommand.hasSucceeded() ) {
                                        po.addLog( "Initialization succeeded\nStarting cluster..." );

                                        Command startClusterCommand =
                                                Commands.getStartCommand( config.getMasterNode() );
                                        manager.getCommandRunner().runCommand( startClusterCommand );

                                        if ( startClusterCommand.hasSucceeded() ) {
                                            po.addLog( "Cluster started successfully" );
                                        }
                                        else {
                                            po.addLog( String.format( "Starting cluster failed, %s, skipping...",
                                                    startClusterCommand.getAllErrors() ) );
                                        }
                                        po.addLog( "Updating db..." );

                                        try {
                                            manager.getDbManager()
                                                   .saveInfo2( Config.PRODUCT_KEY, config.getClusterName(), config );

                                            po.addLogDone( "Database information updated" );
                                        }
                                        catch ( DBException e ) {
                                            po.addLogFailed( String.format( "Failed to update database information, %s",
                                                    e.getMessage() ) );
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
                                po.addLogFailed(
                                        String.format( "Setting slaves failed, %s", setSlavesCommand.getAllErrors() ) );
                            }
                        }
                        else {
                            po.addLogFailed(
                                    String.format( "Setting tracers failed, %s", setTracersCommand.getAllErrors() ) );
                        }
                    }
                    else {
                        po.addLogFailed(
                                String.format( "Setting monitor failed, %s", setMonitorCommand.getAllErrors() ) );
                    }
                }
                else {
                    po.addLogFailed( String.format( "Setting gc node failed, %s", setGCNodeCommand.getAllErrors() ) );
                }
            }
            else {
                po.addLogFailed( String.format( "Setting master node failed, %s", setMasterCommand.getAllErrors() ) );
            }
        }
        else {
            po.addLogFailed( String.format( "Installation failed, %s", installCommand.getAllErrors() ) );
        }
    }
}
