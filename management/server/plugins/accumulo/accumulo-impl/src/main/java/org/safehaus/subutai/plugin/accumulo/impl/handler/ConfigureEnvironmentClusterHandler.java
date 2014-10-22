package org.safehaus.subutai.plugin.accumulo.impl.handler;


import java.util.UUID;

import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.plugin.accumulo.api.AccumuloClusterConfig;
import org.safehaus.subutai.plugin.accumulo.impl.AccumuloImpl;


public class ConfigureEnvironmentClusterHandler extends AbstractOperationHandler<AccumuloImpl>
{

    private AccumuloClusterConfig config;
    private TrackerOperation po;


    public ConfigureEnvironmentClusterHandler( final AccumuloImpl manager, final AccumuloClusterConfig config )
    {
        super( manager, config.getClusterName() );
        this.config = config;
        po = manager.getTracker().createTrackerOperation( AccumuloClusterConfig.PRODUCT_KEY,
                String.format( "Setting up %s cluster...", config.getClusterName() ) );
    }


    @Override
    public UUID getTrackerId()
    {
        return po.getId();
    }


    @Override
    public void run()
    {
        trackerOperation = po;
        po.addLog( "Building environment..." );

        try
        {
            Environment env = manager.getEnvironmentManager().getEnvironmentByUUID( config.getEnvironmentId() );

            ClusterSetupStrategy clusterSetupStrategy = manager.getClusterSetupStrategy( env, config, po );
            clusterSetupStrategy.setup();

            po.addLogDone( String.format( "Cluster %s set up successfully", clusterName ) );
        }
        catch ( ClusterSetupException e )
        {
            po.addLogFailed( String.format( "Failed to setup cluster %s : %s", clusterName, e.getMessage() ) );
        }
    }
}
