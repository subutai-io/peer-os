package io.subutai.core.metric.impl;


import org.junit.Test;
import io.subutai.common.command.RequestBuilder;
import io.subutai.core.metric.api.MonitoringSettings;

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
    public void testGetActivateMonitoringCommand() throws Exception
    {
        MonitoringSettings settings = mock( MonitoringSettings.class );
        RequestBuilder requestBuilder = commands.getActivateMonitoringCommand( HOSTNAME, settings );

        verify( settings ).getCpuAlertThreshold();
        assertNotNull( requestBuilder );
    }

}
