package org.safehaus.subutai.core.peer.impl.container;


import java.util.UUID;
import java.util.concurrent.Callable;

import org.safehaus.subutai.common.peer.ContainerHost;
import org.safehaus.subutai.core.peer.api.ResourceHost;

import com.google.common.base.Preconditions;


public class DestroyContainerWrapperTask implements Callable<UUID>
{
    private final ResourceHost resourceHost;
    private final ContainerHost containerHost;


    public DestroyContainerWrapperTask( final ResourceHost resourceHost, final ContainerHost containerHost )
    {
        Preconditions.checkNotNull( resourceHost );
        Preconditions.checkNotNull( containerHost );

        this.resourceHost = resourceHost;
        this.containerHost = containerHost;
    }


    @Override
    public UUID call() throws Exception
    {
        resourceHost.destroyContainerHost( containerHost );

        return containerHost.getId();
    }
}
