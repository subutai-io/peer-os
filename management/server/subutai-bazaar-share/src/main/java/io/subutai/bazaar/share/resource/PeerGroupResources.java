package io.subutai.bazaar.share.resource;


import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


/**
 * Peer group resources
 */
@JsonIgnoreProperties( ignoreUnknown = true )
public class PeerGroupResources
{
    private List<PeerResources> resources = new ArrayList<>();


    public PeerGroupResources( final List<PeerResources> resources )
    {
        this.resources = resources;
    }


    public List<PeerResources> getResources()
    {
        return resources;
    }
}
