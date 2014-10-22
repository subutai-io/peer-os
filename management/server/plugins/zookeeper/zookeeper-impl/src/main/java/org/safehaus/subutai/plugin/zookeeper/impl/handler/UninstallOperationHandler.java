package org.safehaus.subutai.plugin.zookeeper.impl.handler;


import java.util.UUID;

import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.core.container.api.lxcmanager.LxcDestroyException;
import org.safehaus.subutai.plugin.zookeeper.api.SetupType;
import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;
import org.safehaus.subutai.plugin.zookeeper.impl.ZookeeperImpl;


/**
 * Handles uninstall cluster operation
 */
public class UninstallOperationHandler extends AbstractOperationHandler<ZookeeperImpl>
{

    public UninstallOperationHandler( ZookeeperImpl manager, String clusterName )
    {
        super( manager, clusterName );
        trackerOperation = manager.getTracker().createTrackerOperation( ZookeeperClusterConfig.PRODUCT_KEY,
                String.format( "Destroying cluster %s", clusterName ) );
    }


    @Override
    public UUID getTrackerId()
    {
        return trackerOperation.getId();
    }


    @Override
    public void run()
    {
        ZookeeperClusterConfig config = manager.getCluster( clusterName );
        if ( config == null )
        {
            trackerOperation.addLogFailed(
                    String.format( "Cluster with name %s does not exist\nOperation aborted", clusterName ) );
            return;
        }
        //@todo may be we should always just uninstall ZK or check always it there are any other subutai packages
        // installed on the same nodes
        // because environment supplied initially could contain other products or other products might've been
        // installed later
        if ( config.getSetupType() == SetupType.STANDALONE )
        {
            trackerOperation.addLog( "Destroying lxc containers" );
            try
            {
                manager.getContainerManager().clonesDestroy( config.getNodes() );
                trackerOperation.addLog( "Lxc containers successfully destroyed" );
            }
            catch ( LxcDestroyException ex )
            {
                trackerOperation.addLog( String.format( "%s, skipping...", ex.getMessage() ) );
            }

            trackerOperation.addLog( "Deleting cluster information from database..." );

            manager.getPluginDAO().deleteInfo( ZookeeperClusterConfig.PRODUCT_KEY, config.getClusterName() );
            trackerOperation.addLogDone( "Cluster information deleted from database" );
        }
        else
        {
            //just uninstall nodes
            trackerOperation.addLog( String.format( "Uninstalling %s", ZookeeperClusterConfig.PRODUCT_NAME ) );

            Command uninstallCommand = manager.getCommands().getUninstallCommand( config.getNodes() );
            manager.getCommandRunner().runCommand( uninstallCommand );

            if ( uninstallCommand.hasCompleted() )
            {
                if ( uninstallCommand.hasSucceeded() )
                {
                    trackerOperation.addLog( "Cluster successfully uninstalled" );
                }
                else
                {
                    trackerOperation.addLog( String.format( "Uninstallation failed, %s, skipping...",
                            uninstallCommand.getAllErrors() ) );
                }

                trackerOperation.addLog( "Deleting cluster information from database..." );

                manager.getPluginDAO().deleteInfo( ZookeeperClusterConfig.PRODUCT_KEY, config.getClusterName() );
                trackerOperation.addLogDone( "Cluster information deleted from database" );
            }
            else
            {
                trackerOperation.addLogFailed( "Uninstallation failed, command timed out" );
            }
        }
    }
}
