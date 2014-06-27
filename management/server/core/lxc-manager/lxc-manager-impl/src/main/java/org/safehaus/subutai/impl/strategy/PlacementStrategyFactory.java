package org.safehaus.subutai.impl.strategy;

import java.util.EnumSet;
import java.util.Set;
import org.safehaus.subutai.api.lxcmanager.LxcPlacementStrategy;
import org.safehaus.subutai.api.manager.helper.PlacementStrategyENUM;

public class PlacementStrategyFactory {

    public static LxcPlacementStrategy create(int nodesCount, PlacementStrategyENUM... strategy) {
        if(strategy == null || strategy.length == 0)
            return new DefaultLxcPlacementStrategy(nodesCount);

        Set<PlacementStrategyENUM> set = EnumSet.of(strategy[0], strategy);
        if(set.contains(PlacementStrategyENUM.ROUND_ROBIN))
            return new RoundRobinStrategy(nodesCount);

        return new BestServerStrategy(nodesCount, strategy);
    }

}
