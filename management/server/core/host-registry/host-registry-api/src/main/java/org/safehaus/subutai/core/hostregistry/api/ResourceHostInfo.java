package org.safehaus.subutai.core.hostregistry.api;


import java.util.Set;

import org.safehaus.subutai.common.host.HostInfo;


/**
 * Host info. Can contain info about resource host or management host
 */
public interface ResourceHostInfo extends HostInfo
{
    /**
     * returns hosted containers
     */
    public Set<ContainerHostInfo> getContainers();
}
