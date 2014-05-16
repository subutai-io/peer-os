package org.safehaus.kiskis.mgmt.impl.solr.handler;


import org.safehaus.kiskis.mgmt.api.commandrunner.AgentResult;
import org.safehaus.kiskis.mgmt.api.commandrunner.Command;
import org.safehaus.kiskis.mgmt.api.solr.Config;
import org.safehaus.kiskis.mgmt.impl.solr.SolrImpl;
import org.safehaus.kiskis.mgmt.shared.operation.AbstractOperationHandler;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.NodeState;


public class StartNodeOperationHandler extends AbstractOperationHandler<SolrImpl> {
    private final String lxcHostname;


    public StartNodeOperationHandler( SolrImpl manager, String clusterName, String lxcHostname ) {
        super( manager, clusterName );
        this.lxcHostname = lxcHostname;
        productOperation = manager.getTracker().createProductOperation( Config.PRODUCT_KEY,
                String.format( "Starting node %s in %s", lxcHostname, clusterName ) );
    }


    @Override
    public void run() {
        Config config = manager.getCluster( clusterName );

        if ( config == null ) {
            productOperation.addLogFailed(
                    String.format( "Cluster with name %s does not exist\nOperation aborted", clusterName ) );
            return;
        }

        Agent node = manager.getAgentManager().getAgentByHostname( lxcHostname );

        if ( node == null ) {
            productOperation.addLogFailed(
                    String.format( "Agent with hostname %s is not connected\nOperation aborted", lxcHostname ) );
            return;
        }

        if ( !config.getNodes().contains( node ) ) {
            productOperation.addLogFailed(
                    String.format( "Agent with hostname %s does not belong to cluster %s", lxcHostname, clusterName ) );
            return;
        }

        productOperation.addLog( "Starting node..." );

        Command startCommand = manager.getCommands().getStartCommand( node );
        manager.getCommandRunner().runCommand( startCommand );
        Command statusCommand = manager.getCommands().getStatusCommand( node );
        manager.getCommandRunner().runCommand( statusCommand );
        AgentResult result = statusCommand.getResults().get( node.getUuid() );
        NodeState nodeState = NodeState.UNKNOWN;

        if ( result != null ) {
            if ( result.getStdOut().contains( "is running" ) ) {
                nodeState = NodeState.RUNNING;
            }
            else if ( result.getStdOut().contains( "is not running" ) ) {
                nodeState = NodeState.STOPPED;
            }
        }

        if ( NodeState.RUNNING.equals( nodeState ) ) {
            productOperation.addLogDone( String.format( "Node on %s started", lxcHostname ) );
        }
        else {
            productOperation.addLogFailed(
                    String.format( "Failed to start node %s. %s", lxcHostname, startCommand.getAllErrors() ) );
        }
    }
}
