package org.safehaus.subutai.core.environment.impl.builder;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.common.protocol.EnvironmentBuildTask;
import org.safehaus.subutai.common.protocol.NodeGroup;
import org.safehaus.subutai.common.protocol.PlacementStrategy;
import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.container.api.container.ContainerManager;
import org.safehaus.subutai.core.container.api.lxcmanager.LxcCreateException;
import org.safehaus.subutai.core.container.api.lxcmanager.LxcDestroyException;
import org.safehaus.subutai.core.environment.api.EnvironmentContainer;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentBuildException;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentDestroyException;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.environment.api.helper.EnvironmentContainerNode;
import org.safehaus.subutai.core.network.api.NetworkManager;
import org.safehaus.subutai.core.registry.api.TemplateRegistry;

import com.google.common.collect.Lists;


/**
 * This class creates containers according to supplied Environment Blueprint
 */
public class EnvironmentBuilder
{

    private final TemplateRegistry templateRegistry;
    private final AgentManager agentManager;
    private final NetworkManager networkManager;
    private final ContainerManager containerManager;


    public EnvironmentBuilder( final TemplateRegistry templateRegistry, final AgentManager agentManager,
                               NetworkManager networkManager, ContainerManager containerManager )
    {
        this.templateRegistry = templateRegistry;
        this.agentManager = agentManager;
        this.networkManager = networkManager;
        this.containerManager = containerManager;
    }


    //@todo destroy all containers of all groups inside environment on any failure ???
    public Environment build( final EnvironmentBuildTask environmentBuildTask ) throws EnvironmentBuildException
    {


        EnvironmentBlueprint blueprint = environmentBuildTask.getEnvironmentBlueprint();
        Set<String> physicalNodes = environmentBuildTask.getPhysicalNodes();


        Environment environment = new Environment( blueprint.getName() );
        for ( NodeGroup nodeGroup : blueprint.getNodeGroups() )
        {
            PlacementStrategy placementStrategy = nodeGroup.getPlacementStrategy();
            if ( nodeGroup.getNumberOfNodes() <= 0 )
            {
                throw new EnvironmentBuildException(
                        String.format( "Node Group %s specifies invalid number of nodes %d", nodeGroup.getName(),
                                nodeGroup.getNumberOfNodes() ) );
            }

            Set<Agent> physicalAgents = new HashSet<>();
            if ( !physicalNodes.isEmpty() )
            {
                for ( String host : physicalNodes )
                {
                    Agent pAgent = agentManager.getAgentByHostname( host );
                    if ( pAgent == null )
                    {
                        throw new EnvironmentBuildException(
                                String.format( "Physical agent %s is not connected", host ) );
                    }
                    physicalAgents.add( pAgent );
                }
            }
            Template template = templateRegistry.getTemplate( nodeGroup.getTemplateName() );
            if ( template == null )
            {
                throw new EnvironmentBuildException(
                        String.format( "Template %s not registered", nodeGroup.getTemplateName() ) );
            }
            Set<EnvironmentContainerNode> environmentContainerNodes = new HashSet<>();
            try
            {
                Set<Agent> agents = containerManager
                        .clone( environment.getUuid(), template.getTemplateName(), nodeGroup.getNumberOfNodes(),
                                physicalAgents, placementStrategy );
                for ( Agent agent : agents )
                {
                    environmentContainerNodes.add( new EnvironmentContainerNode( agent, template, nodeGroup.getName() ) );
                }


                // Removing redundant operations with hosts file
                if ( !blueprint.isLinkHosts() )
                {
                    if ( nodeGroup.isLinkHosts() )
                    {
                        networkManager.configHostsOnAgents( Lists.newArrayList( agents ), nodeGroup.getDomainName() );
                    }
                }

                // Removing redundant operations with ssh
                if ( !blueprint.isExchangeSshKeys() )
                {
                    if ( nodeGroup.isExchangeSshKeys() )
                    {
                        networkManager.configSshOnAgents( Lists.newArrayList( agents ) );
                    }
                }
            }
            catch ( LxcCreateException ex )
            {

                //destroy lxcs here
                Set<EnvironmentContainerNode>
                        alreadyBuiltEnvironmentContainerNodes = environment.getEnvironmentContainerNodes();

                if ( alreadyBuiltEnvironmentContainerNodes != null && !alreadyBuiltEnvironmentContainerNodes.isEmpty() )
                {

                    Set<Agent> agents = new HashSet<>();
                    for ( EnvironmentContainerNode environmentContainerNode : alreadyBuiltEnvironmentContainerNodes )
                    {
                        agents.add( environmentContainerNode.getAgent() );
                    }

                    try
                    {
                        containerManager.clonesDestroy( agents );
                    }
                    catch ( LxcDestroyException ignore )
                    {
                    }
                }


                throw new EnvironmentBuildException( ex.toString() );
            }

            environment.getEnvironmentContainerNodes().addAll( environmentContainerNodes );
        }

        List<Agent> allAgents = new ArrayList<>();
        for ( EnvironmentContainerNode environmentContainerNode : environment.getEnvironmentContainerNodes() )
        {
            allAgents.add( environmentContainerNode.getAgent() );
        }

        if ( blueprint.isLinkHosts() )
        {
            networkManager.configHostsOnAgents( allAgents, blueprint.getDomainName() );
        }
        if ( blueprint.isExchangeSshKeys() )
        {
            networkManager.configSshOnAgents( allAgents );
        }

        return environment;
    }


    public void destroy( final Environment environment ) throws EnvironmentDestroyException
    {
        //TODO destroy environment code goes here
    }


    public Environment convertEnvironmentContainersToNodes( final Environment environment )
    {
        final Set<EnvironmentContainerNode> environmentContainerNodes = new HashSet<>();
        for ( EnvironmentContainer container : environment.getContainers() )
        {
            Agent agent = agentManager.getAgentByHostname( container.getHostname() );
            Template template = templateRegistry.getTemplate( container.getTemplateName() );
            EnvironmentContainerNode
                    environmentContainerNode = new EnvironmentContainerNode( agent, template, "cassandra" );
            environmentContainerNodes.add( environmentContainerNode );
        }

        environment.setEnvironmentContainerNodes( environmentContainerNodes );
        return environment;
    }
}
