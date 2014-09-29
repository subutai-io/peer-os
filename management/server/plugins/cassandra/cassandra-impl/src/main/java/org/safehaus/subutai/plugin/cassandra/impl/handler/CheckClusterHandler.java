package org.safehaus.subutai.plugin.cassandra.impl.handler;


import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.core.command.api.command.AgentResult;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.plugin.cassandra.api.CassandraClusterConfig;
import org.safehaus.subutai.plugin.cassandra.impl.CassandraImpl;
import org.safehaus.subutai.plugin.cassandra.impl.Commands;


public class CheckClusterHandler extends AbstractOperationHandler<CassandraImpl>
{

    private static final Logger LOG = Logger.getLogger( CheckClusterHandler.class.getName() );
    private String clusterName;


    public CheckClusterHandler( final CassandraImpl manager, final String clusterName )
    {
        super( manager, clusterName );
        this.clusterName = clusterName;
        this.productOperation = manager.getTracker().createProductOperation( CassandraClusterConfig.PRODUCT_KEY,
                String.format( "Checking all nodes of %s cluster...", clusterName ) );
    }


    @Override
    public void run()
    {
        CassandraClusterConfig config = null;
        config = manager.getPluginDAO().getInfo( CassandraClusterConfig.PRODUCT_KEY.toLowerCase(), clusterName,
                CassandraClusterConfig.class );

        if ( config == null )
        {
            productOperation.addLogFailed(
                    String.format( "Cluster with name %s does not exist. Operation aborted", clusterName ) );
            return;
        }

        Command checkStatusCommand = Commands.getStatusCommand( config.getNodes() );
        manager.getCommandRunner().runCommand( checkStatusCommand );

        if ( checkStatusCommand.hasSucceeded() )
        {
            productOperation.addLogDone( "All nodes are running." );
        }
        else
        {
            logStatusResults( productOperation, checkStatusCommand );
        }
    }


    private void logStatusResults( ProductOperation po, Command checkStatusCommand )
    {

        StringBuilder log = new StringBuilder();

        for ( Map.Entry<UUID, AgentResult> e : checkStatusCommand.getResults().entrySet() )
        {

            String status = "UNKNOWN";
            if ( e.getValue().getExitCode() == 0 )
            {
                status = "Cassandra is running";
            }
            else if ( e.getValue().getExitCode() == 768 )
            {
                status = "Cassandra is not running";
            }

            log.append( String.format( "- %s: %s\n", e.getValue().getAgentUUID(), status ) );
        }

        po.addLogDone( log.toString() );
    }
}
