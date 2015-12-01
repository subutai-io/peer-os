package io.subutai.core.metric.api;


import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import io.subutai.common.metric.BaseMetric;
import io.subutai.common.metric.ProcessResourceUsage;
import io.subutai.common.metric.ResourceHostMetrics;
import io.subutai.common.peer.AlertListener;
import io.subutai.common.peer.AlertPack;
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
     * This method is called by REST endpoint from local peer indicating that some container hosted locally is under
     * stress.
     *
     * @param alertMetric - body of alert in JSON
     */
    //    public void alert( String alertMetric ) throws MonitorException;


    /**
     * @param host physical or container host to be monitored
     */
    public HistoricalMetrics getHistoricalMetrics( Host host, Date startTime, Date endTime );


//    /**
//     * @param hosts physical or container hosts to be monitored
//     * @param resourceType to be retrieved for historical data
//     */
//    public Map<String, List<HistoricalMetric>> getHistoricalMetrics( Collection<Host> hosts,
//                                                                     ResourceType resourceType );

    ResourceHostMetrics getResourceHostMetrics();

    BaseMetric getHostMetric( String id );

    Collection<BaseMetric> getMetrics();

    Collection<AlertPack> getAlerts();

    void addAlertListener( AlertListener alertListener );

    void removeAlertListener( AlertListener alertListener );

    Collection<AlertListener> getAlertListeners();

    List<AlertPack> getAlertPackages();

    void notifyAlertListeners();

    void addAlert( AlertPack alert );

    void deliverAlerts();

    List<AlertPack> getAlertsQueue();
}
