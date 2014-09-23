package org.safehaus.subutai.plugin.hbase.impl.handler;


import org.safehaus.subutai.common.command.Command;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.plugin.hbase.api.HBaseClusterConfig;
import org.safehaus.subutai.plugin.hbase.impl.Commands;
import org.safehaus.subutai.plugin.hbase.impl.HBaseImpl;

import com.google.common.collect.Sets;


public class CheckNodeHandler extends AbstractOperationHandler<HBaseImpl>
{

    private String clusterName, lxcHostname;


    public CheckNodeHandler( final HBaseImpl manager, final String clusterName, final String lxcHostname )
    {
        super( manager, clusterName );
        this.clusterName = clusterName;
        this.lxcHostname = lxcHostname;
        productOperation = manager.getTracker().createProductOperation( HBaseClusterConfig.PRODUCT_KEY,
                String.format( "Checking %s cluster node %s ...", clusterName, lxcHostname ) );
    }


    @Override
    public void run()
    {
        HBaseClusterConfig config = manager.getCluster( clusterName );
        if ( config == null )
        {
            productOperation.addLogFailed(
                    String.format( "Cluster with name %s does not exist\nOperation aborted", clusterName ) );
            return;
        }

        Agent node = manager.getAgentManager().getAgentByHostname( lxcHostname );
        if ( node == null )
        {
            productOperation.addLogFailed( String.format( "Agent is not connected !" ) );
            return;
        }

        Command checkCommand = Commands.getStatusCommand( Sets.newHashSet( node ) );
        manager.getCommandRunner().runCommand( checkCommand );

        if ( checkCommand.hasSucceeded() )
        {
            productOperation.addLogDone( checkCommand.getResults().get( node.getUuid() ).getStdOut() );
        }
        else
        {
            productOperation.addLogFailed( String.format( "Check failed, %s", checkCommand.getAllErrors() ) );
        }
    }
}
