package io.subutai.common.environment;


import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import io.subutai.common.peer.Peer;


public class Topology
{
    private final Map<Peer, Set<NodeGroup>> nodeGroupPlacement = Maps.newHashMap();


    public Map<Peer, Set<NodeGroup>> getNodeGroupPlacement()
    {
        return Collections.unmodifiableMap( nodeGroupPlacement );
    }


    public Set<Peer> getAllPeers()
    {
        return Collections.unmodifiableSet( nodeGroupPlacement.keySet() );
    }


    public void addNodeGroupPlacement( Peer peer, NodeGroup nodeGroup )
    {
        Preconditions.checkNotNull( peer, "Invalid peer" );
        Preconditions.checkNotNull( nodeGroup, "Invalid node group" );

        Set<NodeGroup> peerNodeGroups = nodeGroupPlacement.get( peer );

        if ( peerNodeGroups == null )
        {
            peerNodeGroups = Sets.newHashSet();
            nodeGroupPlacement.put( peer, peerNodeGroups );
        }

        peerNodeGroups.add( nodeGroup );
    }
}
