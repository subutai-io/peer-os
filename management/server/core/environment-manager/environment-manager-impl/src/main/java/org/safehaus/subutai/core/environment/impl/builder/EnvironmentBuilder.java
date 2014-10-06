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
import org.safehaus.subutai.common.util.UUIDUtil;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.container.api.container.ContainerManager;
import org.safehaus.subutai.core.container.api.lxcmanager.LxcCreateException;
import org.safehaus.subutai.core.container.api.lxcmanager.LxcDestroyException;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentBuildException;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentDestroyException;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.environment.api.helper.Node;
import org.safehaus.subutai.core.network.api.NetworkManager;
import org.safehaus.subutai.core.registry.api.Template;
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


        Environment environment = new Environment( UUIDUtil.generateTimeBasedUUID(), blueprint.getName() );
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
            Set<Node> nodes = new HashSet<>();
            try
            {
                Set<Agent> agents = containerManager
                        .clone( environment.getUuid(), template.getTemplateName(), nodeGroup.getNumberOfNodes(),
                                physicalAgents, placementStrategy );
                for ( Agent agent : agents )
                {
                    nodes.add( new Node( agent, template, nodeGroup.getName() ) );
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
                Set<Node> alreadyBuiltNodes = environment.getNodes();

                if ( alreadyBuiltNodes != null && !alreadyBuiltNodes.isEmpty() )
                {

                    Set<Agent> agents = new HashSet<>();
                    for ( Node node : alreadyBuiltNodes )
                    {
                        agents.add( node.getAgent() );
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

            environment.getNodes().addAll( nodes );
        }

        List<Agent> allAgents = new ArrayList<>();
        for ( Node node : environment.getNodes() )
        {
            allAgents.add( node.getAgent() );
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
}
