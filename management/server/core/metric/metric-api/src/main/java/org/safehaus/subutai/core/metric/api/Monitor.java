package org.safehaus.subutai.core.metric.api;


import java.util.Set;
import java.util.UUID;


/**
 * Interface for monitor
 */
public interface Monitor
{
    /**
     * Returns current metrics of containers belonging to the given environment
     *
     * @param environmentId id of environment which containers' metrics to return
     *
     * @return set of metrics, one per each container within an environment
     */
    public Set<ContainerHostMetric> getContainerMetrics( UUID environmentId );


    /**
     * Returns current metrics of local resource hosts
     *
     * These metrics are to be used by container placement strategies in the context of local peer.
     *
     * Resource hosts are "visible" to the local peer only
     *
     * @return set of metrics, one per each resource host within the local peer
     */
    public Set<ResourceHostMetric> getResourceHostMetrics();
}
