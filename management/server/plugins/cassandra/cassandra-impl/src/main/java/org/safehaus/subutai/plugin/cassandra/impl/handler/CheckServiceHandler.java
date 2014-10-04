package org.safehaus.subutai.plugin.cassandra.impl.handler;


import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.command.api.command.AgentResult;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.plugin.cassandra.api.CassandraClusterConfig;
import org.safehaus.subutai.plugin.cassandra.impl.CassandraImpl;

import com.google.common.collect.Sets;


public class CheckServiceHandler extends AbstractOperationHandler<CassandraImpl>
{

    private String lxcHostname;
    private String clusterName;


    public CheckServiceHandler( final CassandraImpl manager, final String clusterName, final String agentUUID )
    {
        super( manager, clusterName );
        this.lxcHostname = agentUUID;
        this.clusterName = clusterName;
        this.productOperation = manager.getTracker().createProductOperation( CassandraClusterConfig.PRODUCT_KEY,
                String.format( "Starting %s cluster...", clusterName ) );
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

        Agent agent = manager.getAgentManager().getAgentByHostname( lxcHostname );
        Command statusServiceCommand = manager.getCommands().getStatusCommand( Sets.newHashSet( agent ) );
        manager.getCommandRunner().runCommand( statusServiceCommand );
        if ( statusServiceCommand.hasSucceeded() )
        {
            AgentResult ar = statusServiceCommand.getResults().get( agent.getUuid() );
            if ( ar.getStdOut().contains( "is running" ) )
            {
                productOperation.addLogDone( "Cassandra is running" );
            }
            else
            {
                productOperation.addLogFailed( "Cassandra is not running" );
            }
        }
        else
        {
            productOperation.addLogFailed( "Cassandra is not running" );
        }
    }
}