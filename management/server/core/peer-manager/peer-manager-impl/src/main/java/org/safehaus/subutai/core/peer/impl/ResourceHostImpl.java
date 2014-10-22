package org.safehaus.subutai.core.peer.impl;


import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.peer.api.ResourceHost;


/**
 * Resource host implementation.
 */
public class ResourceHostImpl extends HostImpl implements ResourceHost
{
    Set<ContainerHostImpl> containersHosts = new HashSet();


    public void addContainerHost( ContainerHost host )
    {
        if ( host == null )
        {
            throw new IllegalArgumentException( "Container host could not be null." );
        }
        containersHosts.add( ( ContainerHostImpl ) host );
    }


//    public HostImpl getChildHost( final String hostname )
    //    {
    //        HostImpl result = null;
    //        Iterator<ContainerHostImpl> iterator = containersHosts.iterator();
    //
    //        while ( result == null && iterator.hasNext() )
    //        {
    //            HostImpl host = iterator.next();
    //            if ( hostname.equals( host.getAgent().getHostname() ) )
    //            {
    //                result = host;
    //            }
    //        }
    //        return result;
    //    }


    @Override
    public boolean startContainerHost( final ContainerHost container )
    {
        return false;
    }


    @Override
    public boolean stopContainerHost( final ContainerHost container )
    {
        return false;
    }


    @Override
    public boolean destroyContainerHost( final ContainerHost container )
    {
        return false;
    }


    @Override
    public ContainerHost getContainerHostByName( final String hostname )
    {
        return findContainerHostByName( hostname );
    }


    private ContainerHost findContainerHostByName( final String hostname )
    {
        ContainerHost result = null;
        Iterator iterator = containersHosts.iterator();

        while ( result == null && iterator.hasNext() )
        {
            ContainerHost host = ( ContainerHost ) iterator.next();

            if ( host.getHostname().equals( hostname ) )
            {
                result = host;
            }
        }
        return result;
    }
}
