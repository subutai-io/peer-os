package org.safehaus.subutai.impl.manager.builder;


import java.util.HashSet;
import java.util.Set;

import org.safehaus.subutai.api.manager.helper.Blueprint;
import org.safehaus.subutai.api.manager.helper.Environment;
import org.safehaus.subutai.api.manager.helper.EnvironmentNodeGroup;
import org.safehaus.subutai.api.manager.helper.NodeGroup;
import org.safehaus.subutai.impl.manager.exception.EnvironmentBuildException;
import org.safehaus.subutai.impl.manager.exception.NodeGroupBuildException;


/**
 * Created by bahadyr on 6/23/14.
 */
public class EnvironmentBuilder {

    NodeGroupBuilder nodeGroupBuilder;


    public EnvironmentBuilder() {
        this.nodeGroupBuilder = new NodeGroupBuilder();
    }


    public Environment build( final Blueprint blueprint ) throws EnvironmentBuildException {
        Environment environment = new Environment();
        Set<EnvironmentNodeGroup> environmentNodeGroups = new HashSet<>();
        for ( NodeGroup nodeGroup : blueprint.getNodeGroups() ) {
            try {
                EnvironmentNodeGroup environmentNodeGroup = nodeGroupBuilder.build( nodeGroup );
                environmentNodeGroups.add( environmentNodeGroup );
            }
            catch ( NodeGroupBuildException e ) {
//                e.printStackTrace();
                //rollback action
                System.out.println(e.getMessage());
            }
            finally {
                throw new EnvironmentBuildException( "Error occured while building nodeGroup" );
            }
        }
        environment.setEnvironmentNodeGroups( environmentNodeGroups );
        return environment;
    }


    public boolean destroy( final Environment environment ) {
        for ( EnvironmentNodeGroup nodeGroup : environment.getEnvironmentNodeGroups() ) {
            nodeGroupBuilder.destroy( nodeGroup );
        }
        return true;
    }
}
