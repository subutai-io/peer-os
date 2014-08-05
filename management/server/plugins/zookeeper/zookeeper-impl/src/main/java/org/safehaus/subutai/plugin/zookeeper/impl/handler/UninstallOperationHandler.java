package org.safehaus.subutai.plugin.zookeeper.impl.handler;


import java.util.UUID;

import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.lxcmanager.LxcDestroyException;
import org.safehaus.subutai.plugin.zookeeper.api.SetupType;
import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;
import org.safehaus.subutai.plugin.zookeeper.impl.Commands;
import org.safehaus.subutai.plugin.zookeeper.impl.ZookeeperImpl;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.Agent;


/**
 * Handles uninstall cluster operation
 */
public class UninstallOperationHandler extends AbstractOperationHandler<ZookeeperImpl> {
    private final ProductOperation po;


    public UninstallOperationHandler( ZookeeperImpl manager, String clusterName ) {
        super( manager, clusterName );
        po = manager.getTracker().createProductOperation( ZookeeperClusterConfig.PRODUCT_KEY,
                String.format( "Destroying cluster %s", clusterName ) );
    }


    @Override
    public UUID getTrackerId() {
        return po.getId();
    }


    @Override
    public void run() {
        ZookeeperClusterConfig config = manager.getCluster( clusterName );
        if ( config == null ) {
            po.addLogFailed( String.format( "Cluster with name %s does not exist\nOperation aborted", clusterName ) );
            return;
        }

        if ( config.getSetupType() == SetupType.STANDALONE ) {
            po.addLog( "Destroying lxc containers" );
            try {
                for ( Agent agent : config.getNodes() ) {
                    manager.getContainerManager().cloneDestroy( agent.getParentHostName(), agent.getHostname() );
                }
                po.addLog( "Lxc containers successfully destroyed" );
            }
            catch ( LxcDestroyException ex ) {
                po.addLog( String.format( "%s, skipping...", ex.getMessage() ) );
            }

            po.addLog( "Updating db..." );
            if ( manager.getDbManager().deleteInfo( ZookeeperClusterConfig.PRODUCT_KEY, config.getClusterName() ) ) {
                po.addLogDone( "Cluster info deleted from DB\nDone" );
            }
            else {
                po.addLogFailed( "Error while deleting cluster info from DB. Check logs.\nFailed" );
            }
        }
        else if ( config.getSetupType() == SetupType.OVER_HADOOP || config.getSetupType() == SetupType.WITH_HADOOP ) {
            //just uninstall nodes
            po.addLog( String.format( "Uninstalling %s", ZookeeperClusterConfig.PRODUCT_KEY ) );

            Command uninstallCommand = Commands.getUninstallCommand( config.getNodes() );

            if ( uninstallCommand.hasCompleted() ) {
                if ( uninstallCommand.hasSucceeded() ) {
                    po.addLog( "Cluster successfully uninstalled" );
                }
                else {
                    po.addLog( String.format( "Uninstallation failed, %s, skipping...",
                            uninstallCommand.getAllErrors() ) );
                }

                po.addLog( "Updating db..." );
                if ( manager.getDbManager()
                            .deleteInfo( ZookeeperClusterConfig.PRODUCT_KEY, config.getClusterName() ) ) {
                    po.addLogDone( "Cluster info deleted from DB\nDone" );
                }
                else {
                    po.addLogFailed( "Error while deleting cluster info from DB. Check logs.\nFailed" );
                }
            }
            else {
                po.addLogFailed( "Uninstallation failed, command timed out" );
            }
        }
    }
}
