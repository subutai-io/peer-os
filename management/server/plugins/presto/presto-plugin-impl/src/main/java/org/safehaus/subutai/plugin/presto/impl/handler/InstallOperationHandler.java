package org.safehaus.subutai.plugin.presto.impl.handler;


import java.util.Iterator;
import java.util.UUID;

import org.safehaus.subutai.common.CollectionUtil;
import org.safehaus.subutai.plugin.presto.api.PrestoClusterConfig;
import org.safehaus.subutai.plugin.presto.impl.PrestoImpl;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.ClusterSetupException;
import org.safehaus.subutai.shared.protocol.ClusterSetupStrategy;

import com.google.common.base.Strings;


/**
 * Created by dilshat on 5/7/14.
 */
public class InstallOperationHandler extends AbstractOperationHandler<PrestoImpl> {
    private final ProductOperation po;
    private final PrestoClusterConfig config;


    public InstallOperationHandler( PrestoImpl manager, PrestoClusterConfig config ) {

        super( manager, config.getClusterName() );
        this.config = config;
        po = manager.getTracker().createProductOperation( PrestoClusterConfig.PRODUCT_KEY,
                String.format( "Installing %s", PrestoClusterConfig.PRODUCT_KEY ) );
    }


    @Override
    public UUID getTrackerId() {
        return po.getId();
    }


    @Override
    public void run() {
        if ( Strings.isNullOrEmpty( config.getClusterName() ) || CollectionUtil.isCollectionEmpty( config.getWorkers() )
                || config.getCoordinatorNode() == null ) {
            po.addLogFailed( "Malformed configuration\nInstallation aborted" );
            return;
        }

        if ( manager.getCluster( config.getClusterName() ) != null ) {
            po.addLogFailed( String.format( "Cluster with name '%s' already exists\nInstallation aborted",
                    config.getClusterName() ) );
            return;
        }

        if ( manager.getAgentManager().getAgentByHostname( config.getCoordinatorNode().getHostname() ) == null ) {
            po.addLogFailed( "Coordinator node is not connected\nInstallation aborted" );
            return;
        }

        //check if node agent is connected
        for ( Iterator<Agent> it = config.getWorkers().iterator(); it.hasNext(); ) {
            Agent node = it.next();
            if ( manager.getAgentManager().getAgentByHostname( node.getHostname() ) == null ) {
                po.addLog( String.format( "Node %s is not connected. Omitting this node from installation",
                        node.getHostname() ) );
                it.remove();
            }
        }

        if ( config.getWorkers().isEmpty() ) {
            po.addLogFailed( "No nodes eligible for installation\nInstallation aborted" );
            return;
        }

        setupWithHadoop();
    }


    /**
     * Sets up a Presto cluster ovre Hadoop
     */
    private void setupWithHadoop() {

        try {
            ClusterSetupStrategy prestoClusterStrategy = manager.getClusterSetupStrategy( po, config );
            prestoClusterStrategy.setup();

            po.addLogDone( String.format( "Cluster %s set up successfully", clusterName ) );
        }
        catch ( ClusterSetupException e ) {
            po.addLogFailed( String.format( "Failed to setup Presto cluster %s : %s", clusterName, e.getMessage() ) );
        }
    }
}
