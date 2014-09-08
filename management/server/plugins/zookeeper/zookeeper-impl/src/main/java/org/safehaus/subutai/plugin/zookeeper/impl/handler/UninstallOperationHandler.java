package org.safehaus.subutai.plugin.zookeeper.impl.handler;


import java.util.UUID;

import org.safehaus.subutai.core.command.api.Command;
import org.safehaus.subutai.core.db.api.DBException;
import org.safehaus.subutai.core.container.api.lxcmanager.LxcDestroyException;
import org.safehaus.subutai.plugin.zookeeper.api.SetupType;
import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;
import org.safehaus.subutai.plugin.zookeeper.impl.Commands;
import org.safehaus.subutai.plugin.zookeeper.impl.ZookeeperImpl;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.tracker.ProductOperation;


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
        //@todo may be we should always just uninstall ZK or check always it there are any other subutai packages
        // installed on the same nodes
        //because environment supplied initially could contain other products or other products might've been
        // installed later
        if ( config.getSetupType() == SetupType.STANDALONE ) {
            po.addLog( "Destroying lxc containers" );
            try {
                manager.getContainerManager().clonesDestroy( config.getNodes() );
                po.addLog( "Lxc containers successfully destroyed" );
            }
            catch ( LxcDestroyException ex ) {
                po.addLog( String.format( "%s, skipping...", ex.getMessage() ) );
            }

            po.addLog( "Deleting cluster information from database..." );

            try {
                manager.getPluginDAO().deleteInfo( ZookeeperClusterConfig.PRODUCT_KEY, config.getClusterName() );
                po.addLogDone( "Cluster information deleted from database" );
            }
            catch ( DBException e ) {
                po.addLogFailed(
                        String.format( "Error while deleting cluster information from database, %s", e.getMessage() ) );
            }
        }
        else {
            //just uninstall nodes
            po.addLog( String.format( "Uninstalling %s", ZookeeperClusterConfig.PRODUCT_NAME ) );

            Command uninstallCommand = Commands.getUninstallCommand( config.getNodes() );

            if ( uninstallCommand.hasCompleted() ) {
                if ( uninstallCommand.hasSucceeded() ) {
                    po.addLog( "Cluster successfully uninstalled" );
                }
                else {
                    po.addLog( String.format( "Uninstallation failed, %s, skipping...",
                            uninstallCommand.getAllErrors() ) );
                }

                po.addLog( "Deleting cluster information from database..." );

                try {
                    manager.getPluginDAO().deleteInfo( ZookeeperClusterConfig.PRODUCT_KEY, config.getClusterName() );
                    po.addLogDone( "Cluster information deleted from database" );
                }
                catch ( DBException e ) {
                    po.addLogFailed( String.format( "Error while deleting cluster information from database, %s",
                            e.getMessage() ) );
                }
            }
            else {
                po.addLogFailed( "Uninstallation failed, command timed out" );
            }
        }
    }
}
