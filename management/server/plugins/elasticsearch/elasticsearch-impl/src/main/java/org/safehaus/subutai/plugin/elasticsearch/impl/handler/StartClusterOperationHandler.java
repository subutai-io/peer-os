package org.safehaus.subutai.plugin.elasticsearch.impl.handler;


import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.plugin.elasticsearch.api.ElasticsearchClusterConfiguration;
import org.safehaus.subutai.plugin.elasticsearch.impl.ElasticsearchImpl;


public class StartClusterOperationHandler extends AbstractOperationHandler<ElasticsearchImpl>
{
    private String clusterName;


    public StartClusterOperationHandler( final ElasticsearchImpl manager, final String clusterName )
    {
        super( manager, clusterName );
        this.clusterName = clusterName;
        trackerOperation = manager.getTracker().createTrackerOperation( ElasticsearchClusterConfiguration.PRODUCT_KEY,
                String.format( "Starting %s cluster...", clusterName ) );
    }


    public void run()
    {
        ElasticsearchClusterConfiguration elasticsearchClusterConfiguration = manager.getCluster( clusterName );
        if ( elasticsearchClusterConfiguration == null )
        {
            trackerOperation.addLogFailed(
                    String.format( "Cluster with name %s does not exist. Operation aborted", clusterName ) );
            return;
        }

        Command stopServiceCommand =
                manager.getCommands().getStartCommand( elasticsearchClusterConfiguration.getNodes() );
        manager.getCommandRunner().runCommand( stopServiceCommand );

        if ( stopServiceCommand.hasSucceeded() )
        {
            trackerOperation.addLogDone( "Start succeeded" );
        }
        else
        {
            trackerOperation.addLogFailed( String.format( "Stop failed, %s", stopServiceCommand.getAllErrors() ) );
        }
    }
}
