package org.safehaus.subutai.core.metric.api;


import java.util.Set;

import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.monitor.api.MonitorException;


/**
 * Interface for monitor
 */
public interface Monitor
{
    /**
     * Returns current metrics of containers belonging to the given environment
     *
     * @param environment environment which containers' metrics to return
     *
     * @return set of metrics, one per each container within an environment
     */
    public Set<ContainerHostMetric> getContainerMetrics( Environment environment ) throws MonitorException;


    /**
     * Returns current metrics of local resource hosts
     *
     * These metrics are to be used by container placement strategies in the context of local peer and for heartbeats to
     * HUB.
     *
     * Resource hosts are "visible" to the local peer only
     *
     * @return set of metrics, one per each resource host within the local peer
     */
    public Set<ResourceHostMetric> getResourceHostMetrics() throws MonitorException;


    /**
     * Enables {@code MetricListener} to be triggered if thresholds on some containers within the given environment are
     * exceeded
     *
     * @param metricListener metricListener  to trigger
     * @param environment environment to monitor
     */

    public void startMonitoring( MetricListener metricListener, Environment environment ) throws MonitorException;

    /**
     * Disables {@code MetricListener} to be triggered for the given environment
     *
     * @param metricListener metricListener  to trigger
     * @param environment environment to monitor
     */
    public void stopMonitoring( MetricListener metricListener, Environment environment ) throws MonitorException;

    /**
     * This method is called by REST endpoint from local peer indicating that some container hosted locally is under
     * stress.
     *
     * @param alertBody - body of alert in JSON
     */
    public void alertThresholdExcess( String alertBody ) throws MonitorException;

    /**
     * This methods is called by REST endpoint when a remote peer sends an alert from one of its hosted containers
     * belonging to this peer
     *
     * @param metric - {@code ContainerHostMetric} metric of the host where thresholds are being exceeded
     */
    public void alertThresholdExcess( ContainerHostMetric metric ) throws MonitorException;
}
