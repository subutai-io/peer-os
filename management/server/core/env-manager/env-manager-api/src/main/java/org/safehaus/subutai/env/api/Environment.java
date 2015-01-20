package org.safehaus.subutai.env.api;


import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.peer.ContainerHost;


/**
 * Environment
 */
public interface Environment
{
    public UUID getId();

    public Set<ContainerHost> getContainers();
}
