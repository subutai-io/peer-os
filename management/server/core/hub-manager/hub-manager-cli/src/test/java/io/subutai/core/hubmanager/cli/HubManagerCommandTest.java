package io.subutai.core.hubmanager.cli;


import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.common.test.SystemOutRedirectTest;
import io.subutai.core.hubmanager.api.HubManager;


/**
 * Test for MetricListCommand
 */
@RunWith( MockitoJUnitRunner.class )
public class HubManagerCommandTest extends SystemOutRedirectTest
{
    @Mock
    HubManager hubManager;

    private SendPeerMetricsCommand sendPeerMetricsCommand;


    @Before
    public void setUp() throws Exception
    {
    }
}
