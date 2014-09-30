package org.safehaus.subutai.plugin.solr.impl.handler;


import java.util.Map;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.core.command.api.command.AgentResult;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.plugin.solr.api.SolrClusterConfig;
import org.safehaus.subutai.plugin.solr.impl.SolrImpl;


public class CheckNodeOperationHandler extends AbstractOperationHandler<SolrImpl>
{
    private final String lxcHostname;


    public CheckNodeOperationHandler( SolrImpl manager, String clusterName, String lxcHostname )
    {
        super( manager, clusterName );
        this.lxcHostname = lxcHostname;
        productOperation = manager.getTracker().createProductOperation( SolrClusterConfig.PRODUCT_KEY,
                String.format( "Checking node %s in %s", lxcHostname, clusterName ) );
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
                    String.format( "Agent with hostname %s does not belong to installation %s", lxcHostname,
                            clusterName ) );
            return;
        }


        Command statusServiceCommand = manager.getCommands().getStatusCommand( node );
        manager.getCommandRunner().runCommand( statusServiceCommand );

        if ( statusServiceCommand.hasSucceeded() )
        {
            productOperation.addLogDone( "Solr is running" );
        }
        else
        {
            logStatusResults( productOperation, statusServiceCommand );
        }
    }


    private void logStatusResults( ProductOperation po, Command checkStatusCommand )
    {

        StringBuilder log = new StringBuilder();

        for ( Map.Entry<UUID, AgentResult> e : checkStatusCommand.getResults().entrySet() )
        {

            String status = "UNKNOWN";
            if ( e.getValue().getExitCode() == 0 )
            {
                status = "Solr is running";
            }
            else if ( e.getValue().getExitCode() == 768 )
            {
                status = "Solr is not running";
            }

            log.append( String.format( "%s\n", status ) ).append( "\n" );
        }

        po.addLogDone( log.toString() );
    }
}
