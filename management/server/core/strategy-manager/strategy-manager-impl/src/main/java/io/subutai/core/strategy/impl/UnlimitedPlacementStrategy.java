package io.subutai.core.strategy.impl;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.subutai.common.environment.Node;
import io.subutai.common.environment.NodeSchema;
import io.subutai.common.environment.Topology;
import io.subutai.common.peer.ContainerSize;
import io.subutai.core.strategy.api.StrategyException;
import io.subutai.core.strategy.api.UnlimitedStrategy;
import io.subutai.hub.share.quota.ContainerQuota;
import io.subutai.hub.share.resource.PeerGroupResources;
import io.subutai.hub.share.resource.PeerResources;


/**
 * Unlimited container placement strategy implementation
 */
public class UnlimitedPlacementStrategy implements UnlimitedStrategy
{

    private List<NodeSchema> scheme = new ArrayList<>();


    @Override
    public String getId()
    {
        return ID;
    }


    @Override
    public String getTitle()
    {
        return "Unlimited container placement strategy.";
    }


    @Override
    public Topology distribute( final String environmentName, final List<NodeSchema> nodeSchema,
                                final PeerGroupResources peerGroupResources,
                                final Map<ContainerSize, ContainerQuota> quotas ) throws StrategyException
    {
        Topology result = new Topology( environmentName );

        Set<Node> ng = distribute( nodeSchema, peerGroupResources );
        for ( Node node : ng )
        {
            result.addNodePlacement( node.getPeerId(), node );
        }

        return result;
    }


    @Override
    public List<NodeSchema> getScheme()
    {
        return scheme;
    }


    protected Set<Node> distribute( List<NodeSchema> nodeSchemas, PeerGroupResources peerGroupResources )
            throws StrategyException
    {

        // build list of allocators
        List<RandomAllocator> allocators = new ArrayList<>();
        for ( PeerResources peerResources : peerGroupResources.getResources() )
        {
            RandomAllocator resourceAllocator = new RandomAllocator( peerResources );
            allocators.add( resourceAllocator );
        }

        if ( allocators.isEmpty() )
        {
            throw new StrategyException( "There are no resource hosts to place containers." );
        }

        // distribute node groups
        for ( NodeSchema nodeSchema : nodeSchemas )
        {
            String containerName = generateContainerName( nodeSchema );

            // select preferred peer
            List<RandomAllocator> preferredAllocators = getPreferredAllocators( allocators );
            boolean allocated = false;
            for ( RandomAllocator resourceAllocator : preferredAllocators )
            {
                allocated =
                        resourceAllocator.allocate( containerName, nodeSchema.getTemplateId(), nodeSchema.getSize() );
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

        Set<Node> nodes = new HashSet<>();

        for ( RandomAllocator resourceAllocator : allocators )
        {
            List<AllocatedContainer> containers = resourceAllocator.getContainers();
            if ( !containers.isEmpty() )
            {
                for ( AllocatedContainer container : containers )
                {
                    Node node = new Node( container.getName(), container.getName(), container.getSize(),
                            container.getPeerId(), container.getHostId(), container.getTemplateId() );

                    nodes.add( node );
                }
            }
        }


        return nodes;
    }


    private List<RandomAllocator> getPreferredAllocators( final List<RandomAllocator> allocators )
    {
        List<RandomAllocator> result = new ArrayList<>( allocators );
        Collections.shuffle( result );
        return result;
    }


    private String generateContainerName( final NodeSchema nodeSchema )
    {
        return nodeSchema.getName().replaceAll( "\\s+", "" );
    }
}
