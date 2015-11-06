package io.subutai.core.localpeer.impl.container;


import java.util.concurrent.Callable;

import com.google.common.base.Preconditions;

import io.subutai.common.peer.ContainerHost;
import io.subutai.core.peer.api.LocalPeer;


public class DestroyContainerWrapperTask implements Callable<ContainerHost>
{
    private final LocalPeer peerManager;
    private final ContainerHost containerHost;


    public DestroyContainerWrapperTask( final LocalPeer peerManager, final ContainerHost containerHost )
    {
        Preconditions.checkNotNull( peerManager );
        Preconditions.checkNotNull( containerHost );

        this.peerManager = peerManager;
        this.containerHost = containerHost;
    }


    @Override
    public ContainerHost call() throws Exception
    {
        peerManager.destroyContainer( containerHost.getContainerId() );

        return containerHost;
    }
}
