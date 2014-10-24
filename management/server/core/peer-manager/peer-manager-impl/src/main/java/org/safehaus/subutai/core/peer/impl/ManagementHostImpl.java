package org.safehaus.subutai.core.peer.impl;


import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.safehaus.subutai.core.peer.api.ManagementHost;
import org.safehaus.subutai.core.peer.api.ResourceHost;


/**
 * Management host implementation.
 */
public class ManagementHostImpl extends HostImpl implements ManagementHost
{
    //    private static final String DEFAULT_MANAGEMENT_HOSTNAME = "management";
    private Set<ResourceHost> resourceHosts = new HashSet();


    @Override
    public Set<ResourceHost> getResourceHosts()
    {
        return resourceHosts;
    }


    @Override
    public ResourceHost getResourceHostByName( final String hostname )
    {
        return findResourceHostByName( hostname );
    }


    @Override
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
