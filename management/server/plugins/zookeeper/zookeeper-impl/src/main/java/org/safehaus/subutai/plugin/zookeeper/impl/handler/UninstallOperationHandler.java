package org.safehaus.subutai.plugin.zookeeper.impl.handler;


import org.safehaus.subutai.common.command.Command;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.core.container.api.lxcmanager.LxcDestroyException;
import org.safehaus.subutai.core.db.api.DBException;
import org.safehaus.subutai.plugin.zookeeper.api.SetupType;
import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;
import org.safehaus.subutai.plugin.zookeeper.impl.Commands;
import org.safehaus.subutai.plugin.zookeeper.impl.ZookeeperImpl;

import java.util.UUID;


/**
 * Handles uninstall cluster operation
 */
public class UninstallOperationHandler extends AbstractOperationHandler<ZookeeperImpl> {

    public UninstallOperationHandler( ZookeeperImpl manager, String clusterName ) {
        super( manager, clusterName );
        productOperation = manager.getTracker().createProductOperation( ZookeeperClusterConfig.PRODUCT_KEY,
                String.format( "Destroying cluster %s", clusterName ) );
    }


    @Override
    public UUID getTrackerId() {
        return productOperation.getId();
    }


    @Override
    public void run() {
        ZookeeperClusterConfig config = manager.getCluster( clusterName );
        if ( config == null ) {
            productOperation.addLogFailed( String.format( "Cluster with name %s does not exist\nOperation aborted", clusterName ) );
            return;
        }
        //@todo may be we should always just uninstall ZK or check always it there are any other subutai packages
        // installed on the same nodes
        //because environment supplied initially could contain other products or other products might've been
        // installed later
        if ( config.getSetupType() == SetupType.STANDALONE ) {
            productOperation.addLog( "Destroying lxc containers" );
            try {
                manager.getContainerManager().clonesDestroy( config.getNodes() );
                productOperation.addLog( "Lxc containers successfully destroyed" );
            }
            catch ( LxcDestroyException ex ) {
                productOperation.addLog( String.format( "%s, skipping...", ex.getMessage() ) );
            }

            productOperation.addLog( "Deleting cluster information from database..." );

            try {
                manager.getPluginDAO().deleteInfo( ZookeeperClusterConfig.PRODUCT_KEY, config.getClusterName() );
                productOperation.addLogDone( "Cluster information deleted from database" );
            }
            catch ( DBException e ) {
                productOperation.addLogFailed(
                        String.format( "Error while deleting cluster information from database, %s", e.getMessage() ) );
            }
        }
        else {
            //just uninstall nodes
            productOperation.addLog( String.format( "Uninstalling %s", ZookeeperClusterConfig.PRODUCT_NAME ) );

            Command uninstallCommand = Commands.getUninstallCommand( config.getNodes() );

            if ( uninstallCommand.hasCompleted() ) {
                if ( uninstallCommand.hasSucceeded() ) {
                    productOperation.addLog( "Cluster successfully uninstalled" );
                }
                else {
                    productOperation.addLog( String.format( "Uninstallation failed, %s, skipping...",
                            uninstallCommand.getAllErrors() ) );
                }

                productOperation.addLog( "Deleting cluster information from database..." );

                try {
                    manager.getPluginDAO().deleteInfo( ZookeeperClusterConfig.PRODUCT_KEY, config.getClusterName() );
                    productOperation.addLogDone( "Cluster information deleted from database" );
                }
                catch ( DBException e ) {
                    productOperation.addLogFailed( String.format( "Error while deleting cluster information from database, %s",
                            e.getMessage() ) );
                }
            }
            else {
                productOperation.addLogFailed( "Uninstallation failed, command timed out" );
            }
        }
    }
}
