package org.safehaus.subutai.plugin.elasticsearch.impl.handler;


import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.core.container.api.lxcmanager.LxcDestroyException;
import org.safehaus.subutai.plugin.elasticsearch.api.ElasticsearchClusterConfiguration;
import org.safehaus.subutai.plugin.elasticsearch.impl.ElasticsearchImpl;


public class UninstallOperationHandler extends AbstractOperationHandler<ElasticsearchImpl>
{

    public UninstallOperationHandler( ElasticsearchImpl manager, String clusterName )
    {
        super( manager, clusterName );
        productOperation = manager.getTracker().createProductOperation( ElasticsearchClusterConfiguration.PRODUCT_KEY,
                String.format( "Destroying %s cluster...", clusterName ) );
    }


    public void run()
    {
        ElasticsearchClusterConfiguration elasticsearchClusterConfiguration = manager.getCluster( clusterName );
        if ( elasticsearchClusterConfiguration == null )
        {
            productOperation.addLogFailed(
                    String.format( "Cluster with name %s does not exist. Operation aborted", clusterName ) );
            return;
        }

        productOperation.addLog( "Destroying lxc containers..." );

        try
        {
            manager.getContainerManager().clonesDestroy( elasticsearchClusterConfiguration.getNodes() );
            productOperation.addLog( "Lxc containers successfully destroyed" );
        }
        catch ( LxcDestroyException ex )
        {
            productOperation.addLog( String.format( "%s, skipping...", ex.getMessage() ) );
        }

        productOperation.addLog( "Updating db..." );

        manager.getPluginDAO().deleteInfo( ElasticsearchClusterConfiguration.PRODUCT_KEY,
                elasticsearchClusterConfiguration.getClusterName() );
        productOperation.addLogDone( "Cluster info deleted from DB\nDone" );
    }
}
