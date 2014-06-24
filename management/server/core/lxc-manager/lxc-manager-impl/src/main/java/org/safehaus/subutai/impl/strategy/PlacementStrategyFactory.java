package org.safehaus.subutai.impl.strategy;

import org.safehaus.subutai.api.lxcmanager.LxcPlacementStrategy;
import org.safehaus.subutai.api.manager.helper.PlacementStrategyENUM;

public class PlacementStrategyFactory {

    private PlacementStrategyFactory() {
    }

    public static PlacementStrategyFactory getInstance() {
        return PlacementStrategyFactoryHolder.INSTANCE;
    }

    public LxcPlacementStrategy create(PlacementStrategyENUM strategy) {
        LxcPlacementStrategy st = new DefaultLxcPlacementStrategy(1);
        return st;
    }

    private static class PlacementStrategyFactoryHolder {

        private static final PlacementStrategyFactory INSTANCE = new PlacementStrategyFactory();
    }
}
