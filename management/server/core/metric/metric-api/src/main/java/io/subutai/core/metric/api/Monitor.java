package io.subutai.core.metric.api;


import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.subutai.common.metric.BaseMetric;
import io.subutai.common.metric.HistoricalMetric;
import io.subutai.common.metric.MetricType;
import io.subutai.common.metric.ProcessResourceUsage;
import io.subutai.common.metric.ResourceHostMetrics;
import io.subutai.common.peer.AlertListener;
import io.subutai.common.peer.AlertPack;
import io.subutai.common.peer.ContainerId;
import io.subutai.common.peer.Host;


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
     * @param metricType to be retrieved for historical data
     */
    public List<HistoricalMetric> getHistoricalMetric( Host host, MetricType metricType );


    /**
     * @param hosts physical or container hosts to be monitored
     * @param metricType to be retrieved for historical data
     */
    public Map<String, List<HistoricalMetric>> getHistoricalMetrics( Collection<Host> hosts, MetricType metricType );

    ResourceHostMetrics getResourceHostMetrics();

    BaseMetric getHostMetric( String id );

    Collection<BaseMetric> getMetrics();

    Collection<AlertPack> getAlerts();

    void addAlertListener( AlertListener alertListener );

    void removeAlertListener( AlertListener alertListener );

    Collection<AlertListener> getAlertListeners();

    Set<AlertPack> getAlertPackages();

    void notifyAlertListeners();

    void addAlert( AlertPack alert );
}
