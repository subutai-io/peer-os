package org.safehaus.subutai.core.containerregistry.api;


import java.util.Set;
import java.util.UUID;


/**
 * Container info
 */
public interface ContainerHostInfo
{
    public UUID getId();


    public String getHostname();


    public Set<String> getIps();


    public ContainerHostState getStatus();
}
