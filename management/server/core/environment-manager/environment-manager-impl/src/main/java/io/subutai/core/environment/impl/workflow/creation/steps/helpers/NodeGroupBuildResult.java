package io.subutai.core.environment.impl.workflow.creation.steps.helpers;


import java.util.Set;

import io.subutai.common.environment.CreateEnvironmentContainerGroupResponse;
import io.subutai.core.environment.impl.entity.EnvironmentContainerImpl;
import io.subutai.core.environment.impl.exception.NodeGroupBuildException;


/**
 * holds containers created and exception occurred during a process
 */
public class NodeGroupBuildResult
{
    private final CreateEnvironmentContainerGroupResponse response;
    private Set<EnvironmentContainerImpl> containers;
    private NodeGroupBuildException exception;


    public NodeGroupBuildResult( final Set<EnvironmentContainerImpl> containers,
                                 final NodeGroupBuildException exception,
                                 final CreateEnvironmentContainerGroupResponse response )
    {
        this.containers = containers;
        this.exception = exception;
        this.response = response;
    }


    public Set<EnvironmentContainerImpl> getContainers()
    {
        return containers;
    }


    public NodeGroupBuildException getException()
    {
        return exception;
    }


    public CreateEnvironmentContainerGroupResponse getResponse()
    {
        return response;
    }


    @Override
    public String toString()
    {
        final StringBuffer sb = new StringBuffer( "NodeGroupBuildResult:\n" );

        for ( EnvironmentContainerImpl c : containers )
        {
            sb.append( String.format( "EnvironmentContainer: id=%s, name=%s\n", c.getId(), c.getHostname() ) );
        }
        if ( exception != null )
        {
            sb.append( "Exception: " + exception.toString() );
        }
        return sb.toString();
    }
}
