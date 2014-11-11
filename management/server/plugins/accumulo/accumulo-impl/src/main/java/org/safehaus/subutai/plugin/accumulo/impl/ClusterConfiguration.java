package org.safehaus.subutai.plugin.accumulo.impl;


import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.exception.ClusterConfigurationException;
import org.safehaus.subutai.common.exception.CommandException;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.RequestBuilder;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.accumulo.api.AccumuloClusterConfig;
import org.safehaus.subutai.plugin.accumulo.api.NodeType;
import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;

import com.google.common.base.Preconditions;


/**
 * Configures Accumulo Cluster
 */
public class ClusterConfiguration
{

    private TrackerOperation po;
    private AccumuloImpl accumuloManager;


    public ClusterConfiguration( final AccumuloImpl accumuloManager, final TrackerOperation po )
    {
        Preconditions.checkNotNull( accumuloManager, "Accumulo Manager is null" );
        Preconditions.checkNotNull( po, "Product Operation is null" );
        this.po = po;
        this.accumuloManager = accumuloManager;
    }


    public void configureCluster( Environment environment, AccumuloClusterConfig accumuloClusterConfig,
                                  ZookeeperClusterConfig zookeeperClusterConfig ) throws ClusterConfigurationException
    {

        po.addLog( "Configuring cluster..." );
        ContainerHost master = getHost( environment, accumuloClusterConfig.getMasterNode() );
        ContainerHost gc = getHost( environment, accumuloClusterConfig.getGcNode() );
        ContainerHost monitor = getHost( environment, accumuloClusterConfig.getMonitor() );


        // configure cluster
        for ( UUID uuid : accumuloClusterConfig.getAllNodes() ){
            ContainerHost host = getHost( environment, uuid );
            executeCommand( host, Commands.getAddMasterCommand( host.getHostname() ) );
            executeCommand( host, Commands.getAddGCCommand( host.getHostname() ) );
            executeCommand( host, Commands.getAddMonitorCommand( host.getHostname() ) );
            executeCommand( host, Commands.getAddTracersCommand( serializeSlaveNodeNames( environment, accumuloClusterConfig.getSlaves() ) ) );
            executeCommand( host, Commands.getAddSlavesCommand( serializeSlaveNodeNames( environment, accumuloClusterConfig.getSlaves() ) ) );

//            executeCommand( host, Commands.getBindZKClusterCommand( serializeZKNodeNames( environment, zookeeperClusterConfig.getNodes() )  ) );

            // configure zookeeper

            // init accumulo instance
            executeCommand( master, Commands.getInitCommand( accumuloClusterConfig.getInstanceName(), accumuloClusterConfig.getPassword() ) );
        }

        // configure gc


        // configure monitor



        // configure tracers


        // configure slaves


        // start cluster

//        Command setMasterCommand = accumuloManager.getCommands()
//                                                  .getAddMasterCommand( accumuloClusterConfig.getAllNodes(),
//                                                          accumuloClusterConfig.getMasterNode() );
//        accumuloManager.getCommandRunner().runCommand( setMasterCommand );
//
//        if ( setMasterCommand.hasSucceeded() )
//        {
//            po.addLog( "Setting master node succeeded\nSetting GC node..." );
//            Command setGCNodeCommand = accumuloManager.getCommands()
//                                                      .getAddGCCommand( accumuloClusterConfig.getAllNodes(),
//                                                              accumuloClusterConfig.getGcNode() );
//            accumuloManager.getCommandRunner().runCommand( setGCNodeCommand );
//            if ( setGCNodeCommand.hasSucceeded() )
//            {
//                po.addLog( "Setting GC node succeeded\nSetting monitor node..." );
//
//                Command setMonitorCommand = accumuloManager.getCommands()
//                                                           .getAddMonitorCommand( accumuloClusterConfig.getAllNodes(),
//                                                                   accumuloClusterConfig.getMonitor() );
//                accumuloManager.getCommandRunner().runCommand( setMonitorCommand );
//
//                if ( setMonitorCommand.hasSucceeded() )
//                {
//                    po.addLog( "Setting monitor node succeeded\nSetting tracers..." );
//
//                    Command setTracersCommand = accumuloManager.getCommands().getAddTracersCommand(
//                            accumuloClusterConfig.getAllNodes(), accumuloClusterConfig.getTracers() );
//                    accumuloManager.getCommandRunner().runCommand( setTracersCommand );
//
//                    if ( setTracersCommand.hasSucceeded() )
//                    {
//                        po.addLog( "Setting tracers succeeded\nSetting slaves..." );
//
//                        Command setSlavesCommand = accumuloManager.getCommands().getAddSlavesCommand(
//                                accumuloClusterConfig.getAllNodes(), accumuloClusterConfig.getSlaves() );
//                        accumuloManager.getCommandRunner().runCommand( setSlavesCommand );
//
//                        if ( setSlavesCommand.hasSucceeded() )
//                        {
//                            po.addLog( "Setting slaves succeeded\nSetting ZK cluster..." );
//
//                            Command setZkClusterCommand = accumuloManager.getCommands().getBindZKClusterCommand(
//                                    accumuloClusterConfig.getAllNodes(), zookeeperClusterConfig.getNodes() );
//                            accumuloManager.getCommandRunner().runCommand( setZkClusterCommand );
//
//                            if ( setZkClusterCommand.hasSucceeded() )
//                            {
//                                po.addLog( "Setting ZK cluster succeeded\nInitializing cluster with HDFS..." );
//
//                                Command initCommand = accumuloManager.getCommands().getInitCommand(
//                                        accumuloClusterConfig.getInstanceName(), accumuloClusterConfig.getPassword(),
//                                        accumuloClusterConfig.getMasterNode() );
//                                accumuloManager.getCommandRunner().runCommand( initCommand );
//
//                                if ( initCommand.hasSucceeded() )
//                                {
//                                    po.addLog( "Initialization succeeded\nStarting cluster..." );
//
//                                    Command startClusterCommand = accumuloManager.getCommands().getStartCommand(
//                                            accumuloClusterConfig.getMasterNode() );
//                                    accumuloManager.getCommandRunner().runCommand( startClusterCommand );
//
//                                    if ( startClusterCommand.hasSucceeded() )
//                                    {
//                                        po.addLog( "Cluster started successfully" );
//                                    }
//                                    else
//                                    {
//                                        po.addLog( String.format( "Starting cluster failed, %s, skipping...",
//                                                startClusterCommand.getAllErrors() ) );
//                                    }
//
//                                    po.addLog( "Updating information in database..." );
//                                    accumuloManager.getPluginDAO().saveInfo( AccumuloClusterConfig.PRODUCT_KEY,
//                                            accumuloClusterConfig.getClusterName(), accumuloClusterConfig );
//
//                                    po.addLog( "Updated information in database" );
//                                }
//                                else
//                                {
//                                    throw new ClusterConfigurationException(
//                                            String.format( "Initialization failed, %s", initCommand.getAllErrors() ) );
//                                }
//                            }
//                            else
//                            {
//                                throw new ClusterConfigurationException( String.format( "Setting ZK cluster failed, %s",
//                                        setZkClusterCommand.getAllErrors() ) );
//                            }
//                        }
//                        else
//                        {
//                            throw new ClusterConfigurationException(
//                                    String.format( "Setting slaves failed, %s", setSlavesCommand.getAllErrors() ) );
//                        }
//                    }
//                    else
//                    {
//                        throw new ClusterConfigurationException(
//                                String.format( "Setting tracers failed, %s", setTracersCommand.getAllErrors() ) );
//                    }
//                }
//                else
//                {
//                    throw new ClusterConfigurationException(
//                            String.format( "Setting monitor failed, %s", setMonitorCommand.getAllErrors() ) );
//                }
//            }
//            else
//            {
//                throw new ClusterConfigurationException(
//                        String.format( "Setting gc node failed, %s", setGCNodeCommand.getAllErrors() ) );
//            }
//        }
//        else
//        {
//            throw new ClusterConfigurationException(
//                    String.format( "Setting master node failed, %s", setMasterCommand.getAllErrors() ) );
//        }
    }

