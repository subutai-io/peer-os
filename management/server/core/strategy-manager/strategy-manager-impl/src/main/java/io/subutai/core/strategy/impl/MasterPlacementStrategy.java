package io.subutai.core.strategy.impl;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import io.subutai.common.environment.Blueprint;
import io.subutai.common.environment.NodeGroup;
import io.subutai.common.peer.ContainerSize;
import io.subutai.common.quota.ContainerQuota;
import io.subutai.common.resource.PeerGroupResources;
import io.subutai.common.resource.PeerResources;
import io.subutai.core.strategy.api.ContainerPlacementStrategy;
import io.subutai.core.strategy.api.NodeSchema;
import io.subutai.core.strategy.api.StrategyException;


/**
 * Master
 */
public class MasterPlacementStrategy implements ContainerPlacementStrategy
{
    private static List<NodeSchema> schema = new ArrayList<>();
    private static MasterPlacementStrategy instance;
    private final String name = "master";

    static
    {
/*
        schema.add( new NodeSchema( "Huge master", ContainerSize.HUGE, "master" ) );
        schema.add( new NodeSchema( "Large master", ContainerSize.LARGE, "master" ) );
        schema.add( new NodeSchema( "Medium master", ContainerSize.MEDIUM, "master" ) );
*/
        schema.add( new NodeSchema( "master", ContainerSize.TINY, "master" ) );
        schema.add( new NodeSchema( "hadoop", ContainerSize.TINY, "hadoop" ) );
        schema.add( new NodeSchema( "cassandra", ContainerSize.TINY, "cassandra" ) );
    }

    public static MasterPlacementStrategy getInstance()
    {
        if ( instance == null )
        {
            instance = new MasterPlacementStrategy();
        }
        return instance;
    }


    @Override
    public String getId()
    {
        return "MASTER-STRATEGY";
    }


    @Override
    public String getTitle()
    {
        return "Base container placement strategy base on 'master' template.";
    }


    @Override
    public List<NodeSchema> getScheme()
    {
        return this.schema;
    }


    @Override
    public Blueprint distribute( PeerGroupResources peerGroupResources, Map<ContainerSize, ContainerQuota> quotas )
            throws StrategyException
    {

        // build list of allocators
        List<ResourceAllocator> allocators = new ArrayList<>();
        for ( PeerResources peerResources : peerGroupResources.getResources() )
        {
            if ( peerResources.getEnvironmentLimit() > 0 )
            {
                ResourceAllocator resourceAllocator = new ResourceAllocator( peerResources );
                allocators.add( resourceAllocator );
            }
        }


        // distribute node groups
        for ( NodeSchema nodeSchema : this.getScheme() )
        {
            String containerName = generateContainerName( nodeSchema );

            // select preferred peer
            List<ResourceAllocator> preferredAllocators = getPreferredAllocators( allocators );
            boolean allocated = false;
            for ( ResourceAllocator resourceAllocator : preferredAllocators )
            {
                allocated = resourceAllocator
                        .allocate( containerName, nodeSchema.getTemplateName(), nodeSchema.getSize(),
                                quotas.get( nodeSchema.getSize() ) );
                if ( allocated )
                {
                    break;
                }
            }

            if ( !allocated )
            {
                throw new StrategyException(
                        "Could not allocate containers. There is no space for container: '" + containerName + "'" );
            }
        }

        Set<NodeGroup> nodeGroups = new HashSet<>();

        for ( ResourceAllocator resourceAllocator : allocators )
        {
            List<ResourceAllocator.AllocatedContainer> containers = resourceAllocator.getContainers();
            if ( !containers.isEmpty() )
            {
                for ( ResourceAllocator.AllocatedContainer container : containers )
                {
                    NodeGroup nodeGroup =
                            new NodeGroup( container.getName(), container.getTemplateName(), container.getSize(), 0,
                                    0, container.getPeerId(), container.getHostId() );
                    nodeGroups.add( nodeGroup );
                }
            }
        }
        return new Blueprint( this.name + UUID.randomUUID(), nodeGroups );
    }


    private List<ResourceAllocator> getPreferredAllocators( final List<ResourceAllocator> allocators )
    {
        List<ResourceAllocator> result = new ArrayList<>( allocators );
        Collections.shuffle( result );
        return result;
    }


    private String generateContainerName( final NodeSchema nodeSchema )
    {
        return nodeSchema.getName().replaceAll( "\\s+", "_" );
    }
}
