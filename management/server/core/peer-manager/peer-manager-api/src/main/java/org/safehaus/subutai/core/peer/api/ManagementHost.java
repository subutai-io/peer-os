package org.safehaus.subutai.core.peer.api;


import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.Agent;

import com.google.common.collect.Sets;


/**
 * Management host implementation.
 */
public class ManagementHost extends SubutaiHost
{
    //    private static final String DEFAULT_MANAGEMENT_HOSTNAME = "management";
    private Set<ResourceHost> resourceHosts = Sets.newHashSet();


    public ManagementHost( final Agent agent, UUID peerId )
    {
        super( agent, peerId );
    }

    //
    //    @Override
    //    public boolean isConnected( final Host host )
    //    {
    //        //TODO: Implement resource host check
    //        return true;
    //    }


    public Set<ResourceHost> getResourceHosts()
    {
        return resourceHosts;
    }


    public void setResourceHosts( final Set<ResourceHost> resourceHosts )
    {
        this.resourceHosts = resourceHosts;
    }


    public ResourceHost getResourceHostByName( final String hostname )
    {
        return findResourceHostByName( hostname );
    }


    public void addResourceHost( final ResourceHost host )
    {
        if ( host == null )
        {
            throw new IllegalArgumentException( "Resource host could not be null." );
        }
        resourceHosts.add( host );
    }


    private ResourceHost findResourceHostByName( final String hostname )
    {
        ResourceHost result = null;
        Iterator iterator = resourceHosts.iterator();

        while ( result == null && iterator.hasNext() )
        {
            ResourceHost host = ( ResourceHost ) iterator.next();

            if ( host.getHostname().equals( hostname ) )
            {
                result = host;
            }
        }
        return result;
    }
}
