package org.safehaus.subutai.core.hostregistry.api;


import java.util.Set;
import java.util.UUID;


/**
 * Parent interface for host info
 */
public interface HostInfo
{
    public UUID getId();


    public String getHostname();


    public Set<Interface> getInterfaces();
}
