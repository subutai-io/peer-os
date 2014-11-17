package org.safehaus.subutai.plugin.oozie.impl.handler;


import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.plugin.oozie.api.OozieClusterConfig;
import org.safehaus.subutai.plugin.oozie.impl.OozieImpl;


public class CheckServerHandler extends AbstractOperationHandler<OozieImpl, OozieClusterConfig>
{

    private static final Logger logger = Logger.getLogger( CheckServerHandler.class.getName() );
    private final TrackerOperation trackerOperation;
    private String clusterName;


    public CheckServerHandler( final OozieImpl manager, final String clusterName )
    {
        super( manager, clusterName );
        this.clusterName = clusterName;
        trackerOperation = manager.getTracker().createTrackerOperation( OozieClusterConfig.PRODUCT_KEY,
                String.format( "Checking status of cluster %s", clusterName ) );
    }


    @Override
    public UUID getTrackerId()
    {
        return trackerOperation.getId();
    }


    @Override
    public void run()
    {

        /*manager.getExecutor().execute( new Runnable()
        {

            public void run()
            {
                OozieClusterConfig config = null;
                config = manager.getPluginDAO()
                                .getInfo( OozieClusterConfig.PRODUCT_KEY, clusterName, OozieClusterConfig.class );

                Agent serverAgent = config.getServer();
                if ( serverAgent == null )
                {
                    trackerOperation
                            .addLogFailed( String.format( "Server agent %s not connected", config.getServer() ) );
                    return;
                }
                Set<Agent> servers = new HashSet<Agent>();
                servers.add( serverAgent );
                Command statusServiceCommand = manager.getCommands().getStatusServerCommand( servers );
                manager.getCommandRunner().runCommand( statusServiceCommand );

                if ( statusServiceCommand.hasCompleted() )
                {

                    trackerOperation
                            .addLogDone( statusServiceCommand.getResults().get( serverAgent.getUuid() ).getStdOut() );
                }
                else
                {
                    trackerOperation.addLogFailed(
                            String.format( "Failed to check status, %s", statusServiceCommand.getAllErrors() ) );
                }
            }
        } );*/
    }
}
