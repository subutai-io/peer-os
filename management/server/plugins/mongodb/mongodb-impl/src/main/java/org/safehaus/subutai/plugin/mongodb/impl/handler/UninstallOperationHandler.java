package org.safehaus.subutai.plugin.mongodb.impl.handler;


import java.util.UUID;

import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentDestroyException;
import org.safehaus.subutai.plugin.mongodb.api.MongoClusterConfig;
import org.safehaus.subutai.plugin.mongodb.impl.MongoImpl;


/**
 * Handles uninstall mongo cluster operation
 */
public class UninstallOperationHandler extends AbstractOperationHandler<MongoImpl, MongoClusterConfig>
{
    private final TrackerOperation po;


    public UninstallOperationHandler( MongoImpl manager, String clusterName )
    {
        super( manager, clusterName );
        po = manager.getTracker().createTrackerOperation( MongoClusterConfig.PRODUCT_KEY,
                String.format( "Destroying cluster %s", clusterName ) );
    }


    @Override
    public UUID getTrackerId()
    {
        return po.getId();
    }


    @Override
    public void run()
    {
        MongoClusterConfig config = manager.getCluster( clusterName );
        if ( config == null )
        {
            po.addLogFailed( String.format( "Cluster with name %s does not exist", clusterName ) );
            return;
        }

        po.addLog( "Destroying lxc containers" );
        try
        {
            manager.getEnvironmentManager().destroyEnvironment( config.getEnvironmentId() );
            po.addLog( "Lxc containers successfully destroyed" );
        }
        catch ( EnvironmentDestroyException ex )
        {
            po.addLog( String.format( "%s, skipping...", ex.getMessage() ) );
        }

        po.addLog( "Deleting cluster information from database.." );

        manager.getPluginDAO().deleteInfo( MongoClusterConfig.PRODUCT_KEY, config.getClusterName() );
        po.addLogDone( "Cluster destroyed." );
    }
}
