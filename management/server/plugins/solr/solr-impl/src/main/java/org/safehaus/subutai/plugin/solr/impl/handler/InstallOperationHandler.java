package org.safehaus.subutai.plugin.solr.impl.handler;


import java.util.UUID;

import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentBuildException;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.plugin.solr.api.SolrClusterConfig;
import org.safehaus.subutai.plugin.solr.impl.SolrImpl;


public class InstallOperationHandler extends AbstractOperationHandler<SolrImpl>
{
    private final SolrClusterConfig solrClusterConfig;
    private TrackerOperation trackerOperation;


    public InstallOperationHandler( SolrImpl manager, SolrClusterConfig solrClusterConfig )
    {
        super( manager, solrClusterConfig.getClusterName() );
        this.solrClusterConfig = solrClusterConfig;
        trackerOperation = manager.getTracker().createTrackerOperation( SolrClusterConfig.PRODUCT_KEY,
                String.format( "Setting up %s cluster...", clusterName ) );
    }

    @Override
    public UUID getTrackerId()
    {
        return trackerOperation.getId();
    }

    @Override
    public void run()
    {

        trackerOperation.addLog( "Building environment..." );

        try
        {
            Environment env = manager.getEnvironmentManager()
                                     .buildEnvironment( manager.getDefaultEnvironmentBlueprint( solrClusterConfig ) );

            ClusterSetupStrategy clusterSetupStrategy =
                    manager.getClusterSetupStrategy( env, solrClusterConfig, trackerOperation );
            clusterSetupStrategy.setup();

            //solrClusterConfig.setEnvironmentId( env.getId() );

            trackerOperation.addLogDone( String.format( "Cluster %s set up successfully", clusterName ) );
        }
        catch ( EnvironmentBuildException | ClusterSetupException e )
        {
            String msg = String.format( "Failed to setup cluster %s : %s", clusterName, e.getMessage() );
            trackerOperation.addLogFailed( msg );
            throw new RuntimeException( msg );
        }
    }
}
