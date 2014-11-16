package org.safehaus.subutai.core.strategy.impl;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.safehaus.subutai.core.strategy.api.ContainerPlacementStrategy;
import org.safehaus.subutai.common.protocol.Criteria;
import org.safehaus.subutai.core.strategy.api.ServerMetric;
import org.safehaus.subutai.core.strategy.api.StrategyException;
import org.safehaus.subutai.core.strategy.api.StrategyManager;
import org.safehaus.subutai.core.strategy.api.StrategyNotAvailable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Strategy Manager implementation
 */
public class StrategyManagerImpl implements StrategyManager
{
    private static final Logger LOG = LoggerFactory.getLogger( StrategyManagerImpl.class );


    List<ContainerPlacementStrategy> placementStrategies =
            Collections.synchronizedList( new ArrayList<ContainerPlacementStrategy>() );


    public void destroy()
    {
        placementStrategies.clear();
    }


    public synchronized void registerStrategy( ContainerPlacementStrategy containerPlacementStrategy )
    {
        LOG.info( String.format( "Registering container placement strategy: %s", containerPlacementStrategy.getId() ) );
        placementStrategies.add( containerPlacementStrategy );
    }


    public synchronized void unregisterStrategy( ContainerPlacementStrategy containerPlacementStrategy )
    {
        if ( containerPlacementStrategy != null )
        {
            LOG.info( String.format( "Unregistering container placement strategy: %s",
                    containerPlacementStrategy.getId() ) );
            placementStrategies.remove( containerPlacementStrategy );
        }
    }


    public List<ContainerPlacementStrategy> getPlacementStrategies()
    {
        return placementStrategies;
    }


    @Override
    public Map<ServerMetric, Integer> getPlacementDistribution( List<ServerMetric> serverMetrics, int nodesCount,
                                                         String strategyId, List<Criteria> criteria )
            throws StrategyException
    {
        ContainerPlacementStrategy containerPlacementStrategy = findStrategyById( strategyId );

        containerPlacementStrategy.calculatePlacement( nodesCount, serverMetrics, criteria );

        Map<ServerMetric, Integer> result = containerPlacementStrategy.getPlacementDistribution();
        int totalSlots = 0;

        for ( int slotCount : result.values() )
        {
            totalSlots += slotCount;
        }

        if ( totalSlots < nodesCount )
        {
            throw new StrategyException( String.format( "Only %d containers can be created", totalSlots ) );
        }

        if ( totalSlots > nodesCount )
        {
            throw new StrategyException(
                    String.format( "Total count of slots [%d] more than requested [%d].", totalSlots, nodesCount ) );
        }

        return result;
    }


    @Override
    public ContainerPlacementStrategy findStrategyById( String strategyId ) throws StrategyNotAvailable
    {
        ContainerPlacementStrategy placementStrategy = null;
        for ( int i = 0; i < placementStrategies.size() && placementStrategy == null; i++ )
        {
            if ( strategyId.equals( placementStrategies.get( i ).getId() ) )
            {
                placementStrategy = placementStrategies.get( i );
            }
        }
        if ( placementStrategy == null )
        {
            throw new StrategyNotAvailable(
                    String.format( "Container placement strategy [%s] not available.", strategyId ) );
        }
        return placementStrategy;
    }
}
