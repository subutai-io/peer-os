package io.subutai.core.peer.impl.container;


import java.util.UUID;
import java.util.concurrent.Callable;

import io.subutai.common.peer.ContainerHost;
import io.subutai.core.peer.api.LocalPeer;

import com.google.common.base.Preconditions;


public class DestroyContainerWrapperTask implements Callable<UUID>
{
    private final LocalPeer localPeer;
    private final ContainerHost containerHost;


    public DestroyContainerWrapperTask( final LocalPeer localPeer, final ContainerHost containerHost )
    {
        Preconditions.checkNotNull( localPeer );
        Preconditions.checkNotNull( containerHost );

        this.localPeer = localPeer;
        this.containerHost = containerHost;
    }


    @Override
    public UUID call() throws Exception
    {
        localPeer.destroyContainer( containerHost );

        return containerHost.getId();
    }
}
