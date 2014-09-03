package org.safehaus.subutai.impl.zookeeper.handler;


import com.google.common.collect.Sets;
import org.safehaus.subutai.core.command.api.AgentResult;
import org.safehaus.subutai.core.command.api.Command;
import org.safehaus.subutai.core.command.api.CommandCallback;
import org.safehaus.subutai.api.zookeeper.Config;
import org.safehaus.subutai.impl.zookeeper.Commands;
import org.safehaus.subutai.impl.zookeeper.ZookeeperImpl;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.Response;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * Created by dilshat on 5/7/14.
 */
public class StartNodeOperationHandler extends AbstractOperationHandler<ZookeeperImpl>
{
    private final ProductOperation po;
    private final String lxcHostname;


    public StartNodeOperationHandler( ZookeeperImpl manager, String clusterName, String lxcHostname )
    {
        super( manager, clusterName );
        this.lxcHostname = lxcHostname;
        po = manager.getTracker().createProductOperation( Config.PRODUCT_KEY,
            String.format( "Starting node %s in %s", lxcHostname, clusterName ) );
    }


    @Override
    public UUID getTrackerId()
    {
        return po.getId();
    }


    @Override
    public void run()
    {
        Config config = manager.getCluster( clusterName );
        if ( config == null )
        {
            po.addLogFailed( String.format( "Cluster with name %s does not exist\nOperation aborted", clusterName ) );
            return;
        }

        final Agent node = manager.getAgentManager().getAgentByHostname( lxcHostname );
        if ( node == null )
        {
            po.addLogFailed(
                String.format( "Agent with hostname %s is not connected\nOperation aborted", lxcHostname ) );
            return;
        }

        if ( !config.getNodes().contains( node ) )
        {
            po.addLogFailed(
                String.format( "Agent with hostname %s does not belong to cluster %s", lxcHostname, clusterName ) );
            return;
        }

        po.addLog( "Starting node..." );

        Command startCommand = Commands.getStartCommand( Sets.newHashSet( node ) );
        final AtomicBoolean ok = new AtomicBoolean();
        manager.getCommandRunner().runCommand( startCommand, new CommandCallback()
        {
            @Override
            public void onResponse( Response response, AgentResult agentResult, Command command )
            {
                if ( agentResult.getStdOut().contains( "STARTED" ) )
                {
                    ok.set( true );
                    stop();
                }
            }
        } );

        if ( ok.get() )
        {
            po.addLogDone( String.format( "Node on %s started", lxcHostname ) );
        }
        else
        {
            po.addLogFailed( String.format( "Failed to start node %s. %s",
                lxcHostname, startCommand.getAllErrors()
            ) );
        }
    }
}
