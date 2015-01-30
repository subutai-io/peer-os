package org.safehaus.subutai.core.metric.impl;


import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.peer.ContainerHost;
import org.safehaus.subutai.core.metric.api.MonitoringSettings;

import com.google.common.collect.Sets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class MonitoringActivationRequestTest
{
    private static final UUID ID = UUID.randomUUID();
    @Mock
    MonitoringSettings monitoringSettings;
    @Mock
    ContainerHost containerHost;

    MonitoringActivationRequest request;


    @Before
    public void setUp() throws Exception
    {
        when( containerHost.getId() ).thenReturn( ID );
        request = new MonitoringActivationRequest( Sets.newHashSet( containerHost ), monitoringSettings );
    }


    @Test
    public void testProperties() throws Exception
    {
        assertEquals( monitoringSettings, request.getMonitoringSettings() );
        assertTrue( request.getContainerHostsIds().contains( ID ) );
    }
}
