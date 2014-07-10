package org.safehaus.subutai.impl.strategy;

import java.util.EnumSet;
import java.util.Set;
import org.safehaus.subutai.api.lxcmanager.LxcPlacementStrategy;
import org.safehaus.subutai.api.manager.helper.PlacementStrategyENUM;

public class PlacementStrategyFactory {

    public static LxcPlacementStrategy create(int nodesCount, PlacementStrategyENUM... strategy) {
        // round-robin is the default
        RoundRobinStrategy rr = new RoundRobinStrategy(nodesCount);
        if(strategy == null || strategy.length == 0) return rr;

        Set<PlacementStrategyENUM> set = EnumSet.of(strategy[0], strategy);
        if(set.contains(PlacementStrategyENUM.ROUND_ROBIN)) return rr;
        if(set.contains(PlacementStrategyENUM.FILLUP_PROCEED))
            return new DefaultLxcPlacementStrategy(nodesCount);

        return new BestServerStrategy(nodesCount, strategy);
    }

    public static PlacementStrategyENUM getDefaultStrategyType() {
        return PlacementStrategyENUM.ROUND_ROBIN;
    }

}
