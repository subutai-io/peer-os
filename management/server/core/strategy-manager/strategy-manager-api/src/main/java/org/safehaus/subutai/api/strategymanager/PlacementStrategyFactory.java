package org.safehaus.subutai.api.strategymanager;

import org.safehaus.subutai.common.protocol.PlacementStrategy;

import java.util.List;

public interface PlacementStrategyFactory {
    public AbstractContainerPlacementStrategy create(int nodesCount, PlacementStrategy strategy, List<Criteria> criteria);
    public PlacementStrategy getDefaultStrategyType();
    public AbstractContainerPlacementStrategy getDefaultStrategy(int nodesCount, List<Criteria> criteria);
}
