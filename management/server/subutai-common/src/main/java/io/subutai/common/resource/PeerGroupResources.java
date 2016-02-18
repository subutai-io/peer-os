package io.subutai.common.resource;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.subutai.common.protocol.PingDistances;


/**
 * Peer group resources
 */
public class PeerGroupResources
{
    private PingDistances distances = new PingDistances();
    private List<PeerResources> resources = new ArrayList<>();


    public PeerGroupResources( final List<PeerResources> resources, final PingDistances distances )
    {
        this.distances = distances;
        this.resources = resources;
    }


    public PingDistances getDistances()
    {
        return distances;
    }


    public void addPeerResources( PeerResources peerResources )
    {
        if ( peerResources == null )
        {
            throw new IllegalArgumentException( "Peer resources could not be null." );
        }

        resources.add( peerResources );
    }


    public List<PeerResources> getResources()
    {
        return resources;
    }
}
