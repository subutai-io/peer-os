package org.safehaus.subutai.plugin.hbase.impl.handler;


import org.safehaus.subutai.common.command.Command;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.plugin.hbase.api.HBaseClusterConfig;
import org.safehaus.subutai.plugin.hbase.impl.Commands;
import org.safehaus.subutai.plugin.hbase.impl.HBaseImpl;

import com.google.common.collect.Sets;


public class StartClusterHandler extends AbstractOperationHandler<HBaseImpl>
{

    private String clusterName;


    public StartClusterHandler( final HBaseImpl manager, final String clusterName )
    {
        super( manager, clusterName );
        this.clusterName = clusterName;
        productOperation = manager.getTracker().createProductOperation( HBaseClusterConfig.PRODUCT_KEY,
                String.format( "Starting %s cluster...", clusterName ) );
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

        Agent master = config.getHbaseMaster();
        if ( master == null )
        {
            productOperation.addLogFailed( String.format( "Master node %s not connected", config.getHbaseMaster() ) );
            return;
        }

        Command startCommand = Commands.getStartCommand( Sets.newHashSet( master ) );
        manager.getCommandRunner().runCommand( startCommand );

        if ( startCommand.hasSucceeded() )
        {
            productOperation.addLogDone( "Start success.." );
        }
        else
        {
            productOperation.addLogFailed( String.format( "Start failed, %s", startCommand.getAllErrors() ) );
        }
    }
}
