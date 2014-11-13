package org.safehaus.subutai.core.hostregistry.api;


import java.util.Set;
import java.util.UUID;


/**
 * Stores currently connected hosts
 */
public interface HostRegistry
{
    public ContainerHostInfo getContainerHostInfoById( UUID id ) throws HostDisconnectedException;

    public ContainerHostInfo getContainerHostInfoByHostname( String hostname ) throws HostDisconnectedException;

    public Set<ContainerHostInfo> getContainerHostsInfo();

    public ResourceHostInfo getResourceHostInfoById( UUID id ) throws HostDisconnectedException;

    public ResourceHostInfo getResourceHostInfoByHostname( String hostname ) throws HostDisconnectedException;

    public Set<ResourceHostInfo> getResourceHostsInfo();

    public void addHostListener( HostListener listener );

    public void removeHostListener( HostListener listener );

    public ResourceHostInfo getResourceHostByContainerHost( ContainerHostInfo containerHostInfo )
            throws HostDisconnectedException;
}
