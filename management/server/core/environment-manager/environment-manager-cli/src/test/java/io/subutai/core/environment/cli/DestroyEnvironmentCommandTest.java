package io.subutai.core.environment.cli;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import io.subutai.core.environment.api.EnvironmentManager;

import static org.mockito.Mockito.verify;


@RunWith( MockitoJUnitRunner.class )
public class DestroyEnvironmentCommandTest
{
    @Mock
    EnvironmentManager environmentManager;

    DestroyEnvironmentCommand command;


    @Before
    public void setUp() throws Exception
    {
        command = new DestroyEnvironmentCommand( environmentManager );
        command.environmentId = TestUtil.ENV_ID.toString();
        command.async = TestUtil.ASYNC;
        command.forceMetadataRemoval = TestUtil.FORCE;
    }


    @Test
    public void testDoExecute() throws Exception
    {
        command.doExecute();

        verify( environmentManager ).destroyEnvironment( TestUtil.ENV_ID, TestUtil.ASYNC, TestUtil.FORCE );
    }
}
