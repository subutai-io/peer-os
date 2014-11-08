package org.safehaus.subutai.core.containerregistry.api;


import java.util.Set;
import java.util.UUID;


/**
 * Stores currently connected hosts
 */
public interface ContainerRegistry
{
    public ContainerHostInfo getContainerInfoById( UUID id );

    public ContainerHostInfo getContainerInfoByHostname( String hostname );

    public Set<ContainerHostInfo> getContainersInfo();

    public HostInfo getHostInfoById( UUID id );

    public HostInfo getHostInfoByHostname( String hostname );

    public Set<HostInfo> getHostsInfo();

    public void addHostListener( HostListener listener );

    public void removeHostListener( HostListener listener );
}
