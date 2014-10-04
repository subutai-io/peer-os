package org.safehaus.subutai.plugin.presto.impl;


import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.Response;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.core.command.api.command.AgentResult;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.core.command.api.command.CommandCallback;
import org.safehaus.subutai.plugin.presto.api.PrestoClusterConfig;

import com.google.common.base.Preconditions;


public class SetupHelper
{

    final ProductOperation po;
    final PrestoImpl manager;
    final PrestoClusterConfig config;


    public SetupHelper( ProductOperation po, PrestoImpl manager, PrestoClusterConfig config )
    {

        Preconditions.checkNotNull( config, "Presto cluster config is null" );
        Preconditions.checkNotNull( po, "Product operation tracker is null" );
        Preconditions.checkNotNull( manager, "Presto manager is null" );

        this.po = po;
        this.manager = manager;
        this.config = config;
    }


    void checkConnected() throws ClusterSetupException
    {

        String hostname = config.getCoordinatorNode().getHostname();
        if ( manager.getAgentManager().getAgentByHostname( hostname ) == null )
        {
            throw new ClusterSetupException( "Coordinator node is not connected" );
        }

        for ( Agent a : config.getWorkers() )
        {
            if ( manager.getAgentManager().getAgentByHostname( a.getHostname() ) == null )
            {
                throw new ClusterSetupException( "Not all worker nodes are connected" );
            }
        }
    }


    public void configureAsCoordinator( Agent agent ) throws ClusterSetupException
    {
        po.addLog( "Configuring coordinator..." );

        Command cmd = manager.getCommands().getSetCoordinatorCommand( agent );
        manager.getCommandRunner().runCommand( cmd );

        if ( cmd.hasSucceeded() )
        {
            po.addLog( "Coordinator configured successfully" );
        }
        else
        {
            throw new ClusterSetupException( "Failed to configure coordinator: " + cmd.getAllErrors() );
        }
    }


    public void configureAsWorker( Set<Agent> set, Agent coordinator ) throws ClusterSetupException
    {
        po.addLog( "Configuring worker(s)..." );

        Command cmd = manager.getCommands().getSetWorkerCommand( coordinator, set );
        manager.getCommandRunner().runCommand( cmd );

        if ( cmd.hasSucceeded() )
        {
            po.addLog( "Workers configured successfully" );
        }
        else
        {
            throw new ClusterSetupException( "Failed to configure workers: " + cmd.getAllErrors() );
        }
    }


    public void startNodes( final Set<Agent> set ) throws ClusterSetupException
    {
        po.addLog( "Starting Presto node(s)..." );
        Command cmd = manager.getCommands().getStartCommand( set );
        final AtomicInteger okCount = new AtomicInteger();
        manager.getCommandRunner().runCommand( cmd, new CommandCallback()
        {
            @Override
            public void onResponse( Response response, AgentResult agentResult, Command command )
            {
                if ( agentResult.getStdOut().toLowerCase().contains( "started" ) )
                {
                    if ( okCount.incrementAndGet() == set.size() )
                    {
                        stop();
                    }
                }
            }
        } );

        if ( okCount.get() == set.size() )
        {
            po.addLogDone( "Presto node(s) started successfully\nDone" );
        }
        else
        {
            throw new ClusterSetupException(
                    String.format( "Failed to start Presto node(s): %s", cmd.getAllErrors() ) );
        }
    }
}
