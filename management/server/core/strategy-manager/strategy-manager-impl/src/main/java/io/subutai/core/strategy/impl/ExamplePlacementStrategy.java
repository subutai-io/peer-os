package io.subutai.core.strategy.impl;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.environment.Blueprint;
import io.subutai.common.environment.NodeGroup;
import io.subutai.common.environment.Topology;
import io.subutai.common.peer.ContainerSize;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.quota.ContainerQuota;
import io.subutai.common.resource.PeerGroupResources;
import io.subutai.common.resource.PeerResources;
import io.subutai.core.strategy.api.ContainerPlacementStrategy;
import io.subutai.core.strategy.api.ExampleStrategy;
import io.subutai.core.strategy.api.NodeSchema;
import io.subutai.core.strategy.api.StrategyException;


/**
 * Master
 */
public class ExamplePlacementStrategy implements ExampleStrategy
{
    private static final Logger LOGGER = LoggerFactory.getLogger( ExamplePlacementStrategy.class );

    private List<NodeSchema> scheme = new ArrayList<>();
    private static ExamplePlacementStrategy instance;


    //    static
    //    {
    ///*
    //        scheme.add( new NodeSchema( "Huge master", ContainerSize.HUGE, "master" ) );
    //        scheme.add( new NodeSchema( "Large master", ContainerSize.LARGE, "master" ) );
    //        scheme.add( new NodeSchema( "Medium master", ContainerSize.MEDIUM, "master" ) );
    //*/
    //        scheme.add( new NodeSchema( "master", ContainerSize.TINY, "master" ) );
    //        scheme.add( new NodeSchema( "hadoop", ContainerSize.TINY, "hadoop" ) );
    //        scheme.add( new NodeSchema( "cassandra", ContainerSize.TINY, "cassandra" ) );
    //    }


    public static ExamplePlacementStrategy getInstance()
    {
        if ( instance == null )
        {
            instance = new ExamplePlacementStrategy();
        }
        return instance;
    }


    public ExamplePlacementStrategy()
    {
        scheme.add( new NodeSchema( "master", ContainerSize.TINY, "master" ) );
        scheme.add( new NodeSchema( "hadoop", ContainerSize.TINY, "hadoop" ) );
        scheme.add( new NodeSchema( "cassandra", ContainerSize.TINY, "cassandra" ) );
    }


    @Override
    public String getId()
    {
        return ID;
    }


    @Override
    public String getTitle()
    {
        return "Example container placement strategy.";
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
        for ( NodeSchema nodeSchema : nodeSchemas )
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
                            new NodeGroup( container.getName(), container.getTemplateName(), container.getSize(), 0, 0,
                                    container.getPeerId(), container.getHostId() );
                    nodeGroups.add( nodeGroup );
                }
            }
        }


        return nodeGroups;
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


    //    protected Topology buildTopology( final Blueprint blueprint, final String cidr, final String strategyId )
    //            throws StrategyException, PeerException
    //    {
    //        LOGGER.debug( "Building topology..." );
    //
    //        Topology topology = new Topology( blueprint.getName(), cidr, blueprint.getSshKey() );
    //
    //        Set<String> peers = blueprint.getPeers();
    //        ContainerPlacementStrategy strategy = strategyManager.findStrategyById( strategyId );
    //        PeerGroupResources groupResources = new PeerGroupResources();
    //        for ( String peerId : peers )
    //        {
    //            Peer peer = peerManager.getPeer( peerId );
    //
    //
    //            groupResources.addPeerResources( peer.getResourceLimits( peerManager.getLocalPeer().getId() ) );
    //        }
    //
    //
    //        final Map<ContainerSize, ContainerQuota> quotas = quotaManager.getDefaultQuotas();
    //        Blueprint newBlueprint = strategy.distribute( groupResources, quotas );
    //
    //        for ( Map.Entry<String, Set<NodeGroup>> placementEntry : newBlueprint.getNodeGroupsMap().entrySet() )
    //        {
    //            Peer peer = peerManager.getPeer( placementEntry.getKey() );
    //            for ( NodeGroup nodeGroup : placementEntry.getValue() )
    //            {
    //                topology.addNodeGroupPlacement( peer, nodeGroup );
    //            }
    //        }
    //
    //
    //        LOGGER.debug( "Topology built." );
    //
    //        return topology;
    //    }
}
