package io.subutai.core.environment.impl.workflow.creation.steps.helpers;


import java.util.ArrayList;
import java.util.List;

import io.subutai.common.environment.CreateEnvironmentContainerResponseCollector;


/**
 * holds containers created and exception occurred during a process
 */
public class NodeGroupBuildResult
{
    private final List<CreateEnvironmentContainerResponseCollector> responses = new ArrayList<>();
    //    private Set<EnvironmentContainerImpl> containers = new HashSet<>();
    //    private NodeGroupBuildException exception;


    public List<CreateEnvironmentContainerResponseCollector> getResponses()
    {
        return responses;
    }


//    public boolean hasSucceeded()
//    {
//
//        for ( CreateEnvironmentContainerGroupResponse response : responses )
//        {
//            if ( !response.hasSucceeded() )
//            {
//                return false;
//            }
//        }
//
//        return true;
//    }
}
