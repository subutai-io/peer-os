package org.safehaus.subutai.plugin.storm.impl.handler;


import java.util.UUID;

import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.storm.api.StormClusterConfiguration;
import org.safehaus.subutai.plugin.storm.impl.StormImpl;


public class DestroyNodeHandler extends AbstractHandler
{

    private final String hostname;


    public DestroyNodeHandler( StormImpl manager, String clusterName, String hostname )
    {
        super( manager, clusterName );
        this.trackerOperation = manager.getTracker().createTrackerOperation( StormClusterConfiguration.PRODUCT_NAME,
                "Remove node from cluster: " + hostname );
        this.hostname = hostname;
    }


    @Override
    public void run()
    {
        TrackerOperation po = trackerOperation;
        StormClusterConfiguration config = manager.getCluster( clusterName );
        if ( config == null )
        {
            po.addLogFailed( String.format( "Cluster '%s' does not exist", clusterName ) );
            return;
        }
        Environment environment =
                manager.getEnvironmentManager().getEnvironmentByUUID( config.getEnvironmentId() );
        ContainerHost containerHost = environment.getContainerHostByHostname( hostname );
        UUID containerHostId = containerHost.getId();
        if ( containerHostId == null )
        {
            po.addLogFailed( String.format( "Node '%s' is not connected", hostname ) );
            return;
        }
        if ( !config.getSupervisors().contains( containerHostId ) )
        {
            po.addLogFailed( "Node is not a member of cluster" );
            return;
        }
        if ( config.getSupervisors().size() == 1 )
        {
            po.addLogFailed( "This is the last node in cluster. Destroy cluster instead" );
            return;
        }

        po.addLogFailed( "Destroy node functionality is not provided by environment manager now. Aborting!" );
//        try
//        {
//            po.addLog( "Destroying container..." );
//            manager.getContainerManager().cloneDestroy( containerHostId.getParentHostName(), containerHostId.getHostname() );
//            po.addLog( "Container destroyed" );
//
//            config.getSupervisors().remove( containerHostId );
//
//            manager.getPluginDao().saveInfo( StormConfig.PRODUCT_NAME, clusterName, config );
//            po.addLogDone( "Saved cluster info" );
//        }
//        catch ( LxcDestroyException ex )
//        {
//            po.addLogFailed( "Failed to destroy node: " + ex.getMessage() );
//            manager.getLogger().error( "Destroy failed", ex );
//        }
    }
}
