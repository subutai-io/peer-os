package io.subutai.core.strategy.api;


import java.util.List;
import java.util.Map;

import io.subutai.common.environment.NodeSchema;
import io.subutai.common.environment.Topology;
import io.subutai.common.peer.ContainerSize;
import io.subutai.hub.share.quota.ContainerQuota;
import io.subutai.hub.share.resource.PeerGroupResources;


/**
 * Container placement strategy contains methods to distribute containers across resource hosts of specified peers
 */
public interface ContainerPlacementStrategy
{
    String getId();

    String getTitle();

    List<NodeSchema> getScheme();

    Topology distribute( String environmentName, List<NodeSchema> nodegroups, PeerGroupResources peerGroupResources,
                         Map<ContainerSize, ContainerQuota> quotas ) throws StrategyException;
}
