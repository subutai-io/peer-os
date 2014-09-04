package org.safehaus.subutai.plugin.hadoop.impl.handler;


import java.util.Iterator;
import java.util.UUID;

import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.hadoop.impl.HadoopImpl;

import com.google.common.base.Strings;


public class InstallOperationHandler extends AbstractOperationHandler<HadoopImpl> {
    private final ProductOperation productOperation;
    private final HadoopClusterConfig config;


    public InstallOperationHandler( HadoopImpl manager, HadoopClusterConfig config ) {
        super( manager, config.getClusterName() );
        this.config = config;
        productOperation = manager.getTracker().createProductOperation( HadoopClusterConfig.PRODUCT_KEY,
                String.format( "Installing %s", HadoopClusterConfig.PRODUCT_KEY ) );
    }


    @Override
    public UUID getTrackerId() {
        return productOperation.getId();
    }


    @Override
    public void run() {
        if ( Strings.isNullOrEmpty( config.getClusterName() ) ||
                config.getCountOfSlaveNodes() == null ||
                config.getCountOfSlaveNodes() <= 0 ) {
            productOperation.addLogFailed( "Malformed configuration\nInstallation aborted" );
            return;
        }

        if ( manager.getCluster( config.getClusterName() ) != null ) {
            productOperation.addLogFailed( String.format( "Cluster with name '%s' already exists\nInstallation aborted",
                    config.getClusterName() ) );
            return;
        }

        //check if node agent is connected
        for ( Iterator<Agent> it = config.getAllNodes().iterator(); it.hasNext(); ) {
            Agent node = it.next();
            if ( manager.getAgentManager().getAgentByHostname( node.getHostname() ) == null ) {
                productOperation.addLog(
                        String.format( "Node %s is not connected. Omitting this node from installation",
                                node.getHostname() ) );
                it.remove();
            }
        }

        if ( config.getAllSlaveNodes().isEmpty() ) {
            productOperation.addLogFailed( "No nodes eligible for installation\nInstallation aborted" );
            return;
        }

        setup();
    }


    private void setup() {

        try {
            ClusterSetupStrategy setupStrategy = manager.getClusterSetupStrategy( productOperation, config );
            setupStrategy.setup();

            productOperation.addLogDone( String.format( "Cluster %s set up successfully", clusterName ) );
        }
        catch ( ClusterSetupException e ) {
            productOperation.addLogFailed(
                    String.format( "Failed to setup Hadoop cluster %s : %s", clusterName, e.getMessage() ) );
        }
    }
}
