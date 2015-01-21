package org.safehaus.subutai.env.api.build;


import java.util.Map;
import java.util.Set;

import org.safehaus.subutai.common.peer.Peer;


public interface Topology
{
    public Map<Peer, Set<NodeGroup>> getNodeGroupPlacement();
}
