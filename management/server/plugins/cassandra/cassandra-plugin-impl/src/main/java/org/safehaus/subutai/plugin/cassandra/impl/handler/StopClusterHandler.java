package org.safehaus.subutai.plugin.cassandra.impl.handler;


import org.safehaus.subutai.common.command.Command;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.plugin.cassandra.api.CassandraClusterConfig;
import org.safehaus.subutai.plugin.cassandra.impl.CassandraImpl;
import org.safehaus.subutai.plugin.cassandra.impl.Commands;


public class StopClusterHandler extends AbstractOperationHandler<CassandraImpl>
{

    private String clusterName;


    public StopClusterHandler( final CassandraImpl manager, final String clusterName )
    {
        super( manager, clusterName );
        this.clusterName = clusterName;
        productOperation = manager.getTracker().createProductOperation( CassandraClusterConfig.PRODUCT_KEY,
                String.format( "Setting up %s cluster...", clusterName ) );
    }


    @Override
    public void run()
    {
        CassandraClusterConfig config = manager.getCluster( clusterName );
        if ( config == null )
        {
            productOperation.addLogFailed(
                    String.format( "Cluster with name %s does not exist\nOperation aborted", clusterName ) );
            return;
        }

        Command stopServiceCommand = Commands.getStopCommand( config.getNodes() );
        manager.getCommandRunner().runCommand( stopServiceCommand );

        if ( stopServiceCommand.hasSucceeded() )
        {
            productOperation.addLogDone( "Stop succeeded" );
        }
        else
        {
            productOperation.addLogFailed( String.format( "Start failed, %s", stopServiceCommand.getAllErrors() ) );
        }
    }
}
