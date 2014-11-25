package org.safehaus.subutai.core.metric.impl;


import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.core.metric.api.MonitoringSettings;


/**
 * Commands for Monitor
 */
public class Commands
{


    public RequestBuilder getCurrentMetricCommand( String hostname )
    {
        return new RequestBuilder( String.format( "subutai monitor %s", hostname ) );
    }


    public RequestBuilder getActivateMonitoringCommand( String hostname, MonitoringSettings monitoringSettings )
    {
        return new RequestBuilder( String.format(
                "subutai monitor -c -p \" metricCollectionIntervalInMin:%s, maxSampleCount:%s, "
                        + "metricCountToAverageToAlert:%s, intervalBetweenAlertsInMin:%s, ramAlertThreshold:%s, "
                        + "cpuAlertThreshold:%s, diskThreshold:%s \" %s",
                monitoringSettings.getMetricCollectionIntervalInMin(), monitoringSettings.getMaxSampleCount(),
                monitoringSettings.getMetricCountToAverageToAlert(), monitoringSettings.getIntervalBetweenAlertsInMin(),
                monitoringSettings.getRamAlertThreshold(), monitoringSettings.getCpuAlertThreshold(),
                monitoringSettings.getDiskAlertThreshold(), hostname ) );
    }
}
