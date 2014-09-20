package org.safehaus.subutai.plugin.hive.impl.handler;


import java.util.Iterator;

import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.plugin.hive.api.HiveConfig;
import org.safehaus.subutai.plugin.hive.impl.HiveImpl;


abstract class AbstractHandler extends AbstractOperationHandler<HiveImpl>
{

    public AbstractHandler( HiveImpl manager, String clusterName )
    {
        super( manager, clusterName );
    }


    boolean isServerNode( HiveConfig config, String hostname )
    {
        return config.getServer().getHostname().equalsIgnoreCase( hostname );
    }


    /**
     * Checks if client nodes are connected and, optionally, removes nodes that are not connected.
     *
     * @return number of connected nodes
     */
    int checkClientNodes( HiveConfig config, boolean removeDisconnected )
    {
        int connected = 0;
        Iterator<Agent> it = config.getClients().iterator();
        while ( it.hasNext() )
        {
            if ( isNodeConnected( it.next().getHostname() ) )
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


    boolean isNodeConnected( String hostname )
    {
        return manager.getAgentManager().getAgentByHostname( hostname ) != null;
    }


    boolean isZero( Integer i )
    {
        return i != null && i == 0;
    }
}
