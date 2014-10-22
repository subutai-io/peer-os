package org.safehaus.subutai.core.peer.api;


/**
 * Management host interface.
 */
public interface ResourceHost extends Host
{
    public boolean startContainerHost( final ContainerHost container );

    public boolean stopContainerHost( final ContainerHost container );

    public boolean destroyContainerHost( final ContainerHost container );

    public ContainerHost getContainerHostByName( String hostname );

    public void addContainerHost( ContainerHost containerHost );
}
