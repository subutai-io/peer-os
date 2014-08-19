package org.safehaus.subutai.plugin.accumulo.impl.handler;


import java.util.UUID;

import org.safehaus.subutai.api.manager.exception.EnvironmentBuildException;
import org.safehaus.subutai.api.manager.helper.Environment;
import org.safehaus.subutai.plugin.accumulo.api.AccumuloClusterConfig;
import org.safehaus.subutai.plugin.accumulo.api.SetupType;
import org.safehaus.subutai.plugin.accumulo.impl.AccumuloImpl;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.ClusterSetupException;
import org.safehaus.subutai.shared.protocol.ClusterSetupStrategy;

import com.google.common.base.Strings;


/**
 * Sets up Accumulo cluster either oer existing Hadoop & Zk clusters or with newly created Hadoop & ZK cluters
 */
public class InstallOperationHandler extends AbstractOperationHandler<AccumuloImpl> {
    private final AccumuloClusterConfig config;
    private final ProductOperation po;
    private HadoopClusterConfig hadoopClusterConfig;
    private ZookeeperClusterConfig zookeeperClusterConfig;


    public InstallOperationHandler( AccumuloImpl manager, AccumuloClusterConfig config ) {
        super( manager, config.getClusterName() );
        this.config = config;
        po = manager.getTracker().createProductOperation( AccumuloClusterConfig.PRODUCT_KEY,
                String.format( "Setting up %s cluster...", config.getClusterName() ) );
    }


    public InstallOperationHandler( final AccumuloImpl manager, final AccumuloClusterConfig config,
                                    final HadoopClusterConfig hadoopClusterConfig,
                                    final ZookeeperClusterConfig zookeeperClusterConfig ) {
        this( manager, config );
        this.hadoopClusterConfig = hadoopClusterConfig;
        this.zookeeperClusterConfig = zookeeperClusterConfig;
    }


    @Override
    public UUID getTrackerId() {
        return po.getId();
    }


    @Override
    public void run() {

        if ( Strings.isNullOrEmpty( config.getClusterName() ) ||
                Strings.isNullOrEmpty( config.getZookeeperClusterName() ) ||
                Strings.isNullOrEmpty( config.getHadoopClusterName() ) ||
                Strings.isNullOrEmpty( config.getInstanceName() ) ||
                Strings.isNullOrEmpty( config.getPassword() ) ) {
            po.addLogFailed( "Malformed configuration" );
            return;
        }

        if ( manager.getCluster( clusterName ) != null ) {
            po.addLogFailed( String.format( "Cluster with name '%s' already exists", clusterName ) );
            return;
        }

        if ( config.getSetupType() == SetupType.OVER_HADOOP_N_ZK ) {
            setupOverHadoopNZk();
        }
        else {
            setupWithHadoopNZk();
        }
    }


    private void setupOverHadoopNZk() {
        try {
            ClusterSetupStrategy setupStrategy = manager.getClusterSetupStrategy( null, config, po );
            setupStrategy.setup();

            po.addLogDone( String.format( "Cluster %s set up successfully", clusterName ) );
        }
        catch ( ClusterSetupException e ) {
            po.addLogFailed( String.format( "Failed to setup cluster %s : %s", clusterName, e.getMessage() ) );
        }
    }


    private void setupWithHadoopNZk() {

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
            ClusterSetupStrategy zkClusterSetupStrategy =
                    manager.getZkManager().getClusterSetupStrategy( env, zookeeperClusterConfig, po );
            zkClusterSetupStrategy.setup();

            //setting up Accumulo cluster
            ClusterSetupStrategy accumuloSetupStrategy = manager.getClusterSetupStrategy( env, config, po );
            accumuloSetupStrategy.setup();

            po.addLogDone( String.format( "Cluster %s set up successfully", clusterName ) );
        }
        catch ( EnvironmentBuildException | ClusterSetupException e ) {
            po.addLogFailed( String.format( "Failed to setup cluster %s : %s", clusterName, e.getMessage() ) );
        }
    }
}
