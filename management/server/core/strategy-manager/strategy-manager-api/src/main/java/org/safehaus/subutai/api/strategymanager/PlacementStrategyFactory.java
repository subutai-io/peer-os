package org.safehaus.subutai.api.strategymanager;

import org.safehaus.subutai.common.protocol.PlacementStrategy;

import java.util.List;

public interface PlacementStrategyFactory {
    public ContainerPlacementStrategy create(int nodesCount, PlacementStrategy strategy, List<Criteria> criteria);
    public PlacementStrategy getDefaultStrategyType();
    public ContainerPlacementStrategy getDefaultStrategy(int nodesCount, List<Criteria> criteria);
}
