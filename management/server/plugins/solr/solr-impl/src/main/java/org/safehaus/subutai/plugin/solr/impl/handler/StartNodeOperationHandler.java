package org.safehaus.subutai.plugin.solr.impl.handler;


import java.util.concurrent.atomic.AtomicBoolean;

import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.Response;
import org.safehaus.subutai.core.command.api.command.AgentResult;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.core.command.api.command.CommandCallback;
import org.safehaus.subutai.plugin.solr.api.SolrClusterConfig;
import org.safehaus.subutai.plugin.solr.impl.SolrImpl;


public class StartNodeOperationHandler extends AbstractOperationHandler<SolrImpl>
{
    private final String lxcHostname;


    public StartNodeOperationHandler( SolrImpl manager, String clusterName, String lxcHostname )
    {
        super( manager, clusterName );
        this.lxcHostname = lxcHostname;
        productOperation = manager.getTracker().createProductOperation( SolrClusterConfig.PRODUCT_KEY,
                String.format( "Starting node %s in %s", lxcHostname, clusterName ) );
    }


    @Override
    public void run()
    {
        SolrClusterConfig solrClusterConfig = manager.getCluster( clusterName );

        if ( solrClusterConfig == null )
        {
            productOperation.addLogFailed( String.format( "Installation with name %s does not exist", clusterName ) );
            return;
        }

        Agent node = manager.getAgentManager().getAgentByHostname( lxcHostname );

        if ( node == null )
        {
            productOperation.addLogFailed( String.format( "Agent with hostname %s is not connected", lxcHostname ) );
            return;
        }

        if ( !solrClusterConfig.getNodes().contains( node ) )
        {
            productOperation.addLogFailed(
                    String.format( "Agent with hostname %s does not belong to cluster %s", lxcHostname, clusterName ) );
            return;
        }


        Command startNodeCommand = manager.getCommands().getStartCommand( node );
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
            productOperation.addLogDone( String.format( "Node %s started", node.getHostname() ) );
        }
        else
        {
            productOperation.addLogFailed( String.format( "Starting node %s failed, %s", node.getHostname(),
                    startNodeCommand.getAllErrors() ) );
        }

        //
        //        productOperation.addLog( "Starting node..." );
        //
        //        Command startCommand = manager.getCommands().getStartCommand( node );
        //        manager.getCommandRunner().runCommand( startCommand );
        //        Command statusCommand = manager.getCommands().getStatusCommand( node );
        //        manager.getCommandRunner().runCommand( statusCommand );
        //        AgentResult result = statusCommand.getResults().get( node.getUuid() );
        //        NodeState nodeState = NodeState.UNKNOWN;
        //
        //        if ( result != null )
        //        {
        //            if ( result.getStdOut().contains( "is running" ) )
        //            {
        //                nodeState = NodeState.RUNNING;
        //            }
        //            else if ( result.getStdOut().contains( "is not running" ) )
        //            {
        //                nodeState = NodeState.STOPPED;
        //            }
        //        }
        //
        //        if ( NodeState.RUNNING.equals( nodeState ) )
        //        {
        //            productOperation.addLogDone( String.format( "Node on %s started", lxcHostname ) );
        //        }
        //        else
        //        {
        //            productOperation.addLogFailed(
        //                    String.format( "Failed to start node %s. %s", lxcHostname,
        // startCommand.getAllErrors() ) );
        //        }
    }
}
