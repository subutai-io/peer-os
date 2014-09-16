package org.safehaus.subutai.plugin.elasticsearch.impl.handler;

import com.google.common.collect.Sets;
import org.safehaus.subutai.common.command.AgentResult;
import org.safehaus.subutai.common.command.Command;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.plugin.elasticsearch.api.ElasticsearchClusterConfiguration;
import org.safehaus.subutai.plugin.elasticsearch.impl.Commands;
import org.safehaus.subutai.plugin.elasticsearch.impl.ElasticsearchImpl;

public class CheckNodeOperationHandler extends AbstractOperationHandler<ElasticsearchImpl> {
    private String lxcHostname;
    private String clusterName;


    public CheckNodeOperationHandler( final ElasticsearchImpl manager, final String clusterName, final String agentUUID ) {
        super( manager, clusterName );
        this.lxcHostname = agentUUID;
        this.clusterName = clusterName;
        this.productOperation = manager.getTracker().createProductOperation( ElasticsearchClusterConfiguration.PRODUCT_KEY,
                String.format( "Starting %s cluster...", clusterName ) );
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
            productOperation.addLogFailed( String.format( "Agent with hostname %s is not connected", lxcHostname ) );
            return;
        }
        if ( !elasticsearchClusterConfiguration.getNodes().contains( node ) ) {
            productOperation.addLogFailed(
                    String.format( "Agent with hostname %s does not belong to cluster %s", lxcHostname, clusterName ) );
            return;
        }

        Agent agent = manager.getAgentManager().getAgentByHostname( lxcHostname );
        Command statusServiceCommand = Commands.getStatusCommand( Sets.newHashSet( agent ) );
        manager.getCommandRunner().runCommand( statusServiceCommand );
        if ( statusServiceCommand.hasSucceeded() ) {
            AgentResult ar = statusServiceCommand.getResults().get( agent.getUuid() );
            if ( ar.getStdOut().contains( "is running" ) ) {
                productOperation.addLog( "elasticsearch is running" );
            }
            else {
                productOperation.addLogFailed( "elasticsearch is not running" );
            }
        }
        else {
            productOperation.addLogFailed( "elasticsearch is not running" );
        }
    }
}
