package org.safehaus.subutai.plugin.cassandra.impl.handler;


import java.util.Map;
import java.util.UUID;

import org.safehaus.subutai.common.command.AgentResult;
import org.safehaus.subutai.common.command.Command;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.plugin.cassandra.api.CassandraClusterConfig;
import org.safehaus.subutai.plugin.cassandra.impl.CassandraImpl;
import org.safehaus.subutai.plugin.cassandra.impl.Commands;


public class CheckNodeHandler extends AbstractOperationHandler<CassandraImpl>
{

    private String clusterName;
    private String lxcHostname;


    public CheckNodeHandler( final CassandraImpl manager, String clusterName, String lxcHostname )
    {
        super( manager, clusterName );
        this.clusterName = clusterName;
        this.lxcHostname = lxcHostname;
        productOperation = manager.getTracker().createProductOperation( CassandraClusterConfig.PRODUCT_KEY,
                String.format( "Checking cassandra on %s of %s cluster...", lxcHostname, clusterName ) );
    }


    @Override
    public void run()
    {
        CassandraClusterConfig cassandraConfig = manager.getCluster( clusterName );
        if ( cassandraConfig == null )
        {
            productOperation.addLogFailed( String.format( "Cluster with name %s does not exist", clusterName ) );
            return;
        }

        final Agent node = manager.getAgentManager().getAgentByHostname( lxcHostname );
        if ( node == null )
        {
            productOperation.addLogFailed( String.format( "Agent with hostname %s is not connected", lxcHostname ) );
            return;
        }
        if ( !cassandraConfig.getNodes().contains( node ) )
        {
            productOperation.addLogFailed(
                    String.format( "Agent with hostname %s does not belong to cluster %s", lxcHostname, clusterName ) );
            return;
        }

        Command checkNodeCommand = Commands.getStatusCommand( node );
        manager.getCommandRunner().runCommand( checkNodeCommand );

        if ( checkNodeCommand.hasSucceeded() )
        {
            productOperation.addLogDone( String.format( "Status on %s is %s", lxcHostname,
                    checkNodeCommand.getResults().get( node.getUuid() ).getStdOut() ) );
        }
        else
        {
            logStatusResults( productOperation, checkNodeCommand );
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
                status = "Cassandra is running";
            }
            else if ( e.getValue().getExitCode() == 768 )
            {
                status = "Cassandra is not running";
            }

            log.append( String.format( "- %s: %s\n", e.getValue().getAgentUUID(), status ) );
        }

        po.addLogDone( log.toString() );
    }
}
