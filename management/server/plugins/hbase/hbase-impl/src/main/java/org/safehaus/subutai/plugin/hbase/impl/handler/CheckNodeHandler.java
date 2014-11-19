package org.safehaus.subutai.plugin.hbase.impl.handler;


import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.plugin.hbase.api.HBaseConfig;
import org.safehaus.subutai.plugin.hbase.impl.HBaseImpl;


public class CheckNodeHandler extends AbstractOperationHandler<HBaseImpl, HBaseConfig>
{

    private String clusterName, lxcHostname;


    public CheckNodeHandler( final HBaseImpl manager, final String clusterName, final String lxcHostname )
    {
        super( manager, clusterName );
        this.clusterName = clusterName;
        this.lxcHostname = lxcHostname;
        trackerOperation = manager.getTracker().createTrackerOperation( HBaseConfig.PRODUCT_KEY,
                String.format( "Checking %s cluster node %s ...", clusterName, lxcHostname ) );
    }


    @Override
    public void run()
    {
        /*HBaseClusterConfig config = manager.getCluster( clusterName );
        if ( config == null )
        {
            trackerOperation.addLogFailed(
                    String.format( "Cluster with name %s does not exist\nOperation aborted", clusterName ) );
            return;
        }

        Agent node = manager.getAgentManager().getAgentByHostname( lxcHostname );
        if ( node == null )
        {
            trackerOperation.addLogFailed( String.format( "Agent is not connected !" ) );
            return;
        }

        Command checkCommand = manager.getCommands().getStatusCommand( Sets.newHashSet( node ) );
        manager.getCommandRunner().runCommand( checkCommand );

        if ( checkCommand.hasSucceeded() )
        {
            trackerOperation.addLogDone( checkCommand.getResults().get( node.getUuid() ).getStdOut() );
        }
        else
        {
            trackerOperation.addLogFailed( String.format( "Check failed, %s", checkCommand.getAllErrors() ) );
        }*/
    }
}

