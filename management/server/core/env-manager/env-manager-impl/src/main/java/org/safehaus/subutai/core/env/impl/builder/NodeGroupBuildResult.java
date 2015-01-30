package org.safehaus.subutai.core.env.impl.builder;


import java.util.Set;

import org.safehaus.subutai.core.env.impl.entity.EnvironmentContainerImpl;
import org.safehaus.subutai.core.env.impl.exception.NodeGroupBuildException;


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
