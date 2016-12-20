package io.subutai.core.environment.impl.workflow.destruction.steps;


import io.subutai.common.environment.Environment;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.PeerException;
import io.subutai.core.environment.impl.entity.EnvironmentContainerImpl;


public class DestroyContainerStep
{
    private final ContainerHost containerHost;


    public DestroyContainerStep( final ContainerHost containerHost )
    {
        this.containerHost = containerHost;
    }


    public Environment execute() throws PeerException
    {
        return ( ( EnvironmentContainerImpl ) containerHost ).destroy( false );
    }
}
