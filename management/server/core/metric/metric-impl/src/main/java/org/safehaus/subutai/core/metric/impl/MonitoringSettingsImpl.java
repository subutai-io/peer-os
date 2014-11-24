package org.safehaus.subutai.core.metric.impl;


import org.safehaus.subutai.core.metric.api.MonitoringSettings;

import com.google.common.base.Preconditions;


public class MonitoringSettingsImpl implements MonitoringSettings
{

    private int metricCollectionIntervalInMin;
    private int maxSampleCount;
    private int metricCountToAverageToAlert;
    private int intervalBetweenAlertsInMin;
    private double ramAlertThreshold;
    private double cpuAlertThreshold;
    private double diskAlertThreshold;


    public MonitoringSettingsImpl( MonitoringSettings monitoringSettings )
    {
        Preconditions.checkNotNull( monitoringSettings );

        this.metricCollectionIntervalInMin = monitoringSettings.getMetricCollectionIntervalInMin();
        this.maxSampleCount = monitoringSettings.getMaxSampleCount();
        this.metricCountToAverageToAlert = monitoringSettings.getMetricCountToAverageToAlert();
        this.intervalBetweenAlertsInMin = monitoringSettings.getIntervalBetweenAlertsInMin();
        this.ramAlertThreshold = monitoringSettings.getRamAlertThreshold();
        this.cpuAlertThreshold = monitoringSettings.getCpuAlertThreshold();
        this.diskAlertThreshold = monitoringSettings.getDiskAlertThreshold();
    }


    @Override
    public int getMetricCollectionIntervalInMin()
    {
        return metricCollectionIntervalInMin;
    }


    @Override
    public int getMaxSampleCount()
    {
        return maxSampleCount;
    }


    @Override
    public int getMetricCountToAverageToAlert()
    {
        return metricCountToAverageToAlert;
    }


    @Override
    public int getIntervalBetweenAlertsInMin()
    {
        return intervalBetweenAlertsInMin;
    }


    @Override
    public double getRamAlertThreshold()
    {
        return ramAlertThreshold;
    }


    @Override
    public double getCpuAlertThreshold()
    {
        return cpuAlertThreshold;
    }


    public double getDiskAlertThreshold()
    {
        return diskAlertThreshold;
    }
}
