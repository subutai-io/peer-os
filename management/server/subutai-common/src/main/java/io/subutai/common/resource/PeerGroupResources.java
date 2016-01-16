package io.subutai.common.resource;


import java.util.Collection;


/**
 * Peer group resources
 */
public class PeerGroupResources
{
    private Collection<PeerResources> resources;


    public PeerGroupResources( final Collection<PeerResources> resources )
    {
        this.resources = resources;
    }


    public Collection<PeerResources> getResources()
    {
        return resources;
    }
}
