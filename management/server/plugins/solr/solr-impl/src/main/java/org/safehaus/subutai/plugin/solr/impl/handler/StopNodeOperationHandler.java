package org.safehaus.subutai.plugin.solr.impl.handler;


import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.plugin.solr.api.SolrClusterConfig;
import org.safehaus.subutai.plugin.solr.impl.SolrImpl;


public class StopNodeOperationHandler extends AbstractOperationHandler<SolrImpl>
{
    private final String lxcHostname;


    public StopNodeOperationHandler( SolrImpl manager, String clusterName, String lxcHostname )
    {
        super( manager, clusterName );
        this.lxcHostname = lxcHostname;
        trackerOperation = manager.getTracker().createTrackerOperation( SolrClusterConfig.PRODUCT_KEY,
                String.format( "Stopping node %s in %s", lxcHostname, clusterName ) );
    }


    @Override
    public void run()
    {
        SolrClusterConfig solrClusterConfig = manager.getCluster( clusterName );

        if ( solrClusterConfig == null )
        {
            trackerOperation.addLogFailed( String.format( "Installation with name %s does not exist", clusterName ) );
            return;
        }

        final Agent node = manager.getAgentManager().getAgentByHostname( lxcHostname );

        if ( node == null )
        {
            trackerOperation.addLogFailed( String.format( "Agent with hostname %s is not connected", lxcHostname ) );
            return;
        }

        if ( !solrClusterConfig.getNodes().contains( node ) )
        {
            trackerOperation.addLogFailed(
                    String.format( "Agent with hostname %s does not belong to cluster %s", lxcHostname, clusterName ) );
            return;
        }

        Command stopServiceCommand = manager.getCommands().getStopCommand( node );
        manager.getCommandRunner().runCommand( stopServiceCommand );

        if ( stopServiceCommand.hasSucceeded() )
        {
            trackerOperation.addLogDone( "Stop succeeded" );
        }
        else
        {
            trackerOperation.addLogFailed( String.format( "Stop failed, %s", stopServiceCommand.getAllErrors() ) );
        }

        //        productOperation.addLog( "Stopping node..." );
        //
        //        Command stopCommand = manager.getCommands().getStopCommand( node );
        //        manager.getCommandRunner().runCommand( stopCommand );
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
        //        if ( NodeState.STOPPED.equals( nodeState ) )
        //        {
        //            productOperation.addLogDone( String.format( "Node on %s stopped", lxcHostname ) );
        //        }
        //        else
        //        {
        //            productOperation.addLogFailed(
        //                    String.format( "Failed to stop node %s. %s", lxcHostname, stopCommand.getAllErrors() ) );
        //        }
    }
}
