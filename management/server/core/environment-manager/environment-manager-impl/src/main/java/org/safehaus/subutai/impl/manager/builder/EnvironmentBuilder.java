package org.safehaus.subutai.impl.manager.builder;


import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.safehaus.subutai.api.container.ContainerManager;
import org.safehaus.subutai.api.manager.helper.Environment;
import org.safehaus.subutai.api.manager.helper.EnvironmentBlueprint;
import org.safehaus.subutai.api.manager.helper.NodeGroup;
import org.safehaus.subutai.api.manager.helper.PlacementStrategyENUM;
import org.safehaus.subutai.impl.manager.exception.EnvironmentBuildException;
import org.safehaus.subutai.impl.manager.exception.EnvironmentInstanceDestroyException;
import org.safehaus.subutai.shared.protocol.Agent;


/**
 * Created by bahadyr on 6/23/14.
 */
public class EnvironmentBuilder {


    public Environment build( final EnvironmentBlueprint blueprint, ContainerManager containerManager )
            throws EnvironmentBuildException {
        Environment environment = new Environment();
        environment.setName( blueprint.getName() );
        for ( NodeGroup nodeGroup : blueprint.getNodeGroups() ) {
            PlacementStrategyENUM e1 = nodeGroup.getPlacementStrategyENUM();
            Collection<Agent> hosts = new HashSet<Agent>();
            int nodeCount = 3;

            Set<Agent> agentList =
                    containerManager.clone( nodeGroup.getName(), nodeGroup.getTemplateName(), nodeCount, hosts, e1 );

            environment.getAgents().addAll( agentList );
        }

        return environment;
    }


    public void destroy( final Environment environment ) throws EnvironmentInstanceDestroyException {
        //TODO destroy environment code goes here
        //        for ( EnvironmentNodeGroup nodeGroup : environment.getEnvironmentNodeGroups() ) {
        //            nodeGroupBuilder.destroy( nodeGroup );
        //        }
    }
}
