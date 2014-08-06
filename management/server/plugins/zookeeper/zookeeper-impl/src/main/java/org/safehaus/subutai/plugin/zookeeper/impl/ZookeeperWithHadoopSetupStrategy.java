package org.safehaus.subutai.plugin.zookeeper.impl;


import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.safehaus.subutai.api.commandrunner.AgentResult;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.commandrunner.CommandCallback;
import org.safehaus.subutai.api.manager.exception.EnvironmentBuildException;
import org.safehaus.subutai.api.manager.helper.Environment;
import org.safehaus.subutai.api.manager.helper.Node;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.hadoop.api.NodeType;
import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.ClusterConfigurationException;
import org.safehaus.subutai.shared.protocol.ClusterSetupException;
import org.safehaus.subutai.shared.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.shared.protocol.Response;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;


/**
 * ZK cluster setup strategy using combo template ZK+Hadoop
 */
public class ZookeeperWithHadoopSetupStrategy implements ClusterSetupStrategy {

    public static final String COMBO_TEMPLATE_NAME = "hadoopnzk";

    private final HadoopClusterConfig hadoopClusterConfig;
    private final ZookeeperClusterConfig zookeeperClusterConfig;
    private final ProductOperation po;
    private final ZookeeperImpl zookeeperManager;


    public ZookeeperWithHadoopSetupStrategy( final HadoopClusterConfig hadoopClusterConfig,
                                             final ZookeeperClusterConfig zookeeperClusterConfig,
                                             final ProductOperation po, final ZookeeperImpl zookeeperManager ) {
        Preconditions.checkNotNull( zookeeperClusterConfig, "ZK cluster config is null" );
        Preconditions.checkNotNull( hadoopClusterConfig, "Hadoop cluster config is null" );
        Preconditions.checkNotNull( po, "Product operation tracker is null" );
        Preconditions.checkNotNull( zookeeperManager, "ZK manager is null" );

        this.hadoopClusterConfig = hadoopClusterConfig;
        this.zookeeperClusterConfig = zookeeperClusterConfig;
        this.po = po;
        this.zookeeperManager = zookeeperManager;
        this.zookeeperClusterConfig.setTemplateName( COMBO_TEMPLATE_NAME );
    }


