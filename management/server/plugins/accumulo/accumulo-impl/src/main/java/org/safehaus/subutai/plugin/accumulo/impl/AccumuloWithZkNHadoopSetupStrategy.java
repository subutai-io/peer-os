package org.safehaus.subutai.plugin.accumulo.impl;


import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.manager.exception.EnvironmentBuildException;
import org.safehaus.subutai.api.manager.helper.Environment;
import org.safehaus.subutai.api.manager.helper.Node;
import org.safehaus.subutai.plugin.accumulo.api.AccumuloClusterConfig;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.hadoop.api.NodeType;
import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.ClusterSetupException;
import org.safehaus.subutai.shared.protocol.ClusterSetupStrategy;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;


/**
 * Accumulo cluster setup strategy using combo template hadoop+zk+accumulo
 */
public class AccumuloWithZkNHadoopSetupStrategy implements ClusterSetupStrategy {

    public static final String COMBO_TEMPLATE_NAME = "hadoopnzknaccumulo";

    private final AccumuloImpl accumuloManager;
    private final ProductOperation po;
    private final HadoopClusterConfig hadoopClusterConfig;
    private final ZookeeperClusterConfig zookeeperClusterConfig;
    private final AccumuloClusterConfig accumuloClusterConfig;


    public AccumuloWithZkNHadoopSetupStrategy( final ProductOperation po,
                                               final AccumuloClusterConfig accumuloClusterConfig,
                                               final HadoopClusterConfig hadoopClusterConfig,
                                               final ZookeeperClusterConfig zookeeperClusterConfig,
                                               final AccumuloImpl accumuloManager ) {

        Preconditions.checkNotNull( hadoopClusterConfig, "Hadoop cluster config is null" );
        Preconditions.checkNotNull( zookeeperClusterConfig, "ZK cluster config is null" );
        Preconditions.checkNotNull( accumuloClusterConfig, "Accumulo cluster config is null" );
        Preconditions.checkNotNull( po, "Product operation tracker is null" );
        Preconditions.checkNotNull( accumuloManager, "Accumulo manager is null" );

        this.po = po;
        this.accumuloManager = accumuloManager;
        this.accumuloClusterConfig = accumuloClusterConfig;
        this.hadoopClusterConfig = hadoopClusterConfig;
        this.zookeeperClusterConfig = zookeeperClusterConfig;
    }