    private String serializeSlaveNodeNames( Environment environment, Set<UUID> slaveNodes )
    {
        StringBuilder slavesSpaceSeparated = new StringBuilder();
        for ( UUID tracer : slaveNodes )
        {
            slavesSpaceSeparated.append( getHost( environment, tracer ).getHostname() ).append( " " );
        }
        return slavesSpaceSeparated.toString();
    }

    private String serializeZKNodeNames( Environment environment, Set<UUID> zkNodes ){
        StringBuilder zkNodesCommaSeparated = new StringBuilder();
        for ( UUID zkNode : zkNodes )
        {
            zkNodesCommaSeparated.append( getHost( environment, zkNode ).getHostname() ).append( ":2181," );
        }
        zkNodesCommaSeparated.delete( zkNodesCommaSeparated.length() - 1, zkNodesCommaSeparated.length() );
        return zkNodesCommaSeparated.toString();
    }

    private void executeCommand( ContainerHost host, String commnad ){
        try
        {
            host.execute( new RequestBuilder( commnad ) );
        }
        catch ( CommandException e )
        {
            e.printStackTrace();
        }
    }


    private ContainerHost getHost( Environment environment, UUID uuid ){
        return environment.getContainerHostByUUID( uuid );
    }

    public void addNode( AccumuloClusterConfig accumuloClusterConfig, ZookeeperClusterConfig zookeeperClusterConfig,
                         Agent agent, NodeType nodeType, boolean install ) throws ClusterConfigurationException
    {

//
//        if ( install )
//        {
//            po.addLog( String.format( "Installing %s on %s node...", AccumuloClusterConfig.PRODUCT_NAME,
//                    agent.getHostname() ) );
//
//            Command installCommand = accumuloManager.getCommands().getInstallCommand( Sets.newHashSet( agent ) );
//            accumuloManager.getCommandRunner().runCommand( installCommand );
//
//            if ( installCommand.hasSucceeded() )
//            {
//                po.addLog( "Installation succeeded" );
//            }
//            else
//            {
//                throw new ClusterConfigurationException(
//                        String.format( "Installation failed, %s", installCommand.getAllErrors() ) );
//            }
//        }
//
//        po.addLog( "Registering node with cluster..." );
//
//        Command addNodeCommand;
//        if ( nodeType.isSlave() )
//        {
//            accumuloClusterConfig.getSlaves().add( agent );
//            addNodeCommand = accumuloManager.getCommands().getAddSlavesCommand( accumuloClusterConfig.getAllNodes(),
//                    accumuloClusterConfig.getSlaves() );
//        }
//        else
//        {
//            accumuloClusterConfig.getTracers().add( agent );
//            addNodeCommand = accumuloManager.getCommands().getAddTracersCommand( accumuloClusterConfig.getAllNodes(),
//                    accumuloClusterConfig.getTracers() );
//        }
//        accumuloManager.getCommandRunner().runCommand( addNodeCommand );

//        if ( addNodeCommand.hasSucceeded() )
//        {
//            po.addLog( "Node registration succeeded\nSetting master node..." );
//
//            Command setMasterNodeCommand = accumuloManager.getCommands().getAddMasterCommand( Sets.newHashSet( agent ),
//                    accumuloClusterConfig.getMasterNode() );
//            accumuloManager.getCommandRunner().runCommand( setMasterNodeCommand );
//
//            if ( setMasterNodeCommand.hasSucceeded() )
//            {
//
//                po.addLog( "Setting master node succeeded\nSetting GC node..." );
//
//                Command setGcNodeCommand = accumuloManager.getCommands().getAddGCCommand( Sets.newHashSet( agent ),
//                        accumuloClusterConfig.getGcNode() );
//                accumuloManager.getCommandRunner().runCommand( setGcNodeCommand );
//
//                if ( setGcNodeCommand.hasSucceeded() )
//                {
//
//                    po.addLog( "Setting GC node succeeded\nSetting monitor node..." );
//
//                    Command setMonitorCommand = accumuloManager.getCommands()
//                                                               .getAddMonitorCommand( Sets.newHashSet( agent ),
//                                                                       accumuloClusterConfig.getMonitor() );
//                    accumuloManager.getCommandRunner().runCommand( setMonitorCommand );
//
//                    if ( setMonitorCommand.hasSucceeded() )
//                    {
//
//                        po.addLog( "Setting monitor node succeeded\nSetting tracers/slaves..." );
//
//                        Command setTracersSlavesCommand = nodeType.isSlave() ? accumuloManager.getCommands()
//                                                                                              .getAddTracersCommand(
//                                                                                                      Sets.newHashSet(
//                                                                                                              agent ),
//                                                                                                      accumuloClusterConfig
//                                                                                                              .getTracers() ) :
//                                                          accumuloManager.getCommands()
//                                                                         .getAddSlavesCommand( Sets.newHashSet( agent ),
//                                                                                 accumuloClusterConfig.getSlaves() );
//
//                        accumuloManager.getCommandRunner().runCommand( setTracersSlavesCommand );
//
//                        if ( setTracersSlavesCommand.hasSucceeded() )
//                        {
//
//                            po.addLog( "Setting tracers/slaves succeeded\nSetting Zk cluster..." );
//
//                            Command setZkClusterCommand = accumuloManager.getCommands().getBindZKClusterCommand(
//                                    Sets.newHashSet( agent ), zookeeperClusterConfig.getNodes() );
//                            accumuloManager.getCommandRunner().runCommand( setZkClusterCommand );
//
//                            if ( setZkClusterCommand.hasSucceeded() )
//                            {
//                                po.addLog( "Setting ZK cluster succeeded\nRestarting cluster..." );
//
//                                Command restartClusterCommand = accumuloManager.getCommands().getRestartCommand(
//                                        accumuloClusterConfig.getMasterNode() );
//                                accumuloManager.getCommandRunner().runCommand( restartClusterCommand );
//
//                                if ( restartClusterCommand.hasSucceeded() )
//                                {
//                                    po.addLog( "Cluster restarted successfully" );
//                                }
//                                else
//                                {
//                                    po.addLog( String.format( "Cluster restart failed, %s, skipping...",
//                                            restartClusterCommand.getAllErrors() ) );
//                                }
//
//                                po.addLog( "Updating information in database..." );
//                                accumuloManager.getPluginDAO().saveInfo( AccumuloClusterConfig.PRODUCT_KEY,
//                                        accumuloClusterConfig.getClusterName(), accumuloClusterConfig );
//
//                                po.addLog( "Updated information in database" );
//                            }
//                            else
//                            {
//                                throw new ClusterConfigurationException( String.format( "Setting ZK cluster failed, %s",
//                                        setZkClusterCommand.getAllErrors() ) );
//                            }
//                        }
//                        else
//                        {
//                            throw new ClusterConfigurationException( String.format( "Setting tracers/slaves failed, %s",
//                                    setTracersSlavesCommand.getAllErrors() ) );
//                        }
//                    }
//                    else
//                    {
//                        throw new ClusterConfigurationException(
//                                String.format( "Setting monitor node failed, %s", setMonitorCommand.getAllErrors() ) );
//                    }
//                }
//                else
//                {
//                    throw new ClusterConfigurationException(
//                            String.format( "Setting GC node failed, %s", setGcNodeCommand.getAllErrors() ) );
//                }
//            }
//            else
//            {
//                throw new ClusterConfigurationException(
//                        String.format( "Setting master node failed, %s", setMasterNodeCommand.getAllErrors() ) );
//            }
//        }
//        else
//        {
//            throw new ClusterConfigurationException(
//                    String.format( "Node registration failed, %s", addNodeCommand.getAllErrors() ) );
//        }
    }


