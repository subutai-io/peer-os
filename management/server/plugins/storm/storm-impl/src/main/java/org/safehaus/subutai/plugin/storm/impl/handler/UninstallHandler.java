package org.safehaus.subutai.plugin.storm.impl.handler;


import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.core.command.api.command.RequestBuilder;
import org.safehaus.subutai.core.container.api.lxcmanager.LxcDestroyException;
import org.safehaus.subutai.plugin.storm.api.StormConfig;
import org.safehaus.subutai.plugin.storm.impl.CommandType;
import org.safehaus.subutai.plugin.storm.impl.Commands;
import org.safehaus.subutai.plugin.storm.impl.StormImpl;


public class UninstallHandler extends AbstractHandler
{

    public UninstallHandler( StormImpl manager, String clusterName )
    {
        super( manager, clusterName );
        this.trackerOperation = manager.getTracker().createTrackerOperation( StormConfig.PRODUCT_NAME,
                "Uninstall cluster " + clusterName );
    }


    @Override
    public void run()
    {
        TrackerOperation po = trackerOperation;
        StormConfig config = manager.getCluster( clusterName );
        if ( config == null )
        {
            po.addLogFailed( "Cluster not found: " + clusterName );
            return;
        }
        if ( !isNodeConnected( config.getNimbus().getHostname() ) )
        {
            po.addLogFailed( String.format( "Master node %s is not connected", config.getNimbus().getHostname() ) );
            return;
        }
        // check worker nodes
        if ( checkSupervisorNodes( config, true ) == 0 )
        {
            po.addLogFailed( "Worker nodes not connected" );
            return;
        }

        Set<Agent> nodes = new HashSet<>( config.getSupervisors() );

        if ( config.isExternalZookeeper() )
        {
            po.addLog( "Removing Storm from external Zookeeper node..." );
            Command cmd = manager.getCommandRunner()
                                 .createCommand( new RequestBuilder( Commands.make( CommandType.PURGE ) ),
                                         new HashSet<>( Arrays.asList( config.getNimbus() ) ) );
            manager.getCommandRunner().runCommand( cmd );
            if ( cmd.hasSucceeded() )
            {
                po.addLog( "Storm successfully removed from nimbus node" );
            }
            else
            {
                po.addLog( "Failed to remove Storm from nimbus node" );
            }
        }
        else
        {
            nodes.add( config.getNimbus() );
        }

        try
        {
            po.addLog( "Destroying container(s)..." );
            manager.getContainerManager().clonesDestroy( nodes );
            po.addLog( "Container(s) destroyed" );

            manager.getPluginDao().deleteInfo( StormConfig.PRODUCT_NAME, clusterName );
            po.addLogDone( "Cluster info deleted" );
        }
        catch ( LxcDestroyException ex )
        {
            String m = "Failed to destroy node(s)";
            po.addLog( m + ex.getMessage() );
            manager.getLogger().error( m, ex );
        }
    }
}
