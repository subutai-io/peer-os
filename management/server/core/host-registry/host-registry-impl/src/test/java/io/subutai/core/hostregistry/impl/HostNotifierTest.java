package io.subutai.core.hostregistry.impl;


import java.io.PrintStream;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Sets;

import io.subutai.common.metric.ResourceAlert;
import io.subutai.core.hostregistry.api.HostListener;
import io.subutai.common.host.ResourceHostInfo;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


@RunWith( MockitoJUnitRunner.class )
public class HostNotifierTest
{

    @Mock
    HostListener listener;
    @Mock
    ResourceHostInfo resourceHostInfo;

    @Mock
    ResourceAlert resourceAlert;

    HostNotifier notifier;

    Set<ResourceAlert> alerts;


    @Before
    public void setUp() throws Exception
    {
        alerts = Sets.newHashSet( resourceAlert );
        notifier = new HostNotifier( listener, resourceHostInfo, alerts );
    }


    @Test
    public void testRun() throws Exception
    {
        notifier.run();

        verify( listener ).onHeartbeat( resourceHostInfo, alerts );

        RuntimeException exception = mock( RuntimeException.class );
        doThrow( exception ).when( listener ).onHeartbeat( resourceHostInfo, alerts );

        notifier.run();

        verify( exception ).printStackTrace( any( PrintStream.class ) );
    }
}
