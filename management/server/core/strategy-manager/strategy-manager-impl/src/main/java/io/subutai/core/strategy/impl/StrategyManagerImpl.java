package io.subutai.core.strategy.impl;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import io.subutai.core.strategy.api.ContainerPlacementStrategy;
import io.subutai.core.strategy.api.StrategyManager;
import io.subutai.core.strategy.api.StrategyNotFoundException;


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


    @Override
    public List<ContainerPlacementStrategy> getPlacementStrategies()
    {
        return placementStrategies;
    }


    @Override
    public ContainerPlacementStrategy findStrategyById( String strategyId ) throws StrategyNotFoundException
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
            throw new StrategyNotFoundException(
                    String.format( "Container placement strategy [%s] not available.", strategyId ) );
        }
        return placementStrategy;
    }


    @Override
    public List<String> getPlacementStrategyTitles()
    {
        return this.getPlacementStrategies().stream().filter( n -> !Strings.isNullOrEmpty( n.getId() ) )
                   .map( ContainerPlacementStrategy::getId ).collect( Collectors.toList() );
    }
}
