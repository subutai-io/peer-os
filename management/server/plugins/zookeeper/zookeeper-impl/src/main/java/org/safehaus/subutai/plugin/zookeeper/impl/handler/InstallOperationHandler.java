package org.safehaus.subutai.plugin.zookeeper.impl.handler;


import java.util.UUID;

import org.safehaus.subutai.core.environment.api.exception.EnvironmentBuildException;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.common.util.CollectionUtil;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.zookeeper.api.SetupType;
import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;
import org.safehaus.subutai.plugin.zookeeper.impl.ZookeeperImpl;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;

import com.google.common.base.Strings;


/**
 * Sets up Zookeeper cluster either a standalone ZK cluster or over hadoop cluster nodes or together with hadoop
 */
public class InstallOperationHandler extends AbstractOperationHandler<ZookeeperImpl> {
    private final ProductOperation po;
    private final ZookeeperClusterConfig config;
    private HadoopClusterConfig hadoopClusterConfig;


    public InstallOperationHandler( ZookeeperImpl manager, ZookeeperClusterConfig config ) {

        super( manager, config.getClusterName() );
        this.config = config;
        po = manager.getTracker().createProductOperation( ZookeeperClusterConfig.PRODUCT_KEY,
                String.format( "Setting up %s cluster...", config.getClusterName() ) );
    }


    public InstallOperationHandler( final ZookeeperImpl manager, ZookeeperClusterConfig config,
                                    final HadoopClusterConfig hadoopClusterConfig ) {
        this( manager, config );
        this.hadoopClusterConfig = hadoopClusterConfig;
    }


    @Override
    public UUID getTrackerId() {
        return po.getId();
    }


    @Override
    public void run() {
        if ( Strings.isNullOrEmpty( config.getClusterName() )
                //either number of nodes to create or hadoop cluster nodes must be present
                || ( config.getSetupType() == SetupType.STANDALONE && config.getNumberOfNodes() <= 0 ) || (
                config.getSetupType() == SetupType.OVER_HADOOP && CollectionUtil.isCollectionEmpty( config.getNodes() )
                        && Strings.isNullOrEmpty( config.getHadoopClusterName() ) ) ||
                ( config.getSetupType() == SetupType.WITH_HADOOP && hadoopClusterConfig == null ) ) {
            po.addLogFailed( "Malformed configuration" );
            return;
        }

        if ( manager.getCluster( clusterName ) != null ) {
            po.addLogFailed( String.format( "Cluster with name '%s' already exists", clusterName ) );
            return;
        }

        if ( config.getSetupType() == SetupType.STANDALONE ) {
            setupStandalone();
        }
        else if ( config.getSetupType() == SetupType.OVER_HADOOP ) {
            setupOverHadoop();
        }
        else if ( config.getSetupType() == SetupType.WITH_HADOOP ) {
            setupWithHadoop();
        }
    }


    /**
     * Sets up ZK cluster over supplied Hadoop cluster nodes
     */
    private void setupOverHadoop() {

        try {
            //setup ZK cluster
            ClusterSetupStrategy zkClusterSetupStrategy = manager.getClusterSetupStrategy( null, config, po );
            zkClusterSetupStrategy.setup();

            po.addLogDone( String.format( "Cluster %s set up successfully", clusterName ) );
        }
        catch ( ClusterSetupException e ) {
            po.addLogFailed(
                    String.format( "Failed to setup an over-Hadoop ZK cluster %s : %s", clusterName, e.getMessage() ) );
        }
    }


    /**
     * Sets up a standalone Zk cluster
     */
    private void setupStandalone() {

        try {
            //create environment
            Environment env = manager.getEnvironmentManager()
                                     .buildEnvironmentAndReturn( manager.getDefaultEnvironmentBlueprint( config ) );

            //setup ZK cluster
            ClusterSetupStrategy zkClusterSetupStrategy = manager.getClusterSetupStrategy( env, config, po );
            zkClusterSetupStrategy.setup();

            po.addLogDone( String.format( "Cluster %s set up successfully", clusterName ) );
        }
        catch ( EnvironmentBuildException | ClusterSetupException e ) {
            po.addLogFailed(
                    String.format( "Failed to setup a standalone ZK cluster %s : %s", clusterName, e.getMessage() ) );
        }
    }


    /**
     * Sets up ZK cluster together with Hadoop cluster
     */
    private void setupWithHadoop() {

        try {

            final String COMBO_TEMPLATE_NAME = "hadoopnzk";
            hadoopClusterConfig.setTemplateName( COMBO_TEMPLATE_NAME );
            //create environment
            Environment env = manager.getEnvironmentManager().buildEnvironmentAndReturn(
                    manager.getHadoopManager().getDefaultEnvironmentBlueprint( hadoopClusterConfig ) );

            //setup Hadoop cluster
            ClusterSetupStrategy hadoopClusterSetupStrategy =
                    manager.getHadoopManager().getClusterSetupStrategy( po, hadoopClusterConfig, env );
            hadoopClusterSetupStrategy.setup();

            //setup ZK cluster
            ClusterSetupStrategy zkClusterSetupStrategy = manager.getClusterSetupStrategy( env, config, po );
            zkClusterSetupStrategy.setup();

            po.addLogDone( String.format( "Cluster %s set up successfully", clusterName ) );
        }
        catch ( EnvironmentBuildException | ClusterSetupException e ) {
            po.addLogFailed(
                    String.format( "Failed to setup a standalone ZK cluster %s : %s", clusterName, e.getMessage() ) );
        }
    }
}
