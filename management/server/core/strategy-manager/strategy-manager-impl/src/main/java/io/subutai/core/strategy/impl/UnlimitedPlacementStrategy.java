package io.subutai.core.strategy.impl;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.environment.NodeGroup;
import io.subutai.common.environment.Topology;
import io.subutai.common.peer.ContainerSize;
import io.subutai.common.quota.ContainerQuota;
import io.subutai.common.resource.PeerGroupResources;
import io.subutai.common.resource.PeerResources;
import io.subutai.core.strategy.api.ExampleStrategy;
import io.subutai.core.strategy.api.NodeSchema;
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
        scheme.add( new NodeSchema( "master", ContainerSize.TINY, "master" ) );
        scheme.add( new NodeSchema( "hadoop", ContainerSize.SMALL, "hadoop" ) );
        scheme.add( new NodeSchema( "cassandra", ContainerSize.HUGE, "cassandra" ) );
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
    public Topology distribute( final String environmentName, final int sshGroupId, final int hostGroupId,
                                PeerGroupResources peerGroupResources, Map<ContainerSize, ContainerQuota> quotas )
            throws StrategyException
    {
        Topology result = new Topology( environmentName, sshGroupId, hostGroupId );

        Set<NodeGroup> nodeGroups = distribute( getScheme(), peerGroupResources, quotas );
        for ( NodeGroup nodeGroup : nodeGroups )
        {
            result.addNodeGroupPlacement( nodeGroup.getPeerId(), nodeGroup );
        }

        return result;
    }


    @Override
    public Topology distribute( final String environmentName, final int sshGroupId, final int hostGroupId,
                                final List<NodeSchema> nodeSchema, final PeerGroupResources peerGroupResources,
                                final Map<ContainerSize, ContainerQuota> quotas ) throws StrategyException
    {
        Topology result = new Topology( environmentName, sshGroupId, hostGroupId );

        Set<NodeGroup> ng = distribute( nodeSchema, peerGroupResources, quotas );
        for ( NodeGroup nodeGroup : ng )
        {
            result.addNodeGroupPlacement( nodeGroup.getPeerId(), nodeGroup );
        }

        return result;
    }


    @Override
    public List<NodeSchema> getScheme()
    {
        return scheme;
    }


    protected Set<NodeGroup> distribute( List<NodeSchema> nodeSchemas, PeerGroupResources peerGroupResources,
                                         Map<ContainerSize, ContainerQuota> quotas ) throws StrategyException
    {

        // build list of allocators
        List<RandomAllocator> allocators = new ArrayList<>();
        for ( PeerResources peerResources : peerGroupResources.getResources() )
        {
            if ( peerResources.getEnvironmentLimit() > 0 )
            {
                RandomAllocator resourceAllocator = new RandomAllocator( peerResources );
                allocators.add( resourceAllocator );
            }
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

        Set<NodeGroup> nodeGroups = new HashSet<>();

        for ( RandomAllocator resourceAllocator : allocators )
        {
            List<RandomAllocator.AllocatedContainer> containers = resourceAllocator.getContainers();
            if ( !containers.isEmpty() )
            {
                for ( RandomAllocator.AllocatedContainer container : containers )
                {
                    NodeGroup nodeGroup =
                            new NodeGroup( container.getName(), container.getTemplateName(), container.getSize(), 0, 0,
                                    container.getPeerId(), container.getHostId() );
                    nodeGroups.add( nodeGroup );
                }
            }
        }


        return nodeGroups;
    }


    private List<RandomAllocator> getPreferredAllocators( final List<RandomAllocator> allocators )
    {
        List<RandomAllocator> result = new ArrayList<>( allocators );
        Collections.shuffle( result );
        return result;
    }


    private String generateContainerName( final NodeSchema nodeSchema )
    {
        return nodeSchema.getName().replaceAll( "\\s+", "_" );
    }
}
