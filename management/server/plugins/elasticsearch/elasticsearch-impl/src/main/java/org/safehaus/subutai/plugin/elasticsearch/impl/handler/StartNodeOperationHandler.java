package org.safehaus.subutai.plugin.elasticsearch.impl.handler;


import java.util.concurrent.atomic.AtomicBoolean;

import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.Response;
import org.safehaus.subutai.core.command.api.command.AgentResult;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.core.command.api.command.CommandCallback;
import org.safehaus.subutai.plugin.elasticsearch.api.ElasticsearchClusterConfiguration;
import org.safehaus.subutai.plugin.elasticsearch.impl.ElasticsearchImpl;

import com.google.common.collect.Sets;


public class StartNodeOperationHandler extends AbstractOperationHandler<ElasticsearchImpl>
{
    private String clusterName;
    private String lxcHostname;


    public StartNodeOperationHandler( final ElasticsearchImpl manager, final String clusterName,
                                      final String lxcHostname )
    {
        super( manager, clusterName );
        this.clusterName = clusterName;
        this.lxcHostname = lxcHostname;
        trackerOperation = manager.getTracker().createTrackerOperation( ElasticsearchClusterConfiguration.PRODUCT_KEY,
                String.format( "Starting %s cluster...", clusterName ) );
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

        Command startNodeCommand = manager.getCommands().getStartCommand( Sets.newHashSet( node ) );
        final AtomicBoolean ok = new AtomicBoolean();
        manager.getCommandRunner().runCommand( startNodeCommand, new CommandCallback()
        {

            @Override
            public void onResponse( Response response, AgentResult agentResult, Command command )
            {
                if ( agentResult.getStdOut().contains( "Starting" ) )
                {
                    ok.set( true );
                    stop();
                }
            }
        } );

        if ( ok.get() )
        {
            trackerOperation.addLogDone( String.format( "Node %s started", node.getHostname() ) );
        }
        else
        {
            trackerOperation.addLogFailed( String.format( "Starting node %s failed, %s", node.getHostname(),
                    startNodeCommand.getAllErrors() ) );
        }
    }
}
