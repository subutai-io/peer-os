package org.safehaus.subutai.plugin.presto.impl.handler;


import com.google.common.collect.Sets;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.plugin.presto.api.PrestoClusterConfig;
import org.safehaus.subutai.plugin.presto.impl.Commands;
import org.safehaus.subutai.plugin.presto.impl.PrestoImpl;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.Agent;

import java.util.UUID;


public class CheckNodeOperationHandler extends AbstractOperationHandler<PrestoImpl>
{
    private final ProductOperation po;
    private final String lxcHostname;


    public CheckNodeOperationHandler( PrestoImpl manager, String clusterName, String lxcHostname )
    {
        super( manager, clusterName );
        this.lxcHostname = lxcHostname;
        po = manager.getTracker().createProductOperation( PrestoClusterConfig.PRODUCT_KEY,
            String.format( "Checking state of %s in %s", lxcHostname, clusterName ) );

    }


    @Override
    public UUID getTrackerId()
    {
        return po.getId();
    }


    @Override
    public void run()
    {
        PrestoClusterConfig config = manager.getCluster( clusterName );
        if ( config == null )
        {
            po.addLogFailed( String.format( "Cluster with name %s does not exist", clusterName ) );
            return;
        }

        Agent node = manager.getAgentManager().getAgentByHostname( lxcHostname );
        if ( node == null )
        {
            po.addLogFailed( String.format( "Agent with hostname %s is not connected", lxcHostname ) );
            return;
        }

        if ( !config.getAllNodes().contains( node ) )
        {
            po.addLogFailed( String.format( "Node %s does not belong to this cluster", lxcHostname ) );
            return;
        }

        po.addLog( "Checking node..." );

        Command checkNodeCommand = Commands.getStatusCommand( Sets.newHashSet( node ) );
        manager.getCommandRunner().runCommand( checkNodeCommand );

        if ( checkNodeCommand.hasCompleted() )
        {
            po.addLogDone( String.format( "%s", checkNodeCommand.getResults().get( node.getUuid() ).getStdOut() ) );
        }
        else
        {
            po.addLogFailed( String.format( "Faied to check status, %s", checkNodeCommand.getAllErrors() ) );
        }
    }
}
