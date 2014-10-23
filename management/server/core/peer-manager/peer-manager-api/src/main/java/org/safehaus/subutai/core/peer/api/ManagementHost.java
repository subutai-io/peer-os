package org.safehaus.subutai.core.peer.api;


import java.util.Set;


/**
 * Management host interface.
 */
public interface ManagementHost extends Host
{
    public Set<ResourceHost> getResourceHosts();

    public ResourceHost getResourceHostByName( String hostname );

    public void addResourceHost( ResourceHost host );
}
