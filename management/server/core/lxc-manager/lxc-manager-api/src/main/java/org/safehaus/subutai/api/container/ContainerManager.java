package org.safehaus.subutai.api.container;

import java.util.Collection;
import java.util.Set;
import org.safehaus.subutai.api.manager.helper.PlacementStrategyENUM;
import org.safehaus.subutai.shared.protocol.Agent;

public interface ContainerManager {

    public Set<Agent> clone(Collection<String> hostNames, String templateName,
            int nodescount, PlacementStrategyENUM... strategy);

}
