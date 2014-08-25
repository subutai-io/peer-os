package org.safehaus.subutai.plugin.accumulo.impl;


import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.dbmanager.DBException;
import org.safehaus.subutai.plugin.accumulo.api.AccumuloClusterConfig;
import org.safehaus.subutai.plugin.accumulo.api.NodeType;
import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.ClusterConfigurationException;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;


/**
 * Configures Accumulo Cluster
 */
public class ClusterConfiguration {

    private ProductOperation po;
    private AccumuloImpl accumuloManager;


    public ClusterConfiguration( final ProductOperation po, final AccumuloImpl accumuloManager ) {
        Preconditions.checkNotNull( accumuloManager, "Accumulo Manager is null" );
        Preconditions.checkNotNull( po, "Product Operation is null" );
        this.po = po;
        this.accumuloManager = accumuloManager;
    }


    public void configureCluster( AccumuloClusterConfig accumuloClusterConfig,
                                  ZookeeperClusterConfig zookeeperClusterConfig ) throws ClusterConfigurationException {

        po.addLog( "Configuring cluster..." );

        Command setMasterCommand = Commands.getAddMasterCommand( accumuloClusterConfig.getAllNodes(),
                accumuloClusterConfig.getMasterNode() );
        accumuloManager.getCommandRunner().runCommand( setMasterCommand );

        if ( setMasterCommand.hasSucceeded() ) {
            po.addLog( "Setting master node succeeded\nSetting GC node..." );
            Command setGCNodeCommand =
                    Commands.getAddGCCommand( accumuloClusterConfig.getAllNodes(), accumuloClusterConfig.getGcNode() );
            accumuloManager.getCommandRunner().runCommand( setGCNodeCommand );
            if ( setGCNodeCommand.hasSucceeded() ) {
                po.addLog( "Setting GC node succeeded\nSetting monitor node..." );

                Command setMonitorCommand = Commands.getAddMonitorCommand( accumuloClusterConfig.getAllNodes(),
                        accumuloClusterConfig.getMonitor() );
                accumuloManager.getCommandRunner().runCommand( setMonitorCommand );

                if ( setMonitorCommand.hasSucceeded() ) {
                    po.addLog( "Setting monitor node succeeded\nSetting tracers..." );

                    Command setTracersCommand = Commands.getAddTracersCommand( accumuloClusterConfig.getAllNodes(),
                            accumuloClusterConfig.getTracers() );
                    accumuloManager.getCommandRunner().runCommand( setTracersCommand );

                    if ( setTracersCommand.hasSucceeded() ) {
                        po.addLog( "Setting tracers succeeded\nSetting slaves..." );

                        Command setSlavesCommand = Commands.getAddSlavesCommand( accumuloClusterConfig.getAllNodes(),
                                accumuloClusterConfig.getSlaves() );
                        accumuloManager.getCommandRunner().runCommand( setSlavesCommand );

                        if ( setSlavesCommand.hasSucceeded() ) {
                            po.addLog( "Setting slaves succeeded\nSetting ZK cluster..." );

                            Command setZkClusterCommand =
                                    Commands.getBindZKClusterCommand( accumuloClusterConfig.getAllNodes(),
                                            zookeeperClusterConfig.getNodes() );
                            accumuloManager.getCommandRunner().runCommand( setZkClusterCommand );

                            if ( setZkClusterCommand.hasSucceeded() ) {
                                po.addLog( "Setting ZK cluster succeeded\nInitializing cluster with HDFS..." );

                                Command initCommand = Commands.getInitCommand( accumuloClusterConfig.getInstanceName(),
                                        accumuloClusterConfig.getPassword(), accumuloClusterConfig.getMasterNode() );
                                accumuloManager.getCommandRunner().runCommand( initCommand );

                                if ( initCommand.hasSucceeded() ) {
                                    po.addLog( "Initialization succeeded\nStarting cluster..." );

                                    Command startClusterCommand =
                                            Commands.getStartCommand( accumuloClusterConfig.getMasterNode() );
                                    accumuloManager.getCommandRunner().runCommand( startClusterCommand );

                                    if ( startClusterCommand.hasSucceeded() ) {
                                        po.addLog( "Cluster started successfully" );
                                    }
                                    else {
                                        po.addLog( String.format( "Starting cluster failed, %s, skipping...",
                                                startClusterCommand.getAllErrors() ) );
                                    }

                                    po.addLog( "Updating information in database..." );
                                    try {
                                        accumuloManager.getPluginDAO().saveInfo( AccumuloClusterConfig.PRODUCT_KEY,
                                                accumuloClusterConfig.getClusterName(), accumuloClusterConfig );

                                        po.addLog( "Updated information in database" );
                                    }
                                    catch ( DBException e ) {
                                        throw new ClusterConfigurationException(
                                                String.format( "Failed to update information in database, %s",
                                                        e.getMessage() ) );
                                    }
                                }
                                else {
                                    throw new ClusterConfigurationException(
                                            String.format( "Initialization failed, %s", initCommand.getAllErrors() ) );
                                }
                            }
                            else {
                                throw new ClusterConfigurationException( String.format( "Setting ZK cluster failed, %s",
                                        setZkClusterCommand.getAllErrors() ) );
                            }
                        }
                        else {
                            throw new ClusterConfigurationException(
                                    String.format( "Setting slaves failed, %s", setSlavesCommand.getAllErrors() ) );
                        }
                    }
                    else {
                        throw new ClusterConfigurationException(
                                String.format( "Setting tracers failed, %s", setTracersCommand.getAllErrors() ) );
                    }
                }
                else {
                    throw new ClusterConfigurationException(
                            String.format( "Setting monitor failed, %s", setMonitorCommand.getAllErrors() ) );
                }
            }
            else {
                throw new ClusterConfigurationException(
                        String.format( "Setting gc node failed, %s", setGCNodeCommand.getAllErrors() ) );
            }
        }
        else {
            throw new ClusterConfigurationException(
                    String.format( "Setting master node failed, %s", setMasterCommand.getAllErrors() ) );
        }
    }


