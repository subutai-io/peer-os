package io.subutai.core.metric.api;


import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import io.subutai.common.metric.Alert;
import io.subutai.common.metric.HistoricalMetrics;
import io.subutai.common.metric.ResourceHostMetric;
import io.subutai.common.metric.ResourceHostMetrics;
import io.subutai.common.peer.AlertEvent;
import io.subutai.common.peer.AlertListener;
import io.subutai.common.peer.Host;
import io.subutai.common.peer.ResourceHost;
import io.subutai.core.metric.api.pojo.P2PInfo;


/**
 * Interface for monitor
 */
public interface Monitor
{

    ResourceHostMetrics getResourceHostMetrics();

    ResourceHostMetric getResourceHostMetric( ResourceHost resourceHost );

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


    String getHistoricalMetrics( final Host host, final Date startTime, final Date endTime );

    void putAlert( Alert alert );

    List<P2PInfo> getP2PStatuses();

    P2PInfo getP2pStatus( String rhId ) throws Exception;

    HistoricalMetrics getMetricsSeries( final Host host, Date startTime, Date endTime );
}
