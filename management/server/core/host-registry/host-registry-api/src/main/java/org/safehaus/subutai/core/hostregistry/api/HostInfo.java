package org.safehaus.subutai.core.hostregistry.api;


import java.util.Set;
import java.util.UUID;


/**
 * Host info. Can contain info about resource host or management host
 */
public interface HostInfo
{

    public UUID getId();


    public String getHostname();


    public Set<String> getIps();


    public String getMacAddress();


    public Set<ContainerHostInfo> getContainers();
}
