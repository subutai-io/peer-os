package org.safehaus.subutai.core.environment.api.helper;


import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.core.peer.api.ContainerHost;


public interface Environment
{
    public long getCreationTimestamp();


    public EnvironmentStatusEnum getStatus();


    public void setStatus( final EnvironmentStatusEnum status );


    public void addContainer( ContainerHost container );


    public Set<ContainerHost> getContainerHosts();


    public String getName();


    public UUID getId();


    public String getPublicKey();


    public void setPublicKey( String key );


    public ContainerHost getContainerHostById( UUID uuid );


    public ContainerHost getContainerHostByHostname( String hostname );


    public Set<ContainerHost> getContainerHostsByIds( Set<UUID> ids );


    public void addContainers( final Set<ContainerHost> containerHosts );


    public void removeContainer( final ContainerHost containerHost );
}