    @Override
    public ZookeeperClusterConfig setup() throws ClusterSetupException {
        if ( Strings.isNullOrEmpty( zookeeperClusterConfig.getClusterName() ) ||
                Strings.isNullOrEmpty( zookeeperClusterConfig.getTemplateName() ) ||
                zookeeperClusterConfig.getNumberOfNodes() <= 0 ) {
            po.addLogFailed( "Malformed configuration\nZookeeper installation aborted" );
        }

        if ( zookeeperManager.getCluster( zookeeperClusterConfig.getClusterName() ) != null ) {
            throw new ClusterSetupException(
                    String.format( "Cluster with name '%s' already exists\nInstallation aborted",
                            zookeeperClusterConfig.getClusterName() ) );
        }

        int HADOOP_MASTER_NODES_QUANTITY = HadoopClusterConfig.DEFAULT_HADOOP_MASTER_NODES_QUANTITY;

        int totalHadoopNodesCount = HADOOP_MASTER_NODES_QUANTITY + hadoopClusterConfig.getCountOfSlaveNodes();
        if ( zookeeperClusterConfig.getNumberOfNodes() > totalHadoopNodesCount ) {
            throw new ClusterSetupException( "Number of needed ZK nodes exceeds number of available Hadoop nodes" );
        }

        //setup environment
        po.addLog( "Building environment..." );
        try {
            hadoopClusterConfig.setTemplateName( COMBO_TEMPLATE_NAME );

            //this part should be moved to Hadoop plugin  --START
            Environment env = zookeeperManager.getEnvironmentManager().buildEnvironmentAndReturn(
                    zookeeperManager.getHadoopManager().getDefaultEnvironmentBlueprint( hadoopClusterConfig ) );

            Set<Agent> masterNodes = new HashSet<>();
            Set<Agent> slaveNodes = new HashSet<>();
            for ( Node node : env.getNodes() ) {
                if ( NodeType.MASTER_NODE.name().equalsIgnoreCase( node.getNodeGroupName() ) ) {
                    masterNodes.add( node.getAgent() );
                }
                else if ( NodeType.SLAVE_NODE.name().equalsIgnoreCase( node.getNodeGroupName() ) ) {
                    slaveNodes.add( node.getAgent() );
                }
            }

            if ( masterNodes.size() != HADOOP_MASTER_NODES_QUANTITY ) {
                throw new ClusterSetupException(
                        String.format( "Hadoop master nodes must be %d in count", HADOOP_MASTER_NODES_QUANTITY ) );
            }
            if ( slaveNodes.isEmpty() ) {
                throw new ClusterSetupException( "Hadoop slave nodes are empty" );
            }

            Iterator<Agent> masterIterator = masterNodes.iterator();
            hadoopClusterConfig.setNameNode( masterIterator.next() );
            hadoopClusterConfig.setSecondaryNameNode( masterIterator.next() );
            hadoopClusterConfig.setJobTracker( masterIterator.next() );
            hadoopClusterConfig.setDataNodes( Lists.newArrayList( slaveNodes ) );
            hadoopClusterConfig.setTaskTrackers( Lists.newArrayList( slaveNodes ) );

            //this part should be moved to Hadoop plugin  --END

            if ( totalHadoopNodesCount != hadoopClusterConfig.getAllNodes().size() ) {
                throw new ClusterSetupException(
                        String.format( "Specified %d hadoop nodes, but %d are created", totalHadoopNodesCount,
                                hadoopClusterConfig.getAllNodes().size() ) );
            }

            po.addLog( String.format( "Setting up %s Hadoop cluster", hadoopClusterConfig.getClusterName() ) );

            //setup Hadoop cluster
            ClusterSetupStrategy hadoopSetupStrategy =
                    zookeeperManager.getHadoopManager().getClusterSetupStrategy( po, hadoopClusterConfig );

            hadoopSetupStrategy.setup();

            po.addLog( "Saving Hadoop cluster information to DB..." );
            if ( zookeeperManager.getDbManager()
                                 .saveInfo( HadoopClusterConfig.PRODUCT_KEY, hadoopClusterConfig.getClusterName(),
                                         hadoopClusterConfig ) ) {
                po.addLog( "Hadoop cluster information saved to DB" );
            }
            else {
                throw new ClusterSetupException( "Failed to save Hadoop cluster information to DB" );
            }

            //setup ZK cluster

            po.addLog( String.format( "Setting up %s ZK cluster", zookeeperClusterConfig.getClusterName() ) );

            //pick random nodes from hadoop cluster to setup ZK cluster if they are not set by calling party
            if ( zookeeperClusterConfig.getNodes() == null || zookeeperClusterConfig.getNodes().isEmpty() ) {

                Set<Agent> zkNodes = new HashSet<>();
                Iterator<Agent> hadoopNodesIterator = hadoopClusterConfig.getAllNodes().iterator();
                for ( int i = 0; i < zookeeperClusterConfig.getNumberOfNodes(); i++ ) {
                    zkNodes.add( hadoopNodesIterator.next() );
                }
                zookeeperClusterConfig.setNodes( zkNodes );
            }

            po.addLog( String.format( "Setting up %s ZK cluster", zookeeperClusterConfig.getClusterName() ) );

            Command configureClusterCommand;
            try {
                configureClusterCommand = Commands.getConfigureClusterCommand( zookeeperClusterConfig.getNodes(),
                        ConfigParams.DATA_DIR.getParamValue() + "/" + ConfigParams.MY_ID_FILE.getParamValue(),
                        ZookeeperStandaloneSetupStrategy.prepareConfiguration( zookeeperClusterConfig.getNodes() ),
                        ConfigParams.CONFIG_FILE_PATH.getParamValue() );
            }
            catch ( ClusterConfigurationException e ) {
                throw new ClusterSetupException( String.format( "Error configuring cluster %s", e.getMessage() ) );
            }

            //configure ZK cluster
            zookeeperManager.getCommandRunner().runCommand( configureClusterCommand );

            if ( configureClusterCommand.hasSucceeded() ) {

                po.addLog( String.format( "Cluster configured\nStarting %s...", ZookeeperClusterConfig.PRODUCT_KEY ) );
                //start all nodes
                Command startCommand = Commands.getStartCommand( zookeeperClusterConfig.getNodes() );
                final AtomicInteger count = new AtomicInteger();
                zookeeperManager.getCommandRunner().runCommand( startCommand, new CommandCallback() {
                    @Override
                    public void onResponse( Response response, AgentResult agentResult, Command command ) {
                        if ( agentResult.getStdOut().contains( "STARTED" ) ) {
                            if ( count.incrementAndGet() == zookeeperClusterConfig.getNodes().size() ) {
                                stop();
                            }
                        }
                    }
                } );

                if ( count.get() == zookeeperClusterConfig.getNodes().size() ) {
                    po.addLog( String.format( "Starting %s succeeded\nDone", ZookeeperClusterConfig.PRODUCT_KEY ) );
                }
                else {
                    po.addLog( String.format( "Starting %s failed, %s, skipping...", ZookeeperClusterConfig.PRODUCT_KEY,
                            startCommand.getAllErrors() ) );
                }

                po.addLog( "Saving cluster information to database..." );

                if ( zookeeperManager.getDbManager().saveInfo( ZookeeperClusterConfig.PRODUCT_KEY,
                        zookeeperClusterConfig.getClusterName(), zookeeperClusterConfig ) ) {
                    po.addLog( "Cluster information saved to database" );
                }
                else {
                    throw new ClusterSetupException( "Failed to save cluster information to database. Check logs" );
                }
            }
            else {
                throw new ClusterSetupException( String.format(
                        "Failed to configure cluster, %s\nPlease configure cluster manually and restart it",
                        configureClusterCommand.getAllErrors() ) );
            }
        }
        catch ( EnvironmentBuildException e ) {
            throw new ClusterSetupException( String.format( "Error building environment: %s", e.getMessage() ) );
        }


        return zookeeperClusterConfig;
    }
}
