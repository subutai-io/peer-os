package org.safehaus.subutai.plugin.hbase.impl.handler;


import org.safehaus.subutai.common.command.Command;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.plugin.hbase.api.HBaseClusterConfig;
import org.safehaus.subutai.plugin.hbase.impl.Commands;
import org.safehaus.subutai.plugin.hbase.impl.HBaseImpl;

import com.google.common.collect.Sets;


/**
 * Created by bahadyr on 8/25/14.
 */
public class StopClusterHandler extends AbstractOperationHandler<HBaseImpl>
{

    private ProductOperation po;
    private String clusterName;


    public StopClusterHandler( final HBaseImpl manager, final String clusterName )
    {
        super( manager, clusterName );
        this.clusterName = clusterName;
        po = manager.getTracker().createProductOperation( HBaseClusterConfig.PRODUCT_KEY,
                String.format( "Setting up %s cluster...", clusterName ) );
    }


    @Override
    public void run()
    {
        final ProductOperation po = manager.getTracker().createProductOperation( HBaseClusterConfig.PRODUCT_KEY,
                String.format( "Stopping cluster %s", clusterName ) );
        manager.getExecutor().execute( new Runnable()
        {

            public void run()
            {
                HBaseClusterConfig config = manager.getDbManager().getInfo( HBaseClusterConfig.PRODUCT_KEY, clusterName,
                        HBaseClusterConfig.class );
                if ( config == null )
                {
                    po.addLogFailed(
                            String.format( "Cluster with name %s does not exist. Operation aborted", clusterName ) );
                    return;
                }

                Agent master = manager.getAgentManager().getAgentByHostname( config.getMaster() );
                if ( master == null )
                {
                    po.addLogFailed( String.format( "Master node %s not connected", config.getMaster() ) );
                    return;
                }


                Command stopCommand = Commands.getStopCommand( Sets.newHashSet( master ) );
                manager.getCommandRunner().runCommand( stopCommand );

                if ( stopCommand.hasSucceeded() )
                {
                    po.addLogDone( "Stop success.." );
                }
                else
                {
                    po.addLogFailed( String.format( "Stop failed, %s", stopCommand.getAllErrors() ) );
                }
            }
        } );
    }
}
