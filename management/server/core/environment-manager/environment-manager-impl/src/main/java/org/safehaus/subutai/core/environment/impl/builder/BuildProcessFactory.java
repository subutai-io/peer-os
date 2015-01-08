package org.safehaus.subutai.core.environment.impl.builder;


import org.safehaus.subutai.core.environment.api.topology.Blueprint2PeerData;
import org.safehaus.subutai.core.environment.api.topology.Blueprint2PeerGroupData;
import org.safehaus.subutai.core.environment.api.topology.Node2PeerData;
import org.safehaus.subutai.core.environment.api.topology.NodeGroup2PeerData;
import org.safehaus.subutai.core.environment.api.topology.NodeGroup2PeerGroupData;
import org.safehaus.subutai.core.environment.api.topology.TopologyData;
import org.safehaus.subutai.core.environment.impl.EnvironmentManagerImpl;


public class BuildProcessFactory
{

    private BuildProcessFactory()
    {
    }


    public static EnvironmentBuildProcessBuilder newBuilder( TopologyData topologyData, EnvironmentManagerImpl em )
    {
        if ( topologyData instanceof Node2PeerData )
        {
            return new Node2PeerBuilder( em );
        }
        if ( topologyData instanceof NodeGroup2PeerData )
        {
            return new NodeGroup2PeerBuilder( em );
        }
        if ( topologyData instanceof Blueprint2PeerData )
        {
            return new Blueprint2PeerBuilder( em );
        }
        if ( topologyData instanceof Blueprint2PeerGroupData )
        {
            return new Blueprint2PeerGroupBuilder( em );
        }
        if ( topologyData instanceof NodeGroup2PeerGroupData )
        {
            return new NodeGroup2PeerGroupBuilder( em );
        }

        throw new IllegalArgumentException( "Unsupported topology data: " + topologyData );
    }
}

