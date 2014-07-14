package org.safehaus.subutai.api.container;

import java.util.*;
import java.util.concurrent.TimeUnit;
import org.safehaus.subutai.api.manager.helper.PlacementStrategyENUM;
import org.safehaus.subutai.shared.protocol.Agent;

public interface ContainerManager {

    public Set<Agent> clone(UUID envId, String templateName, int nodesCount, Collection<Agent> hosts, PlacementStrategyENUM... strategy);

    public boolean attachAndExecute(Agent physicalHost, String cloneName, String cmd);

    public boolean attachAndExecute(Agent physicalHost, String cloneName, String cmd, long t, TimeUnit unit);

}
