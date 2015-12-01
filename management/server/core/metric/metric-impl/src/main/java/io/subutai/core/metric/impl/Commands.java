package io.subutai.core.metric.impl;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.subutai.common.command.RequestBuilder;
import io.subutai.common.peer.Host;
import io.subutai.common.metric.MetricType;
import io.subutai.core.metric.api.MonitoringSettings;


/**
 * Commands for Monitor
 */
public class Commands
{

    public RequestBuilder getCurrentMetricCommand( String hostname )
    {
        return new RequestBuilder( String.format( "subutai stats system %s", hostname ) );
    }


    public RequestBuilder getContainerHostQuotaCommand( String hostname )
    {
        return new RequestBuilder( String.format( "subutai stats quota %s", hostname ) );
    }


    public RequestBuilder getHistoricalMetricCommandOld( Host host, MetricType metricType )
    {
        return new RequestBuilder(
                String.format( "subutai monitor -q %s %s | grep e+", metricType.getName(), host.getHostname() ) );
    }


    //subutai metrics management -s "2015-11-30 03:00:00" -e "2015-11-30 20:00:00"
    public RequestBuilder getHistoricalMetricCommand( Host host, Date start, Date end )
    {
        DateFormat df = new SimpleDateFormat( "YYYY-MM-dd HH:mm:ss" );
        String startTimestamp = df.format( start );
        String endTimestamp = df.format( end );
        return new RequestBuilder(
                String.format( "subutai metrics %s -s \"%s\" -e \"%s\"", host.getHostname(), startTimestamp,
                        endTimestamp ) );
    }


    public RequestBuilder getActivateMonitoringCommand( String hostname, MonitoringSettings monitoringSettings )
    {
        return new RequestBuilder( String.format(
                "subutai monitor -c all -p \" metricCollectionIntervalInMin:%s, maxSampleCount:%s, "
                        + "metricCountToAverageToAlert:%s, intervalBetweenAlertsInMin:%s, ramAlertThreshold:%s, "
                        + "cpuAlertThreshold:%s, diskAlertThreshold:%s \" %s",
                monitoringSettings.getMetricCollectionIntervalInMin(), monitoringSettings.getMaxSampleCount(),
                monitoringSettings.getMetricCountToAverageToAlert(), monitoringSettings.getIntervalBetweenAlertsInMin(),
                monitoringSettings.getRamAlertThreshold(), monitoringSettings.getCpuAlertThreshold(),
                monitoringSettings.getDiskAlertThreshold(), hostname ) );
    }


    public RequestBuilder getProcessResourceUsageCommand( String hostname, int pid )
    {
        return new RequestBuilder( String.format( "subutai monitor -i %s %s", pid, hostname ) );
    }
}
