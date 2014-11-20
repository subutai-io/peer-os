package org.safehaus.subutai.plugin.storm.impl.handler;


import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.exception.CommandException;
import org.safehaus.subutai.common.protocol.CommandResult;
import org.safehaus.subutai.common.protocol.RequestBuilder;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.storm.api.StormClusterConfiguration;
import org.safehaus.subutai.plugin.storm.impl.CommandType;
import org.safehaus.subutai.plugin.storm.impl.Commands;
import org.safehaus.subutai.plugin.storm.impl.StormImpl;


public class UninstallHandler extends AbstractHandler
{

    public UninstallHandler( StormImpl manager, String clusterName )
    {
        super( manager, clusterName );
        this.trackerOperation = manager.getTracker().createTrackerOperation( StormClusterConfiguration.PRODUCT_NAME,
                "Uninstall cluster " + clusterName );
    }


    @Override
    public void run()
    {
        TrackerOperation po = trackerOperation;
        StormClusterConfiguration config = manager.getCluster( clusterName );
        if ( config == null )
        {
            po.addLogFailed( "Cluster not found: " + clusterName );
            return;
        }
        if ( !isNodeConnected( config, config.getNimbus() ) )
        {
            Environment environment =
                    manager.getEnvironmentManager().getEnvironmentByUUID( config.getEnvironmentId() );
            ContainerHost nimbusHost = environment.getContainerHostByUUID( config.getNimbus() );
            po.addLogFailed( String.format( "Master node %s is not connected", nimbusHost.getHostname() ) );
            return;
        }
        // check worker nodes
        if ( checkSupervisorNodes( config, true ) == 0 )
        {
            po.addLogFailed( "Worker nodes not connected" );
            return;
        }

        Set<UUID> nodes = new HashSet<>( config.getSupervisors() );

        if ( config.isExternalZookeeper() )
        {
            po.addLog( "Removing Storm from external Zookeeper node..." );

            Environment environment =
                    manager.getEnvironmentManager().getEnvironmentByUUID( config.getEnvironmentId() );
            ContainerHost nimbusHost = environment.getContainerHostByUUID( config.getNimbus() );
            try
            {
                CommandResult commandResult = nimbusHost.execute( new RequestBuilder( Commands.make( CommandType.PURGE ) ) );

                if ( commandResult.hasSucceeded() )
                {
                    po.addLog( "Storm successfully removed from nimbus node" );
                }
                else
                {
                    po.addLog( "Failed to remove Storm from nimbus node" );
                }
            }
            catch ( CommandException e )
            {
                e.printStackTrace();
            }


        }
        else
        {
            nodes.add( config.getNimbus() );
        }

        //TODO destroy storm supervisor nodes
        po.addLog( "Storm supervisor nodes are not destroyed since environment manager does not provide this functionality yet!" );
//        po.addLog( "Destroying container(s)..." );
//        manager.getContainerManager().clonesDestroy( nodes );
//        po.addLog( "Container(s) destroyed" );

        manager.getPluginDAO().deleteInfo( StormClusterConfiguration.PRODUCT_NAME, clusterName );
        po.addLogDone( "Cluster info deleted" );
    }
}
