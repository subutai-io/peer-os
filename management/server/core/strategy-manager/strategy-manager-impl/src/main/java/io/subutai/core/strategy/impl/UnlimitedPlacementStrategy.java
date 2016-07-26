package io.subutai.core.strategy.impl;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.environment.Node;
import io.subutai.common.environment.NodeSchema;
import io.subutai.common.environment.Topology;
import io.subutai.common.peer.ContainerSize;
import io.subutai.common.quota.ContainerQuota;
import io.subutai.common.resource.PeerGroupResources;
import io.subutai.common.resource.PeerResources;
import io.subutai.core.strategy.api.StrategyException;
import io.subutai.core.strategy.api.UnlimitedStrategy;


/**
 * Unlimited container placement strategy implementation
 */
public class UnlimitedPlacementStrategy implements UnlimitedStrategy
{
    private static final Logger LOGGER = LoggerFactory.getLogger( UnlimitedPlacementStrategy.class );

    private List<NodeSchema> scheme = new ArrayList<>();
    private static UnlimitedPlacementStrategy instance;


    public static UnlimitedPlacementStrategy getInstance()
    {
        if ( instance == null )
        {
            instance = new UnlimitedPlacementStrategy();
        }
        return instance;
    }


    public UnlimitedPlacementStrategy()
    {
        scheme.add( new NodeSchema( "master", ContainerSize.TINY, "master", 0, 0 ) );
        scheme.add( new NodeSchema( "hadoop", ContainerSize.SMALL, "hadoop", 0, 0 ) );
        scheme.add( new NodeSchema( "cassandra", ContainerSize.HUGE, "cassandra", 0, 0 ) );
    }


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
    public Topology distribute( final String environmentName, PeerGroupResources peerGroupResources,
                                Map<ContainerSize, ContainerQuota> quotas ) throws StrategyException
    {
        Topology result = new Topology( environmentName );

        Set<Node> nodes = distribute( getScheme(), peerGroupResources, quotas );
        for ( Node node : nodes )
        {
            result.addNodePlacement( node.getPeerId(), node );
        }

        return result;
    }


    @Override
    public Topology distribute( final String environmentName, final List<NodeSchema> nodeSchema,
                                final PeerGroupResources peerGroupResources,
                                final Map<ContainerSize, ContainerQuota> quotas ) throws StrategyException
    {
        Topology result = new Topology( environmentName );

        Set<Node> ng = distribute( nodeSchema, peerGroupResources, quotas );
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


    protected Set<Node> distribute( List<NodeSchema> nodeSchemas, PeerGroupResources peerGroupResources,
                                    Map<ContainerSize, ContainerQuota> quotas ) throws StrategyException
    {

        // build list of allocators
        List<RandomAllocator> allocators = new ArrayList<>();
        for ( PeerResources peerResources : peerGroupResources.getResources() )
        {
            RandomAllocator resourceAllocator = new RandomAllocator( peerResources );
            allocators.add( resourceAllocator );
        }

        if ( allocators.size() < 1 )
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

        Set<Node> nodes = new HashSet<>();

        for ( RandomAllocator resourceAllocator : allocators )
        {
            List<RandomAllocator.AllocatedContainer> containers = resourceAllocator.getContainers();
            if ( !containers.isEmpty() )
            {
                for ( RandomAllocator.AllocatedContainer container : containers )
                {
                    Node node = new Node( container.getName(), container.getName(), container.getTemplateName(),
                            container.getSize(), 0, 0, container.getPeerId(), container.getHostId() );
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
