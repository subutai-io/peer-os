package org.safehaus.subutai.core.strategy.impl;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.strategy.api.ContainerPlacementStrategy;
import org.safehaus.subutai.core.strategy.api.Criteria;
import org.safehaus.subutai.core.strategy.api.ServerMetric;
import org.safehaus.subutai.core.strategy.api.StrategyException;
import org.safehaus.subutai.core.strategy.api.StrategyManager;
import org.safehaus.subutai.core.strategy.api.StrategyNotAvailable;


/**
 * Created by timur on 9/18/14.
 */
public class StrategyManagerImpl implements StrategyManager {

    List<ContainerPlacementStrategy> placementStrategies =
            Collections.synchronizedList( new ArrayList<ContainerPlacementStrategy>() );


    public StrategyManagerImpl()
    {
        init();
    }


    public void init()
    {
    }


    public void destroy()
    {
        placementStrategies.clear();
    }


    public synchronized void registerStrategy( ContainerPlacementStrategy containerPlacementStrategy )
    {
        System.out.println(
                String.format( "Registering container placement strategy: %s", containerPlacementStrategy.getId() ) );
        placementStrategies.add( containerPlacementStrategy );
    }


    public synchronized void unregisterStrategy( ContainerPlacementStrategy containerPlacementStrategy )
    {
        if ( containerPlacementStrategy != null )
        {
            System.out.println( String.format( "Unregistering container placement strategy: %s",
                    containerPlacementStrategy.getId() ) );
            placementStrategies.remove( containerPlacementStrategy );
        }
    }


    public List<ContainerPlacementStrategy> getPlacementStrategies()
    {
        return placementStrategies;
    }


    @Override
    public Map<Agent, Integer> getPlacementDistribution( Map<Agent, ServerMetric> serverMetrics, int nodesCount,
                                                         String strategyId, List<Criteria> criteria )
            throws StrategyException
    {
        ContainerPlacementStrategy containerPlacementStrategy = findStrategyById( strategyId );
        if ( containerPlacementStrategy == null )
        {
            throw new StrategyNotAvailable(
                    String.format( "Container placement strategy [%s] not available.", strategyId ) );
        }

        containerPlacementStrategy.calculatePlacement( nodesCount, serverMetrics, criteria );

        Map<Agent, Integer> result = containerPlacementStrategy.getPlacementDistribution();
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
    public ContainerPlacementStrategy findStrategyById( String strategyId )
    {
        ContainerPlacementStrategy placementStrategy = null;
        for ( int i = 0; i < placementStrategies.size() && placementStrategy == null; i++ )
        {
            if ( strategyId.equals( placementStrategies.get( i ).getId() ) )
            {
                placementStrategy = placementStrategies.get( i );
            }
        }
        return placementStrategy;
    }
}
