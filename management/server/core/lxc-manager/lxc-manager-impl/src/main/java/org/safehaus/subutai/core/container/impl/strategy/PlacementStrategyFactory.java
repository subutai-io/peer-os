package org.safehaus.subutai.core.container.impl.strategy;


import java.util.EnumSet;
import java.util.Set;

import org.safehaus.subutai.common.protocol.PlacementStrategy;
import org.safehaus.subutai.core.container.api.lxcmanager.LxcPlacementStrategy;


public class PlacementStrategyFactory
{

    private PlacementStrategyFactory()
    {
    }


    public static LxcPlacementStrategy create( int nodesCount, PlacementStrategy... strategy )
    {
        // round-robin is the default
        RoundRobinStrategy rr = new RoundRobinStrategy( nodesCount );
        if ( strategy == null || strategy.length == 0 )
        {
            return rr;
        }

        Set<PlacementStrategy> set = EnumSet.of( strategy[0], strategy );
        if ( set.contains( PlacementStrategy.ROUND_ROBIN ) )
        {
            return rr;
        }
        if ( set.contains( PlacementStrategy.FILLUP_PROCEED ) )
        {
            return new DefaultLxcPlacementStrategy( nodesCount );
        }

        return new BestServerStrategy( nodesCount, strategy );
    }


    public static PlacementStrategy getDefaultStrategyType()
    {
        return PlacementStrategy.ROUND_ROBIN;
    }
}
