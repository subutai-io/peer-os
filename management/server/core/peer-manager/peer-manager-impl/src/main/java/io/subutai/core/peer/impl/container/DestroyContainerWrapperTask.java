package io.subutai.core.peer.impl.container;


import java.util.concurrent.Callable;

import com.google.common.base.Preconditions;

import io.subutai.common.peer.ContainerHost;
import io.subutai.core.peer.api.LocalPeer;


public class DestroyContainerWrapperTask implements Callable<ContainerHost>
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
    public ContainerHost call() throws Exception
    {
        localPeer.destroyContainer( containerHost );

        return containerHost;
    }
}
