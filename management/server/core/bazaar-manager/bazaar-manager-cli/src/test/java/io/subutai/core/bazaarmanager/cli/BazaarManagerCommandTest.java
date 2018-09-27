package io.subutai.core.bazaarmanager.cli;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.common.test.SystemOutRedirectTest;
import io.subutai.core.bazaarmanager.api.BazaarManager;


/**
 * Test forbazaar Manager CLI
 */
@RunWith( MockitoJUnitRunner.class )
public class BazaarManagerCommandTest extends SystemOutRedirectTest
{
    @Mock
    BazaarManager bazaarManager;

    private SendContainerMetricsCommand command;


    @Before
    public void setUp() throws Exception
    {
        command = new SendContainerMetricsCommand( bazaarManager );
    }


    @Test
    public void testDoExecute() throws Exception
    {
        command.doExecute();
    }
}
