package org.safehaus.subutai.plugin.cassandra.impl.handler;


import java.util.Map;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.core.command.api.command.AgentResult;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.plugin.cassandra.api.CassandraClusterConfig;
import org.safehaus.subutai.plugin.cassandra.impl.CassandraImpl;
import org.safehaus.subutai.plugin.cassandra.impl.Commands;

import com.google.common.collect.Sets;


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
        CassandraClusterConfig cassandraClusterConfig = manager.getCluster( clusterName );
        if ( cassandraClusterConfig == null )
        {
            productOperation.addLogFailed( String.format( "Cluster with name %s does not exist", clusterName ) );
            return;
        }

        final Agent node = manager.getAgentManager().getAgentByHostname( lxcHostname );
        if ( node == null )
        {
            productOperation.addLogFailed( "Agent is not connected !" );
            return;
        }
        if ( !cassandraClusterConfig.getNodes().contains( node ) )
        {
            productOperation.addLogFailed(
                    String.format( "Agent with hostname %s does not belong to cluster %s", lxcHostname, clusterName ) );
            return;
        }

        Command statusServiceCommand = Commands.getStatusCommand( Sets.newHashSet( node ) );
        manager.getCommandRunner().runCommand( statusServiceCommand );

        if ( statusServiceCommand.hasSucceeded() )
        {
            productOperation.addLogDone( "Cassandra is running" );
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
                status = "Cassandra is running";
            }
            else if ( e.getValue().getExitCode() == 768 )
            {
                status = "Cassandra is not running";
            }

            log.append( String.format( "%s", status ) );
        }
        po.addLogDone( log.toString() );
    }
}
