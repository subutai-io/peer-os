package org.safehaus.subutai.plugin.hadoop.impl.handler;


import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentDestroyException;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.hadoop.impl.HadoopImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;


public class UninstallOperationHandler extends AbstractOperationHandler<HadoopImpl>
{
    private static final Logger LOG = LoggerFactory.getLogger( UninstallOperationHandler.class );

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

        try
        {
            trackerOperation.addLog( "Destroying environment..." );
            manager.getEnvironmentManager().destroyEnvironment( hadoopClusterConfig.getEnvironmentId() );
            manager.getPluginDAO().deleteInfo( HadoopClusterConfig.PRODUCT_KEY, hadoopClusterConfig.getClusterName() );
            trackerOperation.addLogDone( "Cluster destroyed" );
        }
        catch ( EnvironmentDestroyException e )
        {
            trackerOperation.addLogFailed( String.format( "Error running command, %s", e.getMessage() ) );
            LOG.error( e.getMessage(), e );
        }
    }
}
