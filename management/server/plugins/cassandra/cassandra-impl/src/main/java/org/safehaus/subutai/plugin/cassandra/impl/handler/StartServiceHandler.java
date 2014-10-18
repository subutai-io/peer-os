package org.safehaus.subutai.plugin.cassandra.impl.handler;


import java.util.UUID;

import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.command.api.command.AgentResult;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.plugin.cassandra.api.CassandraClusterConfig;
import org.safehaus.subutai.plugin.cassandra.impl.CassandraImpl;

import com.google.common.collect.Sets;


public class StartServiceHandler extends AbstractOperationHandler<CassandraImpl>
{

    private String clusterName;
    private String lxcHostname;


    public StartServiceHandler( final CassandraImpl manager, final String clusterName, final String lxcHostname )
    {
        super( manager, clusterName );
        this.lxcHostname = lxcHostname;
        this.clusterName = clusterName;
        productOperation = manager.getTracker().createProductOperation( CassandraClusterConfig.PRODUCT_KEY,
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
        if ( !cassandraConfig.getNodes().contains( UUID.fromString( node.getUuid().toString() ) ) )
        {
            productOperation.addLogFailed(
                    String.format( "Agent with hostname %s does not belong to cluster %s", lxcHostname, clusterName ) );
            return;
        }

        Command startCommand = manager.getCommands().getStartCommand( Sets.newHashSet( node ) );
        manager.getCommandRunner().runCommand( startCommand );

        if ( startCommand.hasSucceeded() )
        {
            AgentResult ar = startCommand.getResults().get( node.getUuid() );
            if ( ar.getStdOut().contains( "starting Cassandra ..." ) || ar.getStdOut()
                                                                          .contains( "is already running..." ) )
            {
                productOperation.addLog( ar.getStdOut() );
                productOperation.addLogDone( "Start succeeded" );
            }
        }
        else
        {
            productOperation.addLogFailed( String.format( "Start failed, %s", startCommand.getAllErrors() ) );
        }
    }
}