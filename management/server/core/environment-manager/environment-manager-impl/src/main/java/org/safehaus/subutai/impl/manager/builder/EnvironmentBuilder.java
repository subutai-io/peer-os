package org.safehaus.subutai.impl.manager.builder;


import java.util.HashSet;
import java.util.Set;

import org.safehaus.subutai.api.container.ContainerManager;
import org.safehaus.subutai.api.lxcmanager.LxcCreateException;
import org.safehaus.subutai.api.manager.helper.Environment;
import org.safehaus.subutai.api.manager.helper.EnvironmentBlueprint;
import org.safehaus.subutai.api.manager.helper.Node;
import org.safehaus.subutai.api.manager.helper.NodeGroup;
import org.safehaus.subutai.api.manager.helper.PlacementStrategyENUM;
import org.safehaus.subutai.api.templateregistry.TemplateRegistryManager;
import org.safehaus.subutai.impl.manager.exception.EnvironmentBuildException;
import org.safehaus.subutai.impl.manager.exception.EnvironmentInstanceDestroyException;
import org.safehaus.subutai.shared.protocol.Agent;


/**
 * Created by bahadyr on 6/23/14.
 */
public class EnvironmentBuilder {

    TemplateRegistryManager templateRegistryManager;


    public EnvironmentBuilder( final TemplateRegistryManager templateRegistryManager ) {
        this.templateRegistryManager = templateRegistryManager;
    }


    public Environment build( final EnvironmentBlueprint blueprint, ContainerManager containerManager )
            throws EnvironmentBuildException {
        Environment environment = new Environment();
        environment.setName( blueprint.getName() );
        for ( NodeGroup nodeGroup : blueprint.getNodeGroups() ) {
            PlacementStrategyENUM e1 = nodeGroup.getPlacementStrategyENUM();
            int nodeCount = nodeGroup.getNumberOfNodes();

            Set<Node> nodes = new HashSet<>(  );
            try {
                Set<Agent> agents = containerManager
                        .clone( environment.getUuid(), nodeGroup.getTemplateName(), nodeCount, null, e1 );

                for(Agent agent:agents){
                    nodes.add( new Node( agent,templateRegistryManager.getTemplate( nodeGroup.getTemplateName() ) ) ) ;
                }
            }
            catch ( LxcCreateException ex ) {
                throw new EnvironmentBuildException( ex.toString() );
            }

            environment.setNodes( nodes );
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
