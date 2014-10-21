package org.safehaus.subutai.plugin.elasticsearch.impl.handler;


import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.command.api.command.AgentResult;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.plugin.elasticsearch.api.ElasticsearchClusterConfiguration;
import org.safehaus.subutai.plugin.elasticsearch.impl.ElasticsearchImpl;

import com.google.common.collect.Sets;


public class StopNodeOperationHandler extends AbstractOperationHandler<ElasticsearchImpl>
{
    private String lxcHostname;
    private String clusterName;


    public StopNodeOperationHandler( final ElasticsearchImpl manager, final String clusterName,
                                     final String lxcHostname )
    {
        super( manager, clusterName );
        this.lxcHostname = lxcHostname;
        this.clusterName = clusterName;
        trackerOperation = manager.getTracker().createTrackerOperation( ElasticsearchClusterConfiguration.PRODUCT_KEY,
                String.format( "Stopping %s cluster...", clusterName ) );
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
            trackerOperation.addLogFailed( String.format( "Agent with hostname %s is not connected", lxcHostname ) );
            return;
        }
        if ( !elasticsearchClusterConfiguration.getNodes().contains( node ) )
        {
            trackerOperation.addLogFailed(
                    String.format( "Agent with hostname %s does not belong to cluster %s", lxcHostname, clusterName ) );
            return;
        }


        Command stopServiceCommand = manager.getCommands().getStopCommand( Sets.newHashSet( node ) );
        manager.getCommandRunner().runCommand( stopServiceCommand );
        if ( stopServiceCommand.hasSucceeded() )
        {
            AgentResult ar = stopServiceCommand.getResults().get( node.getUuid() );
            if ( ar.getStdOut().contains( "Stopping Elasticsearch Server" ) )
            {
                trackerOperation.addLogDone( "elasticsearch is not running" );
            }
            else
            {
                trackerOperation.addLogFailed( "Could not stop Elasticsearch" );
            }
        }
        else
        {
            trackerOperation.addLogFailed( "Elasticsearch stop command is not succeeded !!!\"" );
        }
    }
}
