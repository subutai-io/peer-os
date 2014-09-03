package org.safehaus.subutai.plugin.solr.impl.handler;


import org.safehaus.subutai.core.commandrunner.api.AgentResult;
import org.safehaus.subutai.core.commandrunner.api.Command;
import org.safehaus.subutai.plugin.solr.api.SolrClusterConfig;
import org.safehaus.subutai.plugin.solr.impl.SolrImpl;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.enums.NodeState;


public class CheckNodeOperationHandler extends AbstractOperationHandler<SolrImpl> {
    private final String lxcHostname;


    public CheckNodeOperationHandler( SolrImpl manager, String clusterName, String lxcHostname ) {
        super( manager, clusterName );
        this.lxcHostname = lxcHostname;
        productOperation = manager.getTracker().createProductOperation( SolrClusterConfig.PRODUCT_KEY,
                String.format( "Checking node %s in %s", lxcHostname, clusterName ) );
    }


    @Override
    public void run() {
        SolrClusterConfig solrClusterConfig = manager.getCluster( clusterName );

        if ( solrClusterConfig == null ) {
            productOperation.addLogFailed(
                    String.format( "Installation with name %s does not exist", clusterName ) );
            return;
        }

        Agent node = manager.getAgentManager().getAgentByHostname( lxcHostname );

        if ( node == null ) {
            productOperation.addLogFailed(
                    String.format( "Agent with hostname %s is not connected", lxcHostname ) );
            return;
        }

        if ( !solrClusterConfig.getNodes().contains( node ) ) {
            productOperation.addLogFailed(
                    String.format( "Agent with hostname %s does not belong to installation %s", lxcHostname, clusterName ) );
            return;
        }

        productOperation.addLog( "Checking node..." );

        Command checkNodeCommand = manager.getCommands().getStatusCommand( node );
        manager.getCommandRunner().runCommand( checkNodeCommand );

        NodeState nodeState = NodeState.UNKNOWN;

        if ( checkNodeCommand.hasCompleted() ) {
            AgentResult result = checkNodeCommand.getResults().get( node.getUuid() );

            if ( result.getStdOut().contains( "is running" ) ) {
                nodeState = NodeState.RUNNING;
            }
            else if ( result.getStdOut().contains( "is not running" ) ) {
                nodeState = NodeState.STOPPED;
            }
        }

        if ( NodeState.UNKNOWN.equals( nodeState ) ) {
            productOperation.addLogFailed(
                    String.format( "Failed to check status of %s, %s", lxcHostname, checkNodeCommand.getAllErrors() ) );
        }
        else {
            productOperation.addLogDone( String.format( "Node %s is %s", lxcHostname, nodeState ) );
        }
    }
}
