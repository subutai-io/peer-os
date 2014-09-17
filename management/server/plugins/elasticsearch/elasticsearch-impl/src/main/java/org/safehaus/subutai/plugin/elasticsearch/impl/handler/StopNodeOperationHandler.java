package org.safehaus.subutai.plugin.elasticsearch.impl.handler;

import com.google.common.collect.Sets;
import org.safehaus.subutai.common.command.AgentResult;
import org.safehaus.subutai.common.command.Command;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.plugin.elasticsearch.api.ElasticsearchClusterConfiguration;
import org.safehaus.subutai.plugin.elasticsearch.impl.Commands;
import org.safehaus.subutai.plugin.elasticsearch.impl.ElasticsearchImpl;

public class StopNodeOperationHandler extends AbstractOperationHandler<ElasticsearchImpl> {
    private String lxcHostname;
    private String clusterName;


    public StopNodeOperationHandler( final ElasticsearchImpl manager, final String clusterName, final String lxcHostname ) {
        super( manager, clusterName );
        this.lxcHostname = lxcHostname;
        this.clusterName = clusterName;
        productOperation = manager.getTracker().createProductOperation( ElasticsearchClusterConfiguration.PRODUCT_KEY,
                String.format( "Stopping %s cluster...", clusterName ) );
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


        Command stopServiceCommand = Commands.getStopCommand( Sets.newHashSet( node ) );
        manager.getCommandRunner().runCommand( stopServiceCommand );
        if ( stopServiceCommand.hasSucceeded() ) {
            AgentResult ar = stopServiceCommand.getResults().get( node.getUuid() );
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