    public void addNode( AccumuloClusterConfig accumuloClusterConfig, ZookeeperClusterConfig zookeeperClusterConfig,
                         Agent agent, NodeType nodeType, boolean install ) throws ClusterConfigurationException {


        if ( install ) {
            po.addLog( String.format( "Installing %s on %s node...", AccumuloClusterConfig.PRODUCT_NAME,
                    agent.getHostname() ) );

            Command installCommand = Commands.getInstallCommand( Sets.newHashSet( agent ) );
            accumuloManager.getCommandRunner().runCommand( installCommand );

            if ( installCommand.hasSucceeded() ) {
                po.addLog( "Installation succeeded" );
            }
            else {
                throw new ClusterConfigurationException(
                        String.format( "Installation failed, %s", installCommand.getAllErrors() ) );
            }
        }

        po.addLog( "Registering node with cluster..." );

        Command addNodeCommand;
        if ( nodeType.isSlave() ) {
            accumuloClusterConfig.getSlaves().add( agent );
            addNodeCommand = Commands.getAddSlavesCommand( accumuloClusterConfig.getAllNodes(),
                    accumuloClusterConfig.getSlaves() );
        }
        else {
            accumuloClusterConfig.getTracers().add( agent );
            addNodeCommand = Commands.getAddTracersCommand( accumuloClusterConfig.getAllNodes(),
                    accumuloClusterConfig.getTracers() );
        }
        accumuloManager.getCommandRunner().runCommand( addNodeCommand );

        if ( addNodeCommand.hasSucceeded() ) {
            po.addLog( "Node registration succeeded\nSetting master node..." );

            Command setMasterNodeCommand =
                    Commands.getAddMasterCommand( Sets.newHashSet( agent ), accumuloClusterConfig.getMasterNode() );
            accumuloManager.getCommandRunner().runCommand( setMasterNodeCommand );

            if ( setMasterNodeCommand.hasSucceeded() ) {

                po.addLog( "Setting master node succeeded\nSetting GC node..." );

                Command setGcNodeCommand =
                        Commands.getAddGCCommand( Sets.newHashSet( agent ), accumuloClusterConfig.getGcNode() );
                accumuloManager.getCommandRunner().runCommand( setGcNodeCommand );

                if ( setGcNodeCommand.hasSucceeded() ) {

                    po.addLog( "Setting GC node succeeded\nSetting monitor node..." );

                    Command setMonitorCommand = Commands.getAddMonitorCommand( Sets.newHashSet( agent ),
                            accumuloClusterConfig.getMonitor() );
                    accumuloManager.getCommandRunner().runCommand( setMonitorCommand );

                    if ( setMonitorCommand.hasSucceeded() ) {

                        po.addLog( "Setting monitor node succeeded\nSetting tracers/slaves..." );

                        Command setTracersSlavesCommand = nodeType.isSlave() ?
                                                          Commands.getAddTracersCommand( Sets.newHashSet( agent ),
                                                                  accumuloClusterConfig.getTracers() ) :
                                                          Commands.getAddSlavesCommand( Sets.newHashSet( agent ),
                                                                  accumuloClusterConfig.getSlaves() );

                        accumuloManager.getCommandRunner().runCommand( setTracersSlavesCommand );

                        if ( setTracersSlavesCommand.hasSucceeded() ) {

                            po.addLog( "Setting tracers/slaves succeeded\nSetting Zk cluster..." );

                            Command setZkClusterCommand = Commands.getBindZKClusterCommand( Sets.newHashSet( agent ),
                                    zookeeperClusterConfig.getNodes() );
                            accumuloManager.getCommandRunner().runCommand( setZkClusterCommand );

                            if ( setZkClusterCommand.hasSucceeded() ) {
                                po.addLog( "Setting ZK cluster succeeded\nRestarting cluster..." );

                                Command restartClusterCommand =
                                        Commands.getRestartCommand( accumuloClusterConfig.getMasterNode() );
                                accumuloManager.getCommandRunner().runCommand( restartClusterCommand );

                                if ( restartClusterCommand.hasSucceeded() ) {
                                    po.addLog( "Cluster restarted successfully" );
                                }
                                else {
                                    po.addLog( String.format( "Cluster restart failed, %s, skipping...",
                                            restartClusterCommand.getAllErrors() ) );
                                }

                                po.addLog( "Updating information in database..." );
                                try {
                                    accumuloManager.getPluginDAO().saveInfo( AccumuloClusterConfig.PRODUCT_KEY,
                                            accumuloClusterConfig.getClusterName(), accumuloClusterConfig );

                                    po.addLog( "Updated information in database" );
                                }
                                catch ( DBException e ) {
                                    throw new ClusterConfigurationException(
                                            String.format( "Failed to update information in database, %s",
                                                    e.getMessage() ) );
                                }
                            }
                            else {
                                throw new ClusterConfigurationException( String.format( "Setting ZK cluster failed, %s",
                                        setZkClusterCommand.getAllErrors() ) );
                            }
                        }
                        else {
                            throw new ClusterConfigurationException( String.format( "Setting tracers/slaves failed, %s",
                                    setTracersSlavesCommand.getAllErrors() ) );
                        }
                    }
                    else {
                        throw new ClusterConfigurationException(
                                String.format( "Setting monitor node failed, %s", setMonitorCommand.getAllErrors() ) );
                    }
                }
                else {
                    throw new ClusterConfigurationException(
                            String.format( "Setting GC node failed, %s", setGcNodeCommand.getAllErrors() ) );
                }
            }
            else {
                throw new ClusterConfigurationException(
                        String.format( "Setting master node failed, %s", setMasterNodeCommand.getAllErrors() ) );
            }
        }
        else {
            throw new ClusterConfigurationException(
                    String.format( "Node registration failed, %s", addNodeCommand.getAllErrors() ) );
        }
    }


