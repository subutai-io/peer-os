package org.safehaus.subutai.plugin.hbase.impl.handler;


import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.plugin.hbase.api.HBaseClusterConfig;
import org.safehaus.subutai.plugin.hbase.impl.HBaseImpl;

import com.google.common.collect.Sets;


public class StopClusterHandler extends AbstractOperationHandler<HBaseImpl, HBaseClusterConfig>
{
    private String clusterName;


    public StopClusterHandler( final HBaseImpl manager, final String clusterName )
    {
        super( manager, clusterName );
        this.clusterName = clusterName;
        trackerOperation = manager.getTracker().createTrackerOperation( HBaseClusterConfig.PRODUCT_KEY,
                String.format( "Stopping %s cluster...", clusterName ) );
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

        Agent master = config.getHbaseMaster();
        if ( master == null )
        {
            trackerOperation.addLogFailed( String.format( "Master node %s not connected", config.getHbaseMaster() ) );
            return;
        }


        Command stopCommand = manager.getCommands().getStopCommand( Sets.newHashSet( master ) );
        manager.getCommandRunner().runCommand( stopCommand );

        if ( stopCommand.hasSucceeded() )
        {
            trackerOperation.addLogDone( "Stop success.." );
        }
        else
        {
            trackerOperation.addLogFailed( String.format( "Stop failed, %s", stopCommand.getAllErrors() ) );
        }*/
    }
}
