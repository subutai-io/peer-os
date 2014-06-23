package org.safehaus.subutai.impl.manager;


import java.util.HashSet;
import java.util.Set;

import org.safehaus.subutai.api.manager.Blueprint;
import org.safehaus.subutai.api.manager.Environment;
import org.safehaus.subutai.api.manager.NodeGroup;


/**
 * Created by bahadyr on 6/23/14.
 */
public class EnvironmentBuilder {

    NodeGroupBuilder nodeGoupBuilder = new NodeGroupBuilder();


    public Environment build( final Blueprint blueprint ) {
        Environment environment = new Environment();
        Set<NodeGroup> nodeGroupSet = new HashSet<>();
        for ( NodeGroup nodeGroup : blueprint.getNodeGroups() ) {
            NodeGroup createdNodeGroup = nodeGoupBuilder.buildNodeGroup( nodeGroup );
            nodeGroupSet.add( createdNodeGroup );
        }
        environment.setNodeGroups( nodeGroupSet );
        return environment;
    }


    public boolean destroy( final Environment environment ) {
        for ( NodeGroup nodeGroup : environment.getNodeGroups() ) {
            nodeGoupBuilder.destroyNodeGroup( nodeGroup );
        }
        return true;
    }
}
