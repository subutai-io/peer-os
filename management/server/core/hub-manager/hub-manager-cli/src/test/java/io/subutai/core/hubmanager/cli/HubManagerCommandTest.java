package io.subutai.core.hubmanager.cli;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.common.test.SystemOutRedirectTest;
import io.subutai.core.hubmanager.api.HubManager;


/**
 * Test for Hub Manager CLI
 */
@RunWith( MockitoJUnitRunner.class )
public class HubManagerCommandTest extends SystemOutRedirectTest
{
    @Mock
    HubManager hubManager;

    private SendContainerMetricsCommand command;


    @Before
    public void setUp() throws Exception
    {
        command = new SendContainerMetricsCommand( hubManager );
    }


    @Test
    public void testDoExecute() throws Exception
    {
        command.doExecute();
    }
}
