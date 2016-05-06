package io.subutai.core.environment.impl.workflow.modification.steps.helpers;


import io.subutai.common.environment.Environment;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.util.TaskUtil;
import io.subutai.core.environment.impl.entity.EnvironmentContainerImpl;


public class ContainerDestroyTask extends TaskUtil.Task<Environment>
{

    private final ContainerHost containerHost;


    public ContainerDestroyTask( final ContainerHost containerHost )
    {
        this.containerHost = containerHost;
    }


    public ContainerHost getContainerHost()
    {
        return containerHost;
    }


    @Override
    public Environment call() throws Exception
    {
        return ( ( EnvironmentContainerImpl ) containerHost ).destroy();
    }
}