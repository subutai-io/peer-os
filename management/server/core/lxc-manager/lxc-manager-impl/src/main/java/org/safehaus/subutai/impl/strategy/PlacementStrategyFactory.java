package org.safehaus.subutai.impl.strategy;

import org.safehaus.subutai.api.lxcmanager.LxcPlacementStrategy;
import org.safehaus.subutai.api.manager.helper.PlacementStrategyENUM;

public class PlacementStrategyFactory {

    public static LxcPlacementStrategy create(int nodesCount, PlacementStrategyENUM... strategy) {
        if(strategy == null || strategy.length == 0)
            return new DefaultLxcPlacementStrategy(1);

        return new BestServerStrategy(nodesCount, strategy);
    }

}