    @Override
    public AccumuloClusterConfig setup() throws ClusterSetupException {
        if ( accumuloClusterConfig == null ||
                Strings.isNullOrEmpty( accumuloClusterConfig.getClusterName() ) ||
                Strings.isNullOrEmpty( accumuloClusterConfig.getInstanceName() ) ||
                Strings.isNullOrEmpty( accumuloClusterConfig.getPassword() ) ) {
            po.addLogFailed( "Malformed configuration" );
        }

        if ( accumuloManager.getCluster( accumuloClusterConfig.getClusterName() ) != null ) {
            throw new ClusterSetupException(
                    String.format( "Cluster with name '%s' already exists",
                            accumuloClusterConfig.getClusterName() ) );
        }

        int HADOOP_MASTER_NODES_QUANTITY = HadoopClusterConfig.DEFAULT_HADOOP_MASTER_NODES_QUANTITY;

        int totalHadoopNodesCount = HADOOP_MASTER_NODES_QUANTITY + hadoopClusterConfig.getCountOfSlaveNodes();
        if ( AccumuloClusterConfig.DEFAULT_ACCUMULO_MASTER_NODES_QUANTITY + accumuloClusterConfig.getNumberOfTracers()
                + accumuloClusterConfig.getNumberOfSlaves() > totalHadoopNodesCount ) {
            throw new ClusterSetupException(
                    "Number of needed Accumulo nodes exceeds number of available Hadoop nodes" );
        }

        if ( zookeeperClusterConfig.getNumberOfNodes() > totalHadoopNodesCount ) {
            throw new ClusterSetupException( "Number of needed ZK nodes exceeds number of available Hadoop nodes" );
        }


        //setup environment
        po.addLog( "Building environment..." );
        try {
            hadoopClusterConfig.setTemplateName( COMBO_TEMPLATE_NAME );

            Environment env = accumuloManager.getEnvironmentManager().buildEnvironmentAndReturn(
                    accumuloManager.getHadoopManager().getDefaultEnvironmentBlueprint( hadoopClusterConfig ) );

            Set<Agent> hadoopMasterNodes = new HashSet<>();
            Set<Agent> hadoopSlaveNodes = new HashSet<>();
            for ( Node node : env.getNodes() ) {
                if ( NodeType.MASTER_NODE.name().equalsIgnoreCase( node.getNodeGroupName() ) ) {
                    hadoopMasterNodes.add( node.getAgent() );
                }
                else if ( NodeType.SLAVE_NODE.name().equalsIgnoreCase( node.getNodeGroupName() ) ) {
                    hadoopSlaveNodes.add( node.getAgent() );
                }
            }

            if ( hadoopMasterNodes.size() != HADOOP_MASTER_NODES_QUANTITY ) {
                throw new ClusterSetupException(
                        String.format( "Hadoop master nodes must be %d in count", HADOOP_MASTER_NODES_QUANTITY ) );
            }
            if ( hadoopSlaveNodes.isEmpty() ) {
                throw new ClusterSetupException( "Hadoop slave nodes are empty" );
            }

            Iterator<Agent> masterIterator = hadoopMasterNodes.iterator();
            hadoopClusterConfig.setNameNode( masterIterator.next() );
            hadoopClusterConfig.setSecondaryNameNode( masterIterator.next() );
            hadoopClusterConfig.setJobTracker( masterIterator.next() );
            hadoopClusterConfig.setDataNodes( Lists.newArrayList( hadoopSlaveNodes ) );
            hadoopClusterConfig.setTaskTrackers( Lists.newArrayList( hadoopSlaveNodes ) );

            if ( totalHadoopNodesCount != hadoopClusterConfig.getAllNodes().size() ) {
                throw new ClusterSetupException(
                        String.format( "Specified %d hadoop nodes, but %d are created", totalHadoopNodesCount,
                                hadoopClusterConfig.getAllNodes().size() ) );
            }

            po.addLog( String.format( "Setting up %s Hadoop cluster", hadoopClusterConfig.getClusterName() ) );

            //setup Hadoop cluster
            ClusterSetupStrategy hadoopSetupStrategy =
                    accumuloManager.getHadoopManager().getClusterSetupStrategy( po, hadoopClusterConfig );

            hadoopSetupStrategy.setup();

            po.addLog( "Saving Hadoop cluster information to DB..." );
            if ( accumuloManager.getDbManager()
                                .saveInfo( HadoopClusterConfig.PRODUCT_KEY, hadoopClusterConfig.getClusterName(),
                                        hadoopClusterConfig ) ) {
                po.addLog( "Hadoop cluster information saved to DB" );
            }
            else {
                throw new ClusterSetupException( "Failed to save Hadoop cluster information to DB" );
            }

            //setup ZK cluster

            po.addLog( String.format( "Setting up %s ZK cluster", zookeeperClusterConfig.getClusterName() ) );

            Set<Agent> zkNodes = new HashSet<>();
            Iterator<Agent> hadoopNodesIterator = hadoopClusterConfig.getAllNodes().iterator();
            for ( int i = 0; i < zookeeperClusterConfig.getNumberOfNodes(); i++ ) {
                zkNodes.add( hadoopNodesIterator.next() );
            }
            zookeeperClusterConfig.setNodes( zkNodes );

            ClusterSetupStrategy zkSetupStrategy = accumuloManager.getZkManager()
                                                                  .getClusterSetupStrategy( null,
                                                                          zookeeperClusterConfig, po );

            zkSetupStrategy.setup();

            po.addLog( "Saving ZK cluster information to DB..." );
            if ( accumuloManager.getDbManager()
                                .saveInfo( ZookeeperClusterConfig.PRODUCT_KEY, zookeeperClusterConfig.getClusterName(),
                                        zookeeperClusterConfig ) ) {
                po.addLog( "ZK cluster information saved to DB" );
            }
            else {
                throw new ClusterSetupException( "Failed to save ZK cluster information to DB" );
            }


            //setup Accumulo cluster

            po.addLog( String.format( "Setting up %s Accumulo cluster", accumuloClusterConfig.getClusterName() ) );

            Set<Agent> accumuloTracerNodes = new HashSet<>();
            Set<Agent> accumuloSlaveNodes = new HashSet<>();

            Iterator<Agent> hadoopNodesIterator2 = hadoopClusterConfig.getAllNodes().iterator();

            accumuloClusterConfig.setMasterNode( hadoopNodesIterator2.next() );
            accumuloClusterConfig.setGcNode( hadoopNodesIterator2.next() );
            accumuloClusterConfig.setMonitor( hadoopNodesIterator2.next() );
            for ( int i = 0; i < accumuloClusterConfig.getNumberOfTracers(); i++ ) {
                accumuloTracerNodes.add( hadoopNodesIterator2.next() );
            }
            for ( int i = 0; i < accumuloClusterConfig.getNumberOfSlaves(); i++ ) {
                accumuloSlaveNodes.add( hadoopNodesIterator2.next() );
            }

            accumuloClusterConfig.setTracers( accumuloTracerNodes );
            accumuloClusterConfig.setSlaves( accumuloSlaveNodes );

            configureAccumuloCluster();
        }
        catch ( EnvironmentBuildException e ) {
            throw new ClusterSetupException( String.format( "Error building environment: %s", e.getMessage() ) );
        }


        return accumuloClusterConfig;
    }


    private void configureAccumuloCluster() throws ClusterSetupException {
        po.addLog( "Setting master node..." );

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

                                    po.addLog( "Updating db..." );

                                    if ( accumuloManager.getDbManager().saveInfo( AccumuloClusterConfig.PRODUCT_KEY,
                                            accumuloClusterConfig.getClusterName(), accumuloClusterConfig ) ) {
                                        po.addLog( "Cluster info saved to DB" );
                                    }
                                    else {
                                        throw new ClusterSetupException(
                                                "Could not save cluster info to DB! Please see logs" );
                                    }
                                }
                                else {
                                    throw new ClusterSetupException(
                                            String.format( "Initialization failed, %s", initCommand.getAllErrors() ) );
                                }
                            }
                            else {
                                throw new ClusterSetupException( String.format( "Setting ZK cluster failed, %s",
                                        setZkClusterCommand.getAllErrors() ) );
                            }
                        }
                        else {
                            throw new ClusterSetupException(
                                    String.format( "Setting slaves failed, %s", setSlavesCommand.getAllErrors() ) );
                        }
                    }
                    else {
                        throw new ClusterSetupException(
                                String.format( "Setting tracers failed, %s", setTracersCommand.getAllErrors() ) );
                    }
                }
                else {
                    throw new ClusterSetupException(
                            String.format( "Setting monitor failed, %s", setMonitorCommand.getAllErrors() ) );
                }
            }
            else {
                throw new ClusterSetupException(
                        String.format( "Setting gc node failed, %s", setGCNodeCommand.getAllErrors() ) );
            }
        }
        else {
            throw new ClusterSetupException(
                    String.format( "Setting master node failed, %s", setMasterCommand.getAllErrors() ) );
        }
    }
}
