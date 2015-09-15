package io.subutai.core.environment.impl.workflow.creation.steps.helpers;


import java.util.Set;

import io.subutai.core.environment.impl.entity.EnvironmentContainerImpl;
import io.subutai.core.environment.impl.exception.NodeGroupBuildException;


/**
 * holds containers created and exception occurred during a process
 */
public class NodeGroupBuildResult
{
    private Set<EnvironmentContainerImpl> containers;
    private NodeGroupBuildException exception;


    public NodeGroupBuildResult( final Set<EnvironmentContainerImpl> containers,
                                 final NodeGroupBuildException exception )
    {
        this.containers = containers;
        this.exception = exception;
    }


    public Set<EnvironmentContainerImpl> getContainers()
    {
        return containers;
    }


    public NodeGroupBuildException getException()
    {
        return exception;
    }
}
