package org.safehaus.subutai.core.metric.api;


import java.util.Set;

import org.safehaus.subutai.common.environment.Environment;
import org.safehaus.subutai.common.metric.ProcessResourceUsage;
import org.safehaus.subutai.common.peer.ContainerHost;


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
    public Set<ContainerHostMetric> getContainerHostsMetrics( Environment environment ) throws MonitorException;


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
    public Set<ResourceHostMetric> getResourceHostsMetrics() throws MonitorException;


    /**
     * Enables {@code AlertListener} to be triggered if thresholds on some containers within the given environment are
     * exceeded. Monitoring infrastructure is initialized with given monitoring settings. This call needs to be executed
     * only once since subscription is stored in persistent storage
     *
     * @param alertListener alertListener  to trigger
     * @param environment environment to monitor
     * @param monitoringSettings monitoring settings
     */

    public void startMonitoring( AlertListener alertListener, Environment environment,
                                 MonitoringSettings monitoringSettings ) throws MonitorException;

    /**
     * Enables {@code AlertListener} to be triggered if thresholds on the provided container are exceeded. Monitoring
     * infrastructure is initialized with given monitoring settings.
     *
     * @param alertListener alertListener  to trigger
     * @param containerHost container host to activate monitoring on and listen to alerts from
     * @param monitoringSettings monitoring settings
     */
    public void startMonitoring( AlertListener alertListener, ContainerHost containerHost,
                                 MonitoringSettings monitoringSettings ) throws MonitorException;

    /**
     * Disables {@code AlertListener} to be triggered for the given environment
     *
     * @param alertListener alertListener  to trigger
     * @param environment environment to monitor
     */
    public void stopMonitoring( AlertListener alertListener, Environment environment ) throws MonitorException;


    /**
     * Activates monitoring on a given container. However interested party must be subscribed to the container's
     * environment alerts to receive them
     *
     * @param containerHost container host to activate monitoring on
     * @param monitoringSettings monitoring settings
     */

    public void activateMonitoring( ContainerHost containerHost, MonitoringSettings monitoringSettings )
            throws MonitorException;


    public ProcessResourceUsage getProcessResourceUsage( ContainerHost containerHost, int processPid )
            throws MonitorException;

    /**
     * This method is called by REST endpoint from local peer indicating that some container hosted locally is under
     * stress.
     *
     * @param alertMetric - body of alert in JSON
     *
     *
     * TODO take this method to separate interface for by-REST only usage
     */
    public void alert( String alertMetric ) throws MonitorException;

    /**
     * Adds listener to be notified if threshold within environment is exceeded (after this call, interested parties
     * need to execute startMonitoring call passing some environment under interest). Usually one calls this method in
     * init method of client module
     *
     * @param listener - listener
     */
    public void addAlertListener( AlertListener listener );

    /**
     * Removes listener
     *
     * @param listener - listener
     */
    public void removeAlertListener( AlertListener listener );
}
