package org.safehaus.subutai.plugin.oozie.impl.handler;


import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.plugin.oozie.api.OozieClusterConfig;
import org.safehaus.subutai.plugin.oozie.impl.OozieImpl;


public class StopServerHandler extends AbstractOperationHandler<OozieImpl, OozieClusterConfig>
{

    private final TrackerOperation trackerOperation;


    public StopServerHandler( final OozieImpl manager, final String clusterName )
    {
        super( manager, clusterName );
        trackerOperation = manager.getTracker().createTrackerOperation( OozieClusterConfig.PRODUCT_KEY,
                String.format( "Stopping cluster %s...", clusterName ) );
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
                OozieClusterConfig config = manager.getPluginDAO().getInfo( OozieClusterConfig.PRODUCT_KEY, clusterName,
                        OozieClusterConfig.class );
                if ( config == null )
                {
                    trackerOperation.addLogFailed(
                            String.format( "Cluster with name %s does not exist. Operation aborted", clusterName ) );
                    return;
                }
                Agent serverAgent = config.getServer();
                if ( serverAgent == null )
                {
                    trackerOperation
                            .addLogFailed( String.format( "Server agent %s not connected", config.getServer() ) );
                    return;
                }
                Set<Agent> servers = new HashSet<Agent>();
                servers.add( serverAgent );
                Command stopServiceCommand = manager.getCommands().getStopServerCommand( servers );
                manager.getCommandRunner().runCommand( stopServiceCommand );


                if ( stopServiceCommand.hasCompleted() )
                {
                    trackerOperation.addLog( "Checking status..." );

                    Command checkCommand = manager.getCommands().getStatusServerCommand( servers );
                    manager.getCommandRunner().runCommand( checkCommand );

                    if ( checkCommand.hasCompleted() )
                    {
                        trackerOperation
                                .addLogDone( checkCommand.getResults().get( serverAgent.getUuid() ).getStdOut() );
                    }
                    else
                    {
                        trackerOperation.addLogFailed(
                                String.format( "Failed to check status, %s", checkCommand.getAllErrors() ) );
                    }
                }
                else
                {
                    trackerOperation
                            .addLogFailed( String.format( "Stop failed, %s", stopServiceCommand.getAllErrors() ) );
                }

                //                if ( stopServiceCommand.hasSucceeded() )
                //                {
                //                    productOperation.addLogDone( "Stop succeeded" );
                //                }
                //                else
                //                {
                //                    productOperation
                //                            .addLogFailed( String.format( "Stop failed, %s",
                // stopServiceCommand.getAllErrors() ) );
                //                }
            }
        } );*/
    }
}