package org.safehaus.subutai.plugin.storm.impl.handler;


import java.util.Iterator;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.storm.api.StormClusterConfiguration;
import org.safehaus.subutai.plugin.storm.impl.StormImpl;


abstract class AbstractHandler extends AbstractOperationHandler<StormImpl>
{

    public AbstractHandler( StormImpl manager, String clusterName )
    {
        super( manager, clusterName );
    }


    boolean isNimbusNode( StormClusterConfiguration config, String hostname )
    {
        Environment environment =
                manager.getEnvironmentManager().getEnvironmentByUUID( config.getEnvironmentId() );
        ContainerHost containerHost = environment.getContainerHostByUUID( config.getNimbus() );
        return containerHost.getHostname().equalsIgnoreCase( hostname );
    }


    /**
     * Checks if client nodes are connected and, optionally, removes nodes that are not connected.
     *
     * @return number of connected nodes
     */
    int checkSupervisorNodes( StormClusterConfiguration config, boolean removeDisconnected )
    {
        int connected = 0;
        Iterator<UUID> it = config.getSupervisors().iterator();
        Environment environment = manager.getEnvironmentManager().getEnvironmentByUUID( config.getEnvironmentId() );

        while ( it.hasNext() )
        {
            UUID uuid = it.next();
            if ( isNodeConnected( config, uuid ) )
            {
                connected++;
            }
            else if ( removeDisconnected )
            {
                it.remove();
            }
        }
        return connected;
    }


    boolean isNodeConnected( StormClusterConfiguration config, UUID uuid )
    {
        Environment environment =
                manager.getEnvironmentManager().getEnvironmentByUUID( config.getEnvironmentId() );
        ContainerHost containerHost = environment.getContainerHostByUUID( uuid );

        return containerHost.isConnected();
    }
}
