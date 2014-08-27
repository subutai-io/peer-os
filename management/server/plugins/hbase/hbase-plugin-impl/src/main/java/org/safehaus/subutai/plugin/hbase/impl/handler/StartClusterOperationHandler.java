package org.safehaus.subutai.plugin.hbase.impl.handler;


import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.plugin.hbase.api.HBaseConfig;
import org.safehaus.subutai.plugin.hbase.impl.Commands;
import org.safehaus.subutai.plugin.hbase.impl.HBaseImpl;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.Agent;

import com.google.common.collect.Sets;


/**
 * Created by bahadyr on 8/25/14.
 */
public class StartClusterOperationHandler extends AbstractOperationHandler<HBaseImpl> {

    private ProductOperation po;

    private String clusterName;


    public StartClusterOperationHandler( final HBaseImpl manager, final String clusterName ) {
        super( manager, clusterName );
        this.clusterName = clusterName;
        po = manager.getTracker().createProductOperation( HBaseConfig.PRODUCT_KEY,
                String.format( "Starting %s cluster...", clusterName ) );
    }


    @Override
    public void run() {
        final ProductOperation po = manager.getTracker().createProductOperation( HBaseConfig.PRODUCT_KEY,
                String.format( "Starting cluster %s", clusterName ) );
        manager.getExecutor().execute( new Runnable() {

            public void run() {
                HBaseConfig config =
                        manager.getDbManager().getInfo( HBaseConfig.PRODUCT_KEY, clusterName, HBaseConfig.class );
                if ( config == null ) {
                    po.addLogFailed(
                            String.format( "Cluster with name %s does not exist\nOperation aborted", clusterName ) );
                    return;
                }

                Agent master = manager.getAgentManager().getAgentByHostname( config.getMaster() );
                if ( master == null ) {
                    po.addLogFailed( String.format( "Master node %s not connected", config.getMaster() ) );
                    return;
                }

                Command startCommand = Commands.getStartCommand( Sets.newHashSet( master ) );
                manager.getCommandRunner().runCommand( startCommand );

                if ( startCommand.hasSucceeded() ) {
                    po.addLogDone( "Start success.." );
                }
                else {
                    po.addLogFailed( String.format( "Start failed, %s", startCommand.getAllErrors() ) );
                }
            }
        } );
    }
}
