package org.safehaus.subutai.plugin.storm.impl.handler;


import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.exception.CommandException;
import org.safehaus.subutai.common.protocol.CommandResult;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.common.protocol.RequestBuilder;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.storm.api.StormClusterConfiguration;
import org.safehaus.subutai.plugin.storm.impl.CommandType;
import org.safehaus.subutai.plugin.storm.impl.Commands;
import org.safehaus.subutai.plugin.storm.impl.StormImpl;
import org.safehaus.subutai.plugin.storm.impl.StormService;


public class StatusHandler extends AbstractHandler
{

    private final String hostname;


    public StatusHandler( StormImpl manager, String clusterName, String hostname )
    {
        super( manager, clusterName );
        this.hostname = hostname;
        this.trackerOperation =
                manager.getTracker().createTrackerOperation( StormClusterConfiguration.PRODUCT_NAME, "Check status of " + hostname );
    }


    @Override
    public void run()
    {
        TrackerOperation po = trackerOperation;
        StormClusterConfiguration config = manager.getCluster( clusterName );
        if ( config == null )
        {
            po.addLogFailed( String.format( "Cluster '%s' does not exist", clusterName ) );
            return;
        }

        Environment environment =
                manager.getEnvironmentManager().getEnvironmentByUUID( config.getEnvironmentId() );
        ContainerHost containerHost = environment.getContainerHostByHostname( hostname );
        UUID containerHostId = containerHost.getId();

        if ( containerHostId == null )
        {
            po.addLogFailed( hostname + " is not connected" );
            return;
        }
        Set<UUID> set = new HashSet<>( 2 );
        set.add( containerHostId );

        StormService[] services =
                isNimbusNode( config, hostname ) ? new StormService[] { StormService.NIMBUS, StormService.UI } :
                new StormService[] { StormService.SUPERVISOR };
        boolean result = true;
        for ( StormService service : services )
        {
            String s = Commands.make( CommandType.STATUS, service );
            Iterator<UUID> iterator = set.iterator();

            while( iterator.hasNext() ) {
                ContainerHost stormNode = environment.getContainerHostByUUID( iterator.next() );
                try
                {
                    CommandResult commandResult = stormNode.execute( new RequestBuilder( s ).withTimeout( 60 ) );
                    po.addLog( String.format( "Storm %s %s status on %s", service, commandResult.hasSucceeded() ? "" : "not",
                            stormNode.getHostname() ) );
                }
                catch ( CommandException e )
                {
                    po.addLogFailed("Could not run command " + s + ": " + e);
                    e.printStackTrace();
                }
            }
        }
        if ( result )
        {
            po.addLogDone( null );
        }
        else
        {
            po.addLogFailed( null );
        }
    }
}
