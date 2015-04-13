package org.safehaus.subutai.core.env.cli;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.core.env.api.EnvironmentManager;

import static org.mockito.Mockito.verify;


@RunWith( MockitoJUnitRunner.class )
public class RemoveEnvironmentCommandTest
{
    @Mock
    EnvironmentManager environmentManager;

    RemoveEnvironmentCommand command;


    @Before
    public void setUp() throws Exception
    {
        command = new RemoveEnvironmentCommand( environmentManager );
        command.environmentId = TestUtil.ENV_ID.toString();
    }


    @Test
    public void testDoExecute() throws Exception
    {
        command.doExecute();

        verify( environmentManager ).removeEnvironment( TestUtil.ENV_ID );
    }
}
