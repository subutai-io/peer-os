package org.safehaus.subutai.core.peer.impl;


import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.core.peer.api.ContainerHost;


/**
 * Create container response
 */
public class CreateContainerResponse
{
    private Set<ContainerHost> containerHosts;
    private UUID requestId;


    public CreateContainerResponse( UUID requestId, final Set<ContainerHost> containerHosts )
    {
        this.requestId = requestId;
        this.containerHosts = containerHosts;
    }


    public UUID getRequestId()
    {
        return requestId;
    }


    public Set<ContainerHost> getContainerHosts()
    {
        return containerHosts;
    }
}

