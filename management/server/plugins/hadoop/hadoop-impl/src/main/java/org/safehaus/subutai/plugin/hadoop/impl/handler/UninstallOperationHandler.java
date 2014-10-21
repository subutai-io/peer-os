package org.safehaus.subutai.plugin.hadoop.impl.handler;


import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.core.container.api.lxcmanager.LxcDestroyException;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.hadoop.impl.HadoopImpl;

import com.google.common.collect.Sets;


public class UninstallOperationHandler extends AbstractOperationHandler<HadoopImpl>
{

    public UninstallOperationHandler( HadoopImpl manager, String clusterName )
    {
        super( manager, clusterName );
        trackerOperation = manager.getTracker().createTrackerOperation( HadoopClusterConfig.PRODUCT_KEY,
                String.format( "Destroying installation %s", clusterName ) );
    }


    @Override
    public void run()
    {
        HadoopClusterConfig hadoopClusterConfig = manager.getCluster( clusterName );

        if ( hadoopClusterConfig == null )
        {
            trackerOperation.addLogFailed( String.format( "Installation with name %s does not exist", clusterName ) );
            return;
        }

        trackerOperation.addLog( "Destroying lxc containers..." );

        try
        {
            manager.getContainerManager().clonesDestroy( Sets.newHashSet( hadoopClusterConfig.getAllNodes() ) );
            trackerOperation.addLog( "Lxc containers successfully destroyed" );
        }
        catch ( LxcDestroyException ex )
        {
            trackerOperation.addLog( String.format( "%s, skipping...", ex.getMessage() ) );
        }

        trackerOperation.addLog( "Updating db..." );

        manager.getPluginDAO().deleteInfo( HadoopClusterConfig.PRODUCT_KEY, hadoopClusterConfig.getClusterName() );
        trackerOperation.addLogDone( "Information updated in database" );
    }
}
