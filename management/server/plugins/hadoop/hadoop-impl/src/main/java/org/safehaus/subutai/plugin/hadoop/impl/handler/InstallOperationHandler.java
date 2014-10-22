package org.safehaus.subutai.plugin.hadoop.impl.handler;


import java.util.UUID;

import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.hadoop.impl.HadoopImpl;

import com.google.common.base.Strings;


public class InstallOperationHandler extends AbstractOperationHandler<HadoopImpl>
{
    private final TrackerOperation trackerOperation;
    private final HadoopClusterConfig config;


    public InstallOperationHandler( HadoopImpl manager, HadoopClusterConfig config )
    {
        super( manager, config.getClusterName() );
        this.config = config;
        trackerOperation = manager.getTracker().createTrackerOperation( HadoopClusterConfig.PRODUCT_KEY,
                String.format( "Installing %s", HadoopClusterConfig.PRODUCT_KEY ) );
    }


    @Override
    public UUID getTrackerId()
    {
        return trackerOperation.getId();
    }


    @Override
    public void run()
    {
        if ( Strings.isNullOrEmpty( config.getClusterName() ) ||
                config.getCountOfSlaveNodes() == null ||
                config.getCountOfSlaveNodes() <= 0 )
        {
            trackerOperation.addLogFailed( "Malformed configuration\nInstallation aborted" );
            return;
        }

        if ( manager.getCluster( config.getClusterName() ) != null )
        {
            trackerOperation.addLogFailed( String.format( "Cluster with name '%s' already exists\nInstallation aborted",
                    config.getClusterName() ) );
            return;
        }

        setup();
    }


    private void setup()
    {

        try
        {
            ClusterSetupStrategy setupStrategy = manager.getClusterSetupStrategy( trackerOperation, config );
            setupStrategy.setup();

            trackerOperation.addLogDone( String.format( "Cluster %s set up successfully", clusterName ) );
        }
        catch ( ClusterSetupException e )
        {
            trackerOperation.addLogFailed(
                    String.format( "Failed to setup Hadoop cluster %s : %s", clusterName, e.getMessage() ) );
        }
    }
}
