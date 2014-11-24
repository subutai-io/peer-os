package org.safehaus.subutai.core.metric.api;


/**
 * Monitoring settings
 */
public interface MonitoringSettings
{
    //interval between metric samples
    public int getMetricCollectionIntervalInMin();

    //number fo metrics to store
    public int getMaxSampleCount();

    //number of metrics to average in an alert
    public int getMetricCountToAverageToAlert();

    //interval between alerts
    public int getIntervalBetweenAlertsInMin();

    //RAM threshold upon reaching which an alert is generated (percentage)
    public double getRamAlertThreshold();

    //CPU threshold upon reaching which an alert is generated (percentage)
    public double getCpuAlertThreshold();

    //Disk threshold upon reaching which an alert is generated (percentage)
    public double getDiskThreshold();
}