    public void removeNode( AccumuloClusterConfig accumuloClusterConfig, Agent agent, NodeType nodeType )
            throws ClusterConfigurationException {
        Command unregisterNodeCommand;
        if ( nodeType == NodeType.TRACER ) {
            unregisterNodeCommand = Commands.getClearTracerCommand( accumuloClusterConfig.getAllNodes(), agent );
            accumuloClusterConfig.getTracers().remove( agent );
        }
        else {
            unregisterNodeCommand = Commands.getClearSlaveCommand( accumuloClusterConfig.getAllNodes(), agent );
            accumuloClusterConfig.getSlaves().remove( agent );
        }

        po.addLog( "Unregistering node from cluster..." );
        accumuloManager.getCommandRunner().runCommand( unregisterNodeCommand );

        if ( unregisterNodeCommand.hasSucceeded() ) {
            po.addLog( "Node unregistered successfully\nUninstalling Accumulo...." );

            Command uninstallCommand = Commands.getUninstallCommand( Sets.newHashSet( agent ) );
            accumuloManager.getCommandRunner().runCommand( uninstallCommand );

            if ( uninstallCommand.hasSucceeded() ) {
                po.addLog( "Accumulo uninstallation succeeded" );
            }
            else {
                po.addLog( String.format( "Accumulo uninstallation failed, %s, skipping...",
                        uninstallCommand.getAllErrors() ) );
            }

            po.addLog( "Restarting cluster..." );
            Command restartClusterCommand = Commands.getRestartCommand( accumuloClusterConfig.getMasterNode() );
            accumuloManager.getCommandRunner().runCommand( restartClusterCommand );
            if ( restartClusterCommand.hasSucceeded() ) {
                po.addLog( "Cluster restarted successfully" );
            }
            else {
                po.addLog( String.format( "Cluster restart failed, %s, skipping...",
                        restartClusterCommand.getAllErrors() ) );
            }

            po.addLog( "Updating database..." );

            try {
                accumuloManager.getPluginDAO()
                               .saveInfo( AccumuloClusterConfig.PRODUCT_KEY, accumuloClusterConfig.getClusterName(),
                                       accumuloClusterConfig );
                po.addLog( "Database information updated" );
            }
            catch ( DBException e ) {
                throw new ClusterConfigurationException(
                        String.format( "Failed to update database information, %s", e.getMessage() ) );
            }
        }
        else {
            throw new ClusterConfigurationException(
                    String.format( "Unregistering node failed, %s", unregisterNodeCommand.getAllErrors() ) );
        }
    }


