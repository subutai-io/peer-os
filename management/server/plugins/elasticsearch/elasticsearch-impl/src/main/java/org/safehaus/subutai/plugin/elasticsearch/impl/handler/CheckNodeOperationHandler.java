package org.safehaus.subutai.plugin.elasticsearch.impl.handler;


import java.util.Map;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.command.api.command.AgentResult;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.plugin.elasticsearch.api.ElasticsearchClusterConfiguration;
import org.safehaus.subutai.plugin.elasticsearch.impl.ElasticsearchImpl;

import com.google.common.collect.Sets;


public class CheckNodeOperationHandler extends AbstractOperationHandler<ElasticsearchImpl>
{
    private String lxcHostname;
    private String clusterName;


    public CheckNodeOperationHandler( final ElasticsearchImpl manager, final String clusterName,
                                      final String agentUUID )
    {
        super( manager, clusterName );
        this.lxcHostname = agentUUID;
        this.clusterName = clusterName;
        this.trackerOperation = manager.getTracker()
                                       .createTrackerOperation( ElasticsearchClusterConfiguration.PRODUCT_KEY,
                                               String.format( "Checking %s cluster...", clusterName ) );
    }


    @Override
    public void run()
    {

        ElasticsearchClusterConfiguration elasticsearchClusterConfiguration = manager.getCluster( clusterName );
        if ( elasticsearchClusterConfiguration == null )
        {
            trackerOperation.addLogFailed( String.format( "Cluster with name %s does not exist", clusterName ) );
            return;
        }

        final Agent node = manager.getAgentManager().getAgentByHostname( lxcHostname );
        if ( node == null )
        {
            trackerOperation.addLogFailed( "Agent is not connected !" );
            return;
        }
        if ( !elasticsearchClusterConfiguration.getNodes().contains( node ) )
        {
            trackerOperation.addLogFailed(
                    String.format( "Agent with hostname %s does not belong to cluster %s", lxcHostname, clusterName ) );
            return;
        }

        Command statusServiceCommand = manager.getCommands().getStatusCommand( Sets.newHashSet( node ) );
        manager.getCommandRunner().runCommand( statusServiceCommand );

        if ( statusServiceCommand.hasSucceeded() )
        {
            trackerOperation.addLogDone( "elasticsearch is running" );
        }
        else
        {
            logStatusResults( trackerOperation, statusServiceCommand );
        }
    }


    private void logStatusResults( TrackerOperation po, Command checkStatusCommand )
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

            log.append( String.format( "%s\n", status ) ).append( "\n" );
        }

        po.addLogDone( log.toString() );
    }
}
