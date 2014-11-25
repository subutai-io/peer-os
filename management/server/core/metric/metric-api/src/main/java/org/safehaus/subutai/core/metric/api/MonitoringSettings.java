package org.safehaus.subutai.core.metric.api;


import org.safehaus.subutai.common.util.NumUtil;

import com.google.common.base.Preconditions;


/**
 * Monitoring settings
 */
public class MonitoringSettings
{

    //interval between metric samples
    private int metricCollectionIntervalInMin = 1;
    //number fo metrics to store
    private int maxSampleCount = 1440;
    //number of metrics to average in an alert
    private int metricCountToAverageToAlert = 30;
    //interval between alerts
    private int intervalBetweenAlertsInMin = 30;
    //RAM threshold upon reaching which an alert is generated (percentage)
    private int ramAlertThreshold = 80;
    //CPU threshold upon reaching which an alert is generated (percentage)
    private int cpuAlertThreshold = 80;
    //Disk threshold upon reaching which an alert is generated (percentage)
    private int diskAlertThreshold = 80;


    public MonitoringSettings withMetricCollectionIntervalInMin( int metricCollectionIntervalInMin )
    {
        Preconditions.checkArgument( metricCollectionIntervalInMin > 0, "Interval must be greater than 0" );

        this.metricCollectionIntervalInMin = metricCollectionIntervalInMin;

        return this;
    }


    public int getMetricCollectionIntervalInMin()
    {
        return metricCollectionIntervalInMin;
    }


    public MonitoringSettings withMaxSampleCount( int maxSampleCount )
    {
        Preconditions.checkArgument( maxSampleCount > 0, "Count must be greater than 0" );

        this.maxSampleCount = maxSampleCount;

        return this;
    }


    public int getMaxSampleCount()
    {
        return maxSampleCount;
    }


    public MonitoringSettings withMetricCountToAverageToAlert( int metricCountToAverageToAlert )
    {
        Preconditions.checkArgument( metricCountToAverageToAlert > 0, "Count must be greater than 0" );

        this.metricCountToAverageToAlert = metricCountToAverageToAlert;

        return this;
    }


    public int getMetricCountToAverageToAlert()
    {
        return metricCountToAverageToAlert;
    }


    public MonitoringSettings withIntervalBetweenAlertsInMin( int intervalBetweenAlertsInMin )
    {
        Preconditions.checkArgument( intervalBetweenAlertsInMin > 0, "Interval must be greater than 0" );

        this.intervalBetweenAlertsInMin = intervalBetweenAlertsInMin;

        return this;
    }


    public int getIntervalBetweenAlertsInMin()
    {
        return intervalBetweenAlertsInMin;
    }


    public MonitoringSettings withRamAlertThreshold( int ramAlertThreshold )
    {
        Preconditions.checkArgument( NumUtil.isIntBetween( ramAlertThreshold, 1, 100 ),
                "Threshold must be between 1 and 100" );

        this.ramAlertThreshold = ramAlertThreshold;

        return this;
    }


    public int getRamAlertThreshold()
    {
        return ramAlertThreshold;
    }


    public MonitoringSettings withCpuAlertThreshold( int cpuAlertThreshold )
    {
        Preconditions.checkArgument( NumUtil.isIntBetween( cpuAlertThreshold, 1, 100 ),
                "Threshold must be between 1 and 100" );

        this.cpuAlertThreshold = cpuAlertThreshold;

        return this;
    }


    public int getCpuAlertThreshold()
    {
        return cpuAlertThreshold;
    }


    public MonitoringSettings withDiskAlertThreshold( int diskAlertThreshold )
    {
        Preconditions.checkArgument( NumUtil.isIntBetween( diskAlertThreshold, 1, 100 ),
                "Threshold must be between 1 and 100" );

        this.diskAlertThreshold = diskAlertThreshold;

        return this;
    }


    public int getDiskAlertThreshold()
    {
        return diskAlertThreshold;
    }
}
