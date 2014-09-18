package org.safehaus.subutai.plugin.elasticsearch.impl.handler;

import com.google.common.collect.Sets;
import org.safehaus.subutai.common.command.AgentResult;
import org.safehaus.subutai.common.command.Command;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.plugin.elasticsearch.api.ElasticsearchClusterConfiguration;
import org.safehaus.subutai.plugin.elasticsearch.impl.Commands;
import org.safehaus.subutai.plugin.elasticsearch.impl.ElasticsearchImpl;

import java.util.Map;
import java.util.UUID;

public class CheckNodeOperationHandler extends AbstractOperationHandler<ElasticsearchImpl> {
    private String lxcHostname;
    private String clusterName;


    public CheckNodeOperationHandler( final ElasticsearchImpl manager, final String clusterName, final String agentUUID ) {
        super( manager, clusterName );
        this.lxcHostname = agentUUID;
        this.clusterName = clusterName;
        this.productOperation = manager.getTracker().createProductOperation( ElasticsearchClusterConfiguration.PRODUCT_KEY,
                String.format( "Checking %s cluster...", clusterName ) );
    }


    @Override
    public void run() {

        ElasticsearchClusterConfiguration elasticsearchClusterConfiguration = manager.getCluster( clusterName );
        if ( elasticsearchClusterConfiguration == null ) {
            productOperation.addLogFailed( String.format( "Cluster with name %s does not exist", clusterName ) );
            return;
        }

        final Agent node = manager.getAgentManager().getAgentByHostname( lxcHostname );
        if ( node == null ) {
            productOperation.addLogFailed( "Agent is not connected !");
            return;
        }
        if ( !elasticsearchClusterConfiguration.getNodes().contains( node ) ) {
            productOperation.addLogFailed(
                    String.format( "Agent with hostname %s does not belong to cluster %s", lxcHostname, clusterName ) );
            return;
        }

        Command statusServiceCommand = Commands.getStatusCommand( Sets.newHashSet( node ) );
        manager.getCommandRunner().runCommand( statusServiceCommand );

        if ( statusServiceCommand.hasSucceeded() ) {
            productOperation.addLogDone( "elasticsearch is running" );
        }
        else {
            logStatusResults( productOperation, statusServiceCommand );
        }
    }

    private void logStatusResults( ProductOperation po, Command checkStatusCommand ) {

        String log = "";

        for ( Map.Entry<UUID, AgentResult> e : checkStatusCommand.getResults().entrySet() ) {

            String status = "UNKNOWN";
            if ( e.getValue().getExitCode() == 0 ) {
                status = "elasticsearch is running";
            }
            else if ( e.getValue().getExitCode() == 768 ) {
                status = "elasticsearch is not running";
            }

            log += String.format( "%s\n", status );
        }

        po.addLogDone( log );
    }

}
