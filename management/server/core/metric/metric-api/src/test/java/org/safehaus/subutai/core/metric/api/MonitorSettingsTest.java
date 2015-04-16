package org.safehaus.subutai.core.metric.api;


import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;


public class MonitorSettingsTest
{
    private static final int VALUE = 1;
    private MonitoringSettings monitoringSettings;


    @Before
    public void setUp() throws Exception
    {
        monitoringSettings = new MonitoringSettings();
    }


    @Test
    public void testMetricCollectionIntervalInMin() throws Exception
    {
        monitoringSettings.withMetricCollectionIntervalInMin( VALUE );

        assertEquals( monitoringSettings.getMetricCollectionIntervalInMin(), VALUE );
    }


    @Test
    public void testMaxSampleCount() throws Exception
    {
        monitoringSettings.withMaxSampleCount( VALUE );

        assertEquals( monitoringSettings.getMaxSampleCount(), VALUE );
    }


    @Test
    public void testMetricCountToAverageToAlert() throws Exception
    {
        monitoringSettings.withMetricCountToAverageToAlert( VALUE );

        assertEquals( monitoringSettings.getMetricCountToAverageToAlert(), VALUE );
    }


    @Test
    public void testIntervalBetweenAlertsInMin() throws Exception
    {
        monitoringSettings.withIntervalBetweenAlertsInMin( VALUE );

        assertEquals( monitoringSettings.getIntervalBetweenAlertsInMin(), VALUE );
    }


    @Test
    public void testRamAlertThreshold() throws Exception
    {
        monitoringSettings.withRamAlertThreshold( VALUE );

        assertEquals( monitoringSettings.getRamAlertThreshold(), VALUE );
    }


    @Test
    public void testCpuAlertThreshold() throws Exception
    {
        monitoringSettings.withCpuAlertThreshold( VALUE );

        assertEquals( monitoringSettings.getCpuAlertThreshold(), VALUE );
    }


    @Test
    public void testDiskAlertThreshold() throws Exception
    {
        monitoringSettings.withDiskAlertThreshold( VALUE );

        assertEquals( monitoringSettings.getDiskAlertThreshold(), VALUE );
    }
}
