package org.safehaus.subutai.plugin.presto.impl.handler;


import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.plugin.presto.api.PrestoClusterConfig;
import org.safehaus.subutai.plugin.presto.impl.PrestoImpl;

import com.google.common.collect.Sets;


public class CheckNodeOperationHandler extends AbstractOperationHandler<PrestoImpl>
{

    private final String hostname;


    public CheckNodeOperationHandler( PrestoImpl manager, String clusterName, String lxcHostname )
    {
        super( manager, clusterName );
        this.hostname = lxcHostname;
        productOperation = manager.getTracker().createProductOperation( PrestoClusterConfig.PRODUCT_KEY,
                String.format( "Checking state of %s in %s", lxcHostname, clusterName ) );
    }


    @Override
    public void run()
    {
        ProductOperation po = productOperation;
        PrestoClusterConfig config = manager.getCluster( clusterName );
        if ( config == null )
        {
            po.addLogFailed( String.format( "Cluster with name %s does not exist", clusterName ) );
            return;
        }

        Agent node = manager.getAgentManager().getAgentByHostname( hostname );
        if ( node == null )
        {
            po.addLogFailed( String.format( "Agent with hostname %s is not connected", hostname ) );
            return;
        }

        if ( !config.getAllNodes().contains( node ) )
        {
            po.addLogFailed( String.format( "Node %s does not belong to this cluster", hostname ) );
            return;
        }

        Command checkNodeCommand = manager.getCommands().getStatusCommand( Sets.newHashSet( node ) );
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
