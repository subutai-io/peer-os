package org.safehaus.subutai.plugin.elasticsearch.impl.handler;

import org.safehaus.subutai.common.command.Command;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.plugin.elasticsearch.api.ElasticsearchClusterConfiguration;
import org.safehaus.subutai.plugin.elasticsearch.impl.Commands;
import org.safehaus.subutai.plugin.elasticsearch.impl.ElasticsearchImpl;

public class StartNodeOperationHandler extends AbstractOperationHandler<ElasticsearchImpl> {
    private String clusterName;

    public StartNodeOperationHandler( final ElasticsearchImpl manager, final String clusterName ) {
        super( manager, clusterName );
        this.clusterName = clusterName;
        productOperation = manager.getTracker().createProductOperation( ElasticsearchClusterConfiguration.PRODUCT_KEY,
                String.format( "Starting %s cluster...", clusterName ) );
    }


    @Override
    public void run() {
        ElasticsearchClusterConfiguration config = manager.getCluster( clusterName );
        if ( config == null ) {
            productOperation.addLogFailed(
                    String.format( "Cluster with name %s does not exist\nOperation aborted", clusterName ) );
            return;
        }
        Command startServiceCommand = Commands.getStartCommand( config.getNodes() );
        manager.getCommandRunner().runCommand( startServiceCommand );

        if ( startServiceCommand.hasSucceeded() ) {
            productOperation.addLogDone( "Start succeeded" );
        }
        else {
            productOperation.addLogFailed( String.format( "Start failed, %s", startServiceCommand.getAllErrors() ) );
        }
    }
}
