package org.safehaus.subutai.core.metric.impl;


import org.junit.Test;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.core.metric.api.MonitoringSettings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


public class CommandsTest
{
    private static final String HOSTNAME = "host";
    private Commands commands = new Commands();


    @Test
    public void testGetCurrentMetricCommand() throws Exception
    {
        assertEquals( new RequestBuilder( String.format( "subutai monitor %s", HOSTNAME ) ),
                commands.getCurrentMetricCommand( HOSTNAME ) );
    }


    @Test
    public void testGetActivateMonitoringWithCustomSettingsCommand() throws Exception
    {
        MonitoringSettings settings = mock( MonitoringSettings.class );
        RequestBuilder requestBuilder = commands.getActivateMonitoringWithCustomSettingsCommand( HOSTNAME, settings );

        verify( settings ).getCpuAlertThreshold();
        assertNotNull( requestBuilder );
    }


    @Test
    public void testGetActivateMonitoringWithDefaultSettingsCommand() throws Exception
    {
        assertEquals( new RequestBuilder( String.format( "subutai monitor -c %s", HOSTNAME ) ),
                commands.getActivateMonitoringWithDefaultSettingsCommand( HOSTNAME ) );
    }
}
