package org.safehaus.subutai.api.containermanager;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.PlacementStrategy;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

/**
 * Created by timur on 9/4/14.
 */
public interface ContainerManager {

    public Set<Agent> clone(UUID envId, String templateName, int nodesCount, Collection<Agent> hosts,
                            PlacementStrategy... strategy) throws ContainerCreateException;

    public void clonesDestroy(final String hostName, final Set<String> cloneNames) throws ContainerDestroyException;

    public void clonesCreate(final String hostName, final String templateName, final Set<String> cloneNames)
            throws ContainerCreateException;
}
