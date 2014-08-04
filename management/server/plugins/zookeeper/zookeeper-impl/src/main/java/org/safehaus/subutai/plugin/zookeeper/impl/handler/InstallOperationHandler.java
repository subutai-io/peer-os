package org.safehaus.subutai.plugin.zookeeper.impl.handler;


import java.util.UUID;

import org.safehaus.subutai.plugin.zookeeper.api.SetupType;
import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;
import org.safehaus.subutai.plugin.zookeeper.impl.ZookeeperImpl;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.ClusterSetupException;
import org.safehaus.subutai.shared.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.shared.protocol.Util;

import com.google.common.base.Strings;


/**
 * Installs Zookeeper cluster either on newly created lxcs or over hadoop cluster nodes
 */
public class InstallOperationHandler extends AbstractOperationHandler<ZookeeperImpl> {
    private final ProductOperation po;
    private final ZookeeperClusterConfig config;


    public InstallOperationHandler( ZookeeperImpl manager, ZookeeperClusterConfig config ) {
        super( manager, config.getClusterName() );
        this.config = config;
        po = manager.getTracker().createProductOperation( ZookeeperClusterConfig.PRODUCT_KEY,
                String.format( "Installing %s", ZookeeperClusterConfig.PRODUCT_KEY ) );
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
                config.getSetupType() == SetupType.OVER_HADOOP && Util.isCollectionEmpty( config.getNodes() ) ) ) {
            po.addLogFailed( "Malformed configuration\nInstallation aborted" );
            return;
        }

        if ( manager.getCluster( config.getClusterName() ) != null ) {
            po.addLogFailed( String.format( "Cluster with name '%s' already exists\nInstallation aborted",
                    config.getClusterName() ) );
            return;
        }

        if ( config.getSetupType() == SetupType.STANDALONE ) {
            installStandalone();
        }
        else if ( config.getSetupType() == SetupType.OVER_HADOOP ) {
            installOverHadoop();
        }
        else if ( config.getSetupType() == SetupType.WITH_HADOOP ) {
            installWithHadoop();
        }
    }


    /**
     * installs ZK cluster over supplied Hadoop cluster nodes
     */
    private void installOverHadoop() {
        ClusterSetupStrategy clusterSetupStrategy = manager.getClusterSetupStrategy( config, po );

        try {
            ZookeeperClusterConfig finalConfig = ( ZookeeperClusterConfig ) clusterSetupStrategy.setup();

            if ( manager.getDbManager()
                        .saveInfo( ZookeeperClusterConfig.PRODUCT_KEY, config.getClusterName(), finalConfig ) ) {
                po.addLogDone( String.format( "Cluster %s setup successfully", clusterName ) );
            }
            else {
                po.addLogFailed( "Failed to save cluster information to database" );
            }
        }
        catch ( ClusterSetupException e ) {
            po.addLogFailed(
                    String.format( "Failed to setup over-Hadoop ZK cluster %s : %s", clusterName, e.getMessage() ) );
        }
    }


    /**
     * Installs Zk cluster on a newly created set of lxcs
     */
    private void installStandalone() {

        ClusterSetupStrategy clusterSetupStrategy = manager.getClusterSetupStrategy( config, po );

        try {
            ZookeeperClusterConfig finalConfig = ( ZookeeperClusterConfig ) clusterSetupStrategy.setup();

            if ( manager.getDbManager()
                        .saveInfo( ZookeeperClusterConfig.PRODUCT_KEY, config.getClusterName(), finalConfig ) ) {
                po.addLogDone( String.format( "Cluster %s setup successfully", clusterName ) );
            }
            else {
                po.addLogFailed( "Failed to save cluster information to database" );
            }
        }
        catch ( ClusterSetupException e ) {
            po.addLogFailed(
                    String.format( "Failed to setup standalone ZK cluster %s : %s", clusterName, e.getMessage() ) );
        }
    }


    private void installWithHadoop() {
        //call Hadoop CLusterSetupStrategy and feed HadooClusterConfig
        //obtain hadoop nodes and configure ZK cluster with specified number of nodes

    }
}
