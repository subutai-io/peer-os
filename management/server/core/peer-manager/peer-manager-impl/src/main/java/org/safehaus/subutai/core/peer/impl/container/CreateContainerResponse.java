package org.safehaus.subutai.core.peer.impl.container;


import java.util.HashSet;
import java.util.Set;

import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.peer.api.HostKey;


/**
 * Create container response
 */
public class CreateContainerResponse
{
    private Set<HostKey> hostKeys;


    public CreateContainerResponse( final Set<ContainerHost> hostKeys )
    {
        this.hostKeys = new HashSet<>();
        for ( ContainerHost containerHost : hostKeys )
        {
            this.hostKeys.add( new HostKey( containerHost.getHostId(), containerHost.getPeerId(),
                    containerHost.getCreatorPeerId(), containerHost.getHostname(), containerHost.getEnvironmentId(),
                    containerHost.getNodeGroupName() ) );
        }
    }


    public Set<HostKey> getHostKeys()
    {
        return hostKeys;
    }
}

