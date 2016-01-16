package io.subutai.common.resource;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * Peer group resources
 */
public class PeerGroupResources
{
    private List<PeerResources> resources = new ArrayList<>();


    public PeerGroupResources()
    {

    }


    public PeerGroupResources( final List<PeerResources> resources )
    {
        this.resources = resources;
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
