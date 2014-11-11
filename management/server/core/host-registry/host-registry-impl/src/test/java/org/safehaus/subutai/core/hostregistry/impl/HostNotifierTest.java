package org.safehaus.subutai.core.hostregistry.impl;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.core.hostregistry.impl.HostNotifier;
import org.safehaus.subutai.core.hostregistry.api.HostInfo;
import org.safehaus.subutai.core.hostregistry.api.HostListener;

import static org.mockito.Mockito.verify;


@RunWith( MockitoJUnitRunner.class )
public class HostNotifierTest
{

    @Mock
    HostListener listener;
    @Mock
    HostInfo hostInfo;

    HostNotifier notifier;


    @Before
    public void setUp() throws Exception
    {
        notifier = new HostNotifier( listener, hostInfo );
    }


    @Test
    public void testRun() throws Exception
    {
        notifier.run();

        verify( listener ).onHeartbeat( hostInfo );
    }
}