    public void removeNode( AccumuloClusterConfig accumuloClusterConfig, Agent agent, NodeType nodeType )
            throws ClusterConfigurationException
    {
        //        Command unregisterNodeCommand;
        //        if ( nodeType == NodeType.Tracer )
        //        {
        //            unregisterNodeCommand =
        //                    accumuloManager.getCommands().getClearTracerCommand( accumuloClusterConfig.getAllNodes(), agent );

        //            accumuloClusterConfig.getTracers().remove( agent );
        //        }
        //        else
        //        {
        //            unregisterNodeCommand =
        //                    accumuloManager.getCommands().getClearSlaveCommand( accumuloClusterConfig.getAllNodes(), agent );
        //            accumuloClusterConfig.getSlaves().remove( agent );
        //        }
        //
        //        po.addLog( "Unregistering node from cluster..." );
        //        accumuloManager.getCommandRunner().runCommand( unregisterNodeCommand );
        //
        //        if ( unregisterNodeCommand.hasSucceeded() )
        //        {
        //            po.addLog( "Node unregistered successfully\nUninstalling Accumulo...." );
        //
        //            Command uninstallCommand = accumuloManager.getCommands().getUninstallCommand( Sets.newHashSet( agent ) );
        //            accumuloManager.getCommandRunner().runCommand( uninstallCommand );
        //
        //            if ( uninstallCommand.hasSucceeded() )
        //            {
        //                po.addLog( "Accumulo uninstallation succeeded" );
        //            }
        //            else
        //            {
        //                po.addLog( String.format( "Accumulo uninstallation failed, %s, skipping...",
        //                        uninstallCommand.getAllErrors() ) );
        //            }
        //
        //            po.addLog( "Restarting cluster..." );
        //            Command restartClusterCommand =
        //                    accumuloManager.getCommands().getRestartCommand( accumuloClusterConfig.getMasterNode() );
        //            accumuloManager.getCommandRunner().runCommand( restartClusterCommand );
        //            if ( restartClusterCommand.hasSucceeded() )
        //            {
        //                po.addLog( "Cluster restarted successfully" );
        //            }
        //            else
        //            {
        //                po.addLog( String.format( "Cluster restart failed, %s, skipping...",
        //                        restartClusterCommand.getAllErrors() ) );
        //            }
        //
        //            po.addLog( "Updating database..." );
        //
        //            accumuloManager.getPluginDAO()
        //                           .saveInfo( AccumuloClusterConfig.PRODUCT_KEY, accumuloClusterConfig.getClusterName(),
        //                                   accumuloClusterConfig );
        //            po.addLog( "Database information updated" );
        //        }
        //        else
        //        {
        //            throw new ClusterConfigurationException(
        //                    String.format( "Unregistering node failed, %s", unregisterNodeCommand.getAllErrors() ) );
        //        }
        //    }


        //    public void destroyCluster( AccumuloClusterConfig accumuloClusterConfig ) throws ClusterConfigurationException
        //    {

        //        po.addLog( "Uninstalling cluster..." );
        //
        //        Command uninstallCommand =
        //                accumuloManager.getCommands().getUninstallCommand( accumuloClusterConfig.getAllNodes() );
        //        accumuloManager.getCommandRunner().runCommand( uninstallCommand );
        //
        //        if ( uninstallCommand.hasCompleted() )
        //        {
        //            if ( uninstallCommand.hasSucceeded() )
        //            {
        //                po.addLog( "Cluster successfully uninstalled" );
        //            }
        //            else
        //            {
        //                po.addLog( String.format( "Uninstallation failed, %s, skipping...", uninstallCommand.getAllErrors() ) );
        //            }
        //        }
        //
        //        po.addLog( "Removing Accumulo from HDFS..." );
        //
        //        Command removeAccumuloFromHDFSCommand =
        //                accumuloManager.getCommands().getRemoveAccumuloFromHFDSCommand( accumuloClusterConfig.getMasterNode() );
        //        accumuloManager.getCommandRunner().runCommand( removeAccumuloFromHDFSCommand );
        //
        //        if ( removeAccumuloFromHDFSCommand.hasSucceeded() )
        //        {
        //            po.addLog( "Accumulo successfully removed from HDFS" );
        //        }
        //        else
        //        {
        //            po.addLog( String.format( "Removing Accumulo from HDFS failed, %s, skipping...",
        //                    removeAccumuloFromHDFSCommand.getAllErrors() ) );
        //        }
        //
        //        po.addLog( "Updating database..." );
        //        accumuloManager.getPluginDAO()
        //                       .deleteInfo( AccumuloClusterConfig.PRODUCT_KEY, accumuloClusterConfig.getClusterName
        // () );
        //
        //        po.addLog( "Database information updated" );
        //    }
    }
}
