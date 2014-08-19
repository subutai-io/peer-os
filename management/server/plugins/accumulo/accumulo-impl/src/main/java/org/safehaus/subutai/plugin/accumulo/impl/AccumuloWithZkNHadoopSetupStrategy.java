package org.safehaus.subutai.plugin.accumulo.impl;


import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.dbmanager.DBException;
import org.safehaus.subutai.api.manager.helper.Environment;
import org.safehaus.subutai.api.manager.helper.Node;
import org.safehaus.subutai.plugin.accumulo.api.AccumuloClusterConfig;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.ClusterConfigurationException;
import org.safehaus.subutai.shared.protocol.ClusterSetupException;
import org.safehaus.subutai.shared.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.shared.protocol.settings.Common;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


/**
 * Accumulo cluster setup strategy using environment
 */
public class AccumuloWithZkNHadoopSetupStrategy implements ClusterSetupStrategy {

    private final Environment environment;
    private final AccumuloImpl accumuloManager;
    private final ProductOperation po;
    private final AccumuloClusterConfig accumuloClusterConfig;


    public AccumuloWithZkNHadoopSetupStrategy( final Environment environment,
                                               final AccumuloClusterConfig accumuloClusterConfig,
                                               final ProductOperation po, final AccumuloImpl accumuloManager ) {

        Preconditions.checkNotNull( environment, "Environment is null" );
        Preconditions.checkNotNull( accumuloClusterConfig, "Accumulo cluster config is null" );
        Preconditions.checkNotNull( po, "Product operation tracker is null" );
        Preconditions.checkNotNull( accumuloManager, "Accumulo manager is null" );

        this.environment = environment;
        this.po = po;
        this.accumuloManager = accumuloManager;
        this.accumuloClusterConfig = accumuloClusterConfig;
    }


    @Override
    public AccumuloClusterConfig setup() throws ClusterSetupException {
        if ( Strings.isNullOrEmpty( accumuloClusterConfig.getClusterName() ) ||
                Strings.isNullOrEmpty( accumuloClusterConfig.getInstanceName() ) ||
                Strings.isNullOrEmpty( accumuloClusterConfig.getPassword() ) ) {
            po.addLogFailed( "Malformed configuration" );
        }

        if ( accumuloManager.getCluster( accumuloClusterConfig.getClusterName() ) != null ) {
            throw new ClusterSetupException(
                    String.format( "Cluster with name '%s' already exists", accumuloClusterConfig.getClusterName() ) );
        }

        HadoopClusterConfig hadoopClusterConfig =
                accumuloManager.getHadoopManager().getCluster( accumuloClusterConfig.getHadoopClusterName() );
        if ( hadoopClusterConfig == null ) {
            throw new ClusterSetupException( String.format( "Hadoop cluster with name '%s' not found",
                    accumuloClusterConfig.getHadoopClusterName() ) );
        }

        ZookeeperClusterConfig zookeeperClusterConfig =
                accumuloManager.getZkManager().getCluster( accumuloClusterConfig.getZookeeperClusterName() );
        if ( zookeeperClusterConfig == null ) {
            throw new ClusterSetupException( String.format( "Zookeeper cluster with name '%s' not found",
                    accumuloClusterConfig.getZookeeperClusterName() ) );
        }

        //get ZK nodes with Hadoop installed from environment
        Set<Agent> accumuloAgents = new HashSet<>();
        for ( Node node : environment.getNodes() ) {
            if ( node.getTemplate().getProducts().contains( Common.PACKAGE_PREFIX + AccumuloClusterConfig.PRODUCT_NAME )
                    && node.getTemplate().getProducts()
                           .contains( Common.PACKAGE_PREFIX + HadoopClusterConfig.PRODUCT_NAME ) ) {
                accumuloAgents.add( node.getAgent() );
            }
        }

        int numberOfNeededAccumuloNodes =
                AccumuloClusterConfig.DEFAULT_ACCUMULO_MASTER_NODES_QUANTITY + accumuloClusterConfig
                        .getNumberOfTracers() + accumuloClusterConfig.getNumberOfSlaves();

        if ( numberOfNeededAccumuloNodes > accumuloAgents.size() ) {
            throw new ClusterSetupException( String.format(
                    "Number of needed Accumulo nodes (%d) exceeds number of available nodes with Hadoop installed (%d)",
                    numberOfNeededAccumuloNodes, accumuloAgents.size() ) );
        }

        Set<Agent> accumuloTracerNodes = new HashSet<>();
        Set<Agent> accumuloSlaveNodes = new HashSet<>();

        Iterator<Agent> agentIterator = accumuloAgents.iterator();

        accumuloClusterConfig.setMasterNode( agentIterator.next() );
        accumuloClusterConfig.setGcNode( agentIterator.next() );
        accumuloClusterConfig.setMonitor( agentIterator.next() );
        for ( int i = 0; i < accumuloClusterConfig.getNumberOfTracers(); i++ ) {
            accumuloTracerNodes.add( agentIterator.next() );
        }
        for ( int i = 0; i < accumuloClusterConfig.getNumberOfSlaves(); i++ ) {
            accumuloSlaveNodes.add( agentIterator.next() );
        }

        accumuloClusterConfig.setTracers( accumuloTracerNodes );
        accumuloClusterConfig.setSlaves( accumuloSlaveNodes );

        try {
            configureCluster( accumuloClusterConfig, zookeeperClusterConfig );
        }
        catch ( ClusterConfigurationException e ) {
            throw new ClusterSetupException( e.getMessage() );
        }


        return accumuloClusterConfig;
    }


    private void configureCluster( AccumuloClusterConfig accumuloClusterConfig,
                                   ZookeeperClusterConfig zookeeperClusterConfig )
            throws ClusterConfigurationException {

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
                                        accumuloManager.getDbManager().saveInfo2( AccumuloClusterConfig.PRODUCT_KEY,
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
}
