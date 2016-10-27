package io.subutai.core.strategy.impl;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Iterators;

import io.subutai.common.environment.Node;
import io.subutai.common.environment.NodeSchema;
import io.subutai.common.environment.Topology;
import io.subutai.common.peer.ContainerSize;
import io.subutai.core.strategy.api.RoundRobinStrategy;
import io.subutai.core.strategy.api.StrategyException;
import io.subutai.hub.share.quota.ContainerQuota;
import io.subutai.hub.share.resource.PeerGroupResources;
import io.subutai.hub.share.resource.PeerResources;


/**
 * Round robin container placement strategy implementation
 */
public class RoundRobinPlacementStrategy implements RoundRobinStrategy
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
        List<RoundRobinAllocator> allocators = new ArrayList<>();
        for ( PeerResources peerResources : peerGroupResources.getResources() )
        {
            RoundRobinAllocator resourceAllocator = new RoundRobinAllocator( peerResources );
            allocators.add( resourceAllocator );
        }

        if ( allocators.isEmpty() )
        {
            throw new StrategyException( "There are no resource hosts to place containers." );
        }

        final Iterator<RoundRobinAllocator> iterator = Iterators.cycle( allocators );
        // distribute node groups
        for ( NodeSchema nodeSchema : nodeSchemas )
        {
            String containerName = generateContainerName( nodeSchema );

            boolean allocated = false;
            int counter = 0;
            while ( counter < allocators.size() )
            {

                final RoundRobinAllocator resourceAllocator = iterator.next();
                allocated =
                        resourceAllocator.allocate( containerName, nodeSchema.getTemplateId(), nodeSchema.getSize() );
                if ( allocated )
                {
                    break;
                }
                counter++;
            }

            if ( !allocated )
            {
                throw new StrategyException(
                        "Could not allocate containers. There is no space for container: '" + containerName + "'" );
            }
        }

        Set<Node> nodes = new HashSet<>();

        for ( RoundRobinAllocator resourceAllocator : allocators )
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


    private String generateContainerName( final NodeSchema nodeSchema )
    {
        return nodeSchema.getName().replaceAll( "\\s+", "" );
    }
}