    public void destroyCluster( AccumuloClusterConfig accumuloClusterConfig ) throws ClusterConfigurationException {

        po.addLog( "Uninstalling cluster..." );

        Command uninstallCommand = Commands.getUninstallCommand( accumuloClusterConfig.getAllNodes() );
        accumuloManager.getCommandRunner().runCommand( uninstallCommand );

        if ( uninstallCommand.hasCompleted() ) {
            if ( uninstallCommand.hasSucceeded() ) {
                po.addLog( "Cluster successfully uninstalled" );
            }
            else {
                po.addLog( String.format( "Uninstallation failed, %s, skipping...", uninstallCommand.getAllErrors() ) );
            }
        }

        po.addLog( "Removing Accumulo from HDFS..." );

        Command removeAccumuloFromHDFSCommand =
                Commands.getRemoveAccumuloFromHFDSCommand( accumuloClusterConfig.getMasterNode() );
        accumuloManager.getCommandRunner().runCommand( removeAccumuloFromHDFSCommand );

        if ( removeAccumuloFromHDFSCommand.hasSucceeded() ) {
            po.addLog( "Accumulo successfully removed from HDFS" );
        }
        else {
            po.addLog( String.format( "Removing Accumulo from HDFS failed, %s, skipping...",
                    removeAccumuloFromHDFSCommand.getAllErrors() ) );
        }

        po.addLog( "Updating database..." );
        try {
            accumuloManager.getPluginDAO()
                           .deleteInfo( AccumuloClusterConfig.PRODUCT_KEY, accumuloClusterConfig.getClusterName() );

            po.addLog( "Database information updated" );
        }
        catch ( DBException e ) {
            throw new ClusterConfigurationException(
                    String.format( "Failed to update database information, %s", e.getMessage() ) );
        }
    }
}
