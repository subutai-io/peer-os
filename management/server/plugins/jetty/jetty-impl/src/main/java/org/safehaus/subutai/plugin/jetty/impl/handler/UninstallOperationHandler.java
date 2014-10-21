package org.safehaus.subutai.plugin.jetty.impl.handler;


import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.core.container.api.lxcmanager.LxcDestroyException;
import org.safehaus.subutai.plugin.jetty.api.JettyConfig;
import org.safehaus.subutai.plugin.jetty.impl.JettyImpl;


public class UninstallOperationHandler extends AbstractOperationHandler<JettyImpl>
{

    public UninstallOperationHandler(final JettyImpl manager, final String clusterName) {
        super( manager, clusterName );
        trackerOperation = manager.getTracker().createTrackerOperation( JettyConfig.PRODUCT_KEY,
                String.format( "Destroying %s cluster...", clusterName ) );
    }

    @Override
    public void run()
    {
        trackerOperation.addLog( "Building environment..." );
        JettyConfig config = manager.getCluster( clusterName );
        if ( config == null )
        {
            trackerOperation.addLogFailed( String.format( "Cluster with name %s does not exist", clusterName ) );
            return;
        }

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

        trackerOperation.addLog( "Deleting cluster information from database.." );

        manager.getPluginDAO().deleteInfo( JettyConfig.PRODUCT_KEY, config.getClusterName() );
        trackerOperation.addLogDone( "Cluster info deleted from database" );
    }
}
