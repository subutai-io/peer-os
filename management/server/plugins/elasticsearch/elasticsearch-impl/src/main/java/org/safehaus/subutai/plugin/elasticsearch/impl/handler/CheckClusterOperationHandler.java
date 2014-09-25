package org.safehaus.subutai.plugin.elasticsearch.impl.handler;


import java.util.Map;
import java.util.UUID;

import org.safehaus.subutai.core.command.api.command.AgentResult;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.plugin.elasticsearch.api.ElasticsearchClusterConfiguration;
import org.safehaus.subutai.plugin.elasticsearch.impl.Commands;
import org.safehaus.subutai.plugin.elasticsearch.impl.ElasticsearchImpl;


public class CheckClusterOperationHandler extends AbstractOperationHandler<ElasticsearchImpl>
{
    private String clusterName;


    public CheckClusterOperationHandler( final ElasticsearchImpl manager, final String clusterName )
    {
        super( manager, clusterName );
        this.clusterName = clusterName;
        productOperation = manager.getTracker().createProductOperation( ElasticsearchClusterConfiguration.PRODUCT_KEY,
                String.format( "Starting %s cluster...", clusterName ) );
    }


    public void run()
    {
        ElasticsearchClusterConfiguration elasticsearchClusterConfiguration = manager.getCluster( clusterName );
        if ( elasticsearchClusterConfiguration == null )
        {
            productOperation.addLogFailed(
                    String.format( "Cluster with name %s does not exist\nOperation aborted", clusterName ) );
            return;
        }

        Command checkStatusCommand = Commands.getStatusCommand( elasticsearchClusterConfiguration.getNodes() );
        manager.getCommandRunner().runCommand( checkStatusCommand );

        if ( checkStatusCommand.hasSucceeded() )
        {
            productOperation.addLogDone( "All nodes are running." );
        }
        else
        {
            logStatusResults( productOperation, checkStatusCommand );
        }
    }


    private void logStatusResults( ProductOperation po, Command checkStatusCommand )
    {

        StringBuilder log = new StringBuilder();

        for ( Map.Entry<UUID, AgentResult> e : checkStatusCommand.getResults().entrySet() )
        {

            String status = "UNKNOWN";
            if ( e.getValue().getExitCode() == 0 )
            {
                status = "elasticsearch is running";
            }
            else if ( e.getValue().getExitCode() == 768 )
            {
                status = "elasticsearch is not running";
            }

            log.append( String.format( "- %s: %s\n", e.getValue().getAgentUUID(), status ) ).append( "\n" );
        }

        po.addLogDone( log.toString() );
    }
}
