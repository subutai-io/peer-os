package io.subutai.core.env.impl.builder;


import java.util.Set;

import io.subutai.core.env.impl.entity.EnvironmentContainerImpl;
import io.subutai.core.env.impl.exception.NodeGroupBuildException;


/**
 * Result for {@link NodeGroupBuilder} task
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
