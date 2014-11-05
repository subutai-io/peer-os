package org.safehaus.subutai.core.environment.impl.topologies;


import org.safehaus.subutai.common.protocol.NodeGroup;
import org.safehaus.subutai.core.peer.api.Peer;

import com.google.common.collect.Multimap;


/**
 * Created by bahadyr on 11/5/14.
 */
public class NodeGroupsToPeers extends Topology
{

    Multimap<Peer, NodeGroup> peerNodeGroupMap;


    public NodeGroupsToPeers()
    {
        super( templateRegistry );
    }


    public Multimap<Peer, NodeGroup> getPeerNodeGroupMap()
    {
        return peerNodeGroupMap;
    }
}
