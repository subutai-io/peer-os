package org.safehaus.subutai.plugin.solr.impl.handler;


import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentDestroyException;
import org.safehaus.subutai.plugin.solr.api.SolrClusterConfig;
import org.safehaus.subutai.plugin.solr.impl.SolrImpl;


public class UninstallOperationHandler extends AbstractOperationHandler<SolrImpl>
{

    public UninstallOperationHandler( SolrImpl manager, String clusterName )
    {
        super( manager, clusterName );
        trackerOperation = manager.getTracker().createTrackerOperation( SolrClusterConfig.PRODUCT_KEY,
                String.format( "Destroying installation %s", clusterName ) );
    }


    @Override
    public void run()
    {
        SolrClusterConfig solrClusterConfig = manager.getCluster( clusterName );

        if ( solrClusterConfig == null )
        {
            trackerOperation.addLogFailed( String.format( "Installation with name %s does not exist", clusterName ) );
            return;
        }


        try
        {
            trackerOperation.addLog( "Destroying environment..." );
            manager.getEnvironmentManager().destroyEnvironment( solrClusterConfig.getEnvironmentId() );
            manager.getPluginDAO().deleteInfo( solrClusterConfig.PRODUCT_KEY, solrClusterConfig.getClusterName() );
            trackerOperation.addLogDone( "Cluster destroyed" );
        }
        catch ( EnvironmentDestroyException e )
        {
            trackerOperation.addLog( String.format( "%s, skipping...", e.getMessage() ) );
        }

        trackerOperation.addLog( "Updating db..." );

        manager.getPluginDAO().deleteInfo( SolrClusterConfig.PRODUCT_KEY, solrClusterConfig.getClusterName() );
        trackerOperation.addLogDone( "Information updated in database" );
    }
}
