package org.safehaus.subutai.plugin.spark.impl.handler;


import java.util.Iterator;
import java.util.UUID;

import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.common.util.CollectionUtil;
import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;
import org.safehaus.subutai.plugin.spark.impl.SparkImpl;

import com.google.common.base.Strings;


public class InstallOperationHandler extends AbstractOperationHandler<SparkImpl> {
    private final ProductOperation po;
    private final SparkClusterConfig config;


    public InstallOperationHandler( SparkImpl manager, SparkClusterConfig config ) {
        super( manager, config.getClusterName() );
        this.config = config;
        po = SparkImpl.getTracker().createProductOperation( SparkClusterConfig.PRODUCT_KEY,
                String.format( "Installing %s", SparkClusterConfig.PRODUCT_KEY ) );
    }


    @Override
    public UUID getTrackerId() {
        return po.getId();
    }


    @Override
    public void run() {
        if ( Strings.isNullOrEmpty( config.getClusterName() ) || CollectionUtil
                .isCollectionEmpty( config.getSlaveNodes() ) || config.getMasterNode() == null ) {
            po.addLogFailed( "Malformed configuration\nInstallation aborted" );
            return;
        }

        if ( manager.getCluster( config.getClusterName() ) != null ) {
            po.addLogFailed( String.format( "Cluster with name '%s' already exists\nInstallation aborted",
                    config.getClusterName() ) );
            return;
        }

        if ( SparkImpl.getAgentManager().getAgentByHostname( config.getMasterNode().getHostname() ) == null ) {
            po.addLogFailed( "Master node is not connected\nInstallation aborted" );
            return;
        }

        //check if node agent is connected
        for ( Iterator<Agent> it = config.getSlaveNodes().iterator(); it.hasNext(); ) {
            Agent node = it.next();
            if ( SparkImpl.getAgentManager().getAgentByHostname( node.getHostname() ) == null ) {
                po.addLog( String.format( "Node %s is not connected. Omitting this node from installation",
                        node.getHostname() ) );
                it.remove();
            }
        }

        if ( config.getSlaveNodes().isEmpty() ) {
            po.addLogFailed( "No nodes eligible for installation\nInstallation aborted" );
            return;
        }
        setupSpark();
    }


    private void setupSpark() {
        try {
            ClusterSetupStrategy sparkClusterStrategy = manager.getClusterSetupStrategy( po, config );
            sparkClusterStrategy.setup();
            po.addLogDone( String.format( "Cluster %s set up successfully", clusterName ) );
        }
        catch ( ClusterSetupException e ) {
            po.addLogFailed( String.format( "Failed to setup Spark cluster %s : %s", clusterName, e.getMessage() ) );
        }
    }
}
