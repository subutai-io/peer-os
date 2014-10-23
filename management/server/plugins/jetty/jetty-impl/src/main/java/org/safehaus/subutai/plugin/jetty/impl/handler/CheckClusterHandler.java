package org.safehaus.subutai.plugin.jetty.impl.handler;


import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.command.api.command.AgentResult;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.plugin.jetty.api.JettyConfig;
import org.safehaus.subutai.plugin.jetty.impl.JettyImpl;


public class CheckClusterHandler extends AbstractOperationHandler<JettyImpl>
{

    private static final Logger LOG = Logger.getLogger( CheckClusterHandler.class.getName() );
    private String clusterName;


    public CheckClusterHandler( final JettyImpl manager, final String clusterName )
    {
        super( manager, clusterName );
        this.clusterName = clusterName;
        this.trackerOperation = manager.getTracker().createTrackerOperation( JettyConfig.PRODUCT_KEY,
                String.format( "Checking all nodes of %s cluster...", clusterName ) );
    }


    @Override
    public void run()
    {
        JettyConfig config = null;
        config = manager.getPluginDAO().getInfo( JettyConfig.PRODUCT_KEY.toLowerCase(), clusterName,
                JettyConfig.class );

        if ( config == null )
        {
            trackerOperation.addLogFailed(
                    String.format( "Cluster with name %s does not exist. Operation aborted", clusterName ) );
            return;
        }

        Command checkStatusCommand = manager.getCommands().getStatusCommand( config.getNodes() );
        manager.getCommandRunner().runCommand( checkStatusCommand );

        if ( checkStatusCommand.hasSucceeded() )
        {
            trackerOperation.addLogDone( "All nodes are running." );
        }
        else
        {
            logStatusResults( trackerOperation, checkStatusCommand );
        }
    }


    private void logStatusResults( TrackerOperation po, Command checkStatusCommand )
    {

        StringBuilder log = new StringBuilder();

        for ( Map.Entry<UUID, AgentResult> e : checkStatusCommand.getResults().entrySet() )
        {

            String status = "UNKNOWN";
            if ( e.getValue().getExitCode() == 0 )
            {
                status = "Jetty is running";
            }
            else if ( e.getValue().getExitCode() == 768 )
            {
                status = "Jetty is not running";
            }

            log.append( String.format( "- %s: %s\n", e.getValue().getAgentUUID(), status ) );
        }

        po.addLogDone( log.toString() );
    }
}
