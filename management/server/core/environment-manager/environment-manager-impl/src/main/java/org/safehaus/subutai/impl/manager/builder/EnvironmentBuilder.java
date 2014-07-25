package org.safehaus.subutai.impl.manager.builder;


import java.util.HashSet;
import java.util.Set;

import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.container.ContainerManager;
import org.safehaus.subutai.api.lxcmanager.LxcCreateException;
import org.safehaus.subutai.api.manager.exception.EnvironmentBuildException;
import org.safehaus.subutai.api.manager.exception.EnvironmentDestroyException;
import org.safehaus.subutai.api.manager.helper.Environment;
import org.safehaus.subutai.api.manager.helper.EnvironmentBlueprint;
import org.safehaus.subutai.api.manager.helper.Node;
import org.safehaus.subutai.api.manager.helper.NodeGroup;
import org.safehaus.subutai.api.manager.helper.PlacementStrategy;
import org.safehaus.subutai.api.templateregistry.Template;
import org.safehaus.subutai.api.templateregistry.TemplateRegistryManager;
import org.safehaus.subutai.shared.protocol.Agent;


/**
 * Created by bahadyr on 6/23/14.
 */
public class EnvironmentBuilder {

    private final TemplateRegistryManager templateRegistryManager;
    private final AgentManager agentManager;


    public EnvironmentBuilder( final TemplateRegistryManager templateRegistryManager,
                               final AgentManager agentManager ) {
        this.templateRegistryManager = templateRegistryManager;
        this.agentManager = agentManager;
    }


    public Environment build( final EnvironmentBlueprint blueprint, ContainerManager containerManager )
            throws EnvironmentBuildException {
        Environment environment = new Environment( blueprint.getName() );
        for ( NodeGroup nodeGroup : blueprint.getNodeGroups() ) {
            PlacementStrategy placementStrategy = nodeGroup.getPlacementStrategy();
            if ( nodeGroup.getNumberOfNodes() <= 0 ) {

            }

            Set<Node> nodes = new HashSet<>();
            Set<Agent> physicalAgents = new HashSet<>();
            for ( String host : nodeGroup.getPhysicalNodes() ) {
                Agent pAgent = agentManager.getAgentByHostname( host );
                if ( pAgent == null ) {
                    throw new EnvironmentBuildException( String.format( "Physical agent %s is not connected", host ) );
                }
                physicalAgents.add( pAgent );
            }
            Template template = templateRegistryManager.getTemplate( nodeGroup.getTemplateName() );
            if ( template == null ) {
                throw new EnvironmentBuildException(
                        String.format( "Template %s not registered", nodeGroup.getTemplateName() ) );
            }
            try {
                Set<Agent> agents = containerManager
                        .clone( environment.getUuid(), template.getTemplateName(), nodeGroup.getNumberOfNodes(),
                                physicalAgents, placementStrategy );
                for ( Agent agent : agents ) {
                    nodes.add( new Node( agent, template, nodeGroup.getName() ) );
                }
            }
            catch ( LxcCreateException ex ) {
                throw new EnvironmentBuildException( ex.toString() );
            }

            environment.getNodes().addAll( nodes );
        }

        return environment;
    }


    public void destroy( final Environment environment ) throws EnvironmentDestroyException {
        //TODO destroy environment code goes here
        //        for ( EnvironmentNodeGroup nodeGroup : environment.getEnvironmentNodeGroups() ) {
        //            nodeGroupBuilder.destroy( nodeGroup );
        //        }
    }
}
