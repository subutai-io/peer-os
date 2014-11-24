package org.safehaus.subutai.core.metric.impl;


import org.safehaus.subutai.common.command.RequestBuilder;


/**
 * Commands for Monitor
 */
public class Commands
{


    public RequestBuilder getCurrentMetricCommand( String hostname )
    {
        return new RequestBuilder( String.format( "subutai monitor %s", hostname ) );
    }


    public RequestBuilder getActivateMonitoringWithCustomSettingsCommand( String hostname,
                                                                          int metricCollectionIntervalInMin,
                                                                          int maxSampleCount,
                                                                          int metricCountToAverageToAlert,
                                                                          int intervalBetweenAlertsInMin,
                                                                          double ramAlertThreshold,
                                                                          double cpuAlertThreshold,
                                                                          double diskThreshold )
    {
        return new RequestBuilder( String.format(
                "subutai monitor -c -p \" metricCollectionIntervalInMin:%s, maxSampleCount:%s, "
                        + "metricCountToAverageToAlert:%s, intervalBetweenAlertsInMin:%s, ramAlertThreshold:%s, "
                        + "cpuAlertThreshold:%s, diskThreshold:%s \" %s", metricCollectionIntervalInMin, maxSampleCount,
                metricCountToAverageToAlert, intervalBetweenAlertsInMin, ramAlertThreshold, cpuAlertThreshold,
                diskThreshold, hostname ) );
    }


    public RequestBuilder getActivateMonitoringWithDefaultSettingsCommand( String hostname )
    {
        return new RequestBuilder( String.format( "subutai monitor -c %s", hostname ) );
    }
}
