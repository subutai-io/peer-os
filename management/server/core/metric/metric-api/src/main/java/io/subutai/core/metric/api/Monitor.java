package io.subutai.core.metric.api;


import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import io.subutai.common.metric.Alert;
import io.subutai.common.metric.ProcessResourceUsage;
import io.subutai.common.metric.ResourceHostMetrics;
import io.subutai.common.peer.AlertEvent;
import io.subutai.common.peer.AlertListener;
import io.subutai.common.peer.ContainerId;
import io.subutai.common.peer.Host;
import io.subutai.common.resource.HistoricalMetrics;


/**
 * Interface for monitor
 */
public interface Monitor
{
    /**
     * Returns process resource usage on a given container host
     *
     * @param containerId - container ID
     * @param pid - process ID
     *
     * @return - {@code ProcessResourceUsage}
     */

    ProcessResourceUsage getProcessResourceUsage( ContainerId containerId, int pid ) throws MonitorException;

    /**
     * @param host physical or container host to be monitored
     */
    HistoricalMetrics getHistoricalMetrics( Host host, Date startTime, Date endTime );

    ResourceHostMetrics getResourceHostMetrics();

    /**
     * Returns the list of alerts
     */
    Collection<AlertEvent> getAlertEvents();

    /**
     * Adds alert event
     */
    void addAlert( AlertEvent alert );

    /**
     * Returns the queue of alerts emitted from local peer
     */
    List<AlertEvent> getAlertsQueue();

    /**
     * Returns the list of alert listeners
     */
    Set<AlertListener> getAlertListeners();


    String getPlainHistoricalMetrics( final Host host, final Date startTime, final Date endTime );

    void putAlert( Alert alert );
}
