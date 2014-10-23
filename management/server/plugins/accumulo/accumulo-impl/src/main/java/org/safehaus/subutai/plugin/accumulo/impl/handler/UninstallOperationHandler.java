package org.safehaus.subutai.plugin.accumulo.impl.handler;


import java.util.UUID;

import org.safehaus.subutai.common.exception.ClusterConfigurationException;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.plugin.accumulo.api.AccumuloClusterConfig;
import org.safehaus.subutai.plugin.accumulo.impl.AccumuloImpl;
import org.safehaus.subutai.plugin.accumulo.impl.ClusterConfiguration;


/**
 * Handles destroy cluster operation
 */
public class UninstallOperationHandler extends AbstractOperationHandler<AccumuloImpl>
{

    public UninstallOperationHandler( AccumuloImpl manager, String clusterName )
    {
        super( manager, clusterName );
        trackerOperation = manager.getTracker().createTrackerOperation( AccumuloClusterConfig.PRODUCT_KEY,
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
        AccumuloClusterConfig accumuloClusterConfig = manager.getCluster( clusterName );
        if ( accumuloClusterConfig == null )
        {
            trackerOperation.addLogFailed( String.format( "Cluster with name %s does not exist", clusterName ) );
            return;
        }

        try
        {
            new ClusterConfiguration( trackerOperation, manager ).destroyCluster( accumuloClusterConfig );
            trackerOperation.addLogDone( "Cluster successfully destroyed" );
        }
        catch ( ClusterConfigurationException e )
        {
            trackerOperation.addLogFailed( String.format( "Failed to destroy cluster, %s", e.getMessage() ) );
        }
    }
}
