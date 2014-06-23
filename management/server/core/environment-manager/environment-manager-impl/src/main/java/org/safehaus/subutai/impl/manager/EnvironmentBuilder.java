package org.safehaus.subutai.impl.manager;


import java.util.HashSet;
import java.util.Set;

import org.safehaus.subutai.api.manager.Blueprint;
import org.safehaus.subutai.api.manager.Environment;
import org.safehaus.subutai.api.manager.NodeGroup;
import org.safehaus.subutai.impl.manager.org.safehaus.subutai.impl.manager.exception.EnvironmentBuildException;
import org.safehaus.subutai.impl.manager.org.safehaus.subutai.impl.manager.exception.NodeGroupBuildException;


/**
 * Created by bahadyr on 6/23/14.
 */
public class EnvironmentBuilder {

    NodeGroupBuilder nodeGroupBuilder = new NodeGroupBuilder();


    public Environment build( final Blueprint blueprint ) throws EnvironmentBuildException {
        Environment environment = new Environment();
        Set<NodeGroup> nodeGroupSet = new HashSet<>();
        for ( NodeGroup nodeGroup : blueprint.getNodeGroups() ) {
            NodeGroup createdNodeGroup = null;
            try {
                createdNodeGroup = nodeGroupBuilder.buildNodeGroup( nodeGroup );
                nodeGroupSet.add( createdNodeGroup );
            }
            catch ( NodeGroupBuildException e ) {
                e.printStackTrace();
                //rollback action
            } finally {
                throw new EnvironmentBuildException();
            }
        }
        environment.setNodeGroups( nodeGroupSet );
        return environment;
    }


    public boolean destroy( final Environment environment ) {
        for ( NodeGroup nodeGroup : environment.getNodeGroups() ) {
            nodeGroupBuilder.destroyNodeGroup( nodeGroup );
        }
        return true;
    }
}
