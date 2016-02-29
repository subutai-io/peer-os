package io.subutai.core.environment.impl.workflow.creation.steps.helpers;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.subutai.common.environment.CreateEnvironmentContainerGroupResponse;
import io.subutai.core.environment.impl.entity.EnvironmentContainerImpl;
import io.subutai.core.environment.impl.exception.NodeGroupBuildException;


/**
 * holds containers created and exception occurred during a process
 */
public class NodeGroupBuildResult
{
    private final List<CreateEnvironmentContainerGroupResponse> responses = new ArrayList<>();
    //    private Set<EnvironmentContainerImpl> containers = new HashSet<>();
    //    private NodeGroupBuildException exception;


    public List<CreateEnvironmentContainerGroupResponse> getResponses()
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
