package org.safehaus.subutai.core.environment.cli;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;

import static org.mockito.Mockito.when;


/**
 * Created by bahadyr on 9/25/14.
 */
@RunWith( MockitoJUnitRunner.class )
public class DestroyEnvironmentCommandTest
{
    private static final String NAME = "name";
    DestroyEnvironmentCommand command;
    @Mock
    EnvironmentManager manager;


    @Before
    public void setUp() throws Exception
    {
        command = new DestroyEnvironmentCommand();
        command.setEnvironmentManager( manager );
    }


    @Test
    public void test() throws Exception
    {
        command.setEnvironmentName( NAME );
        when( manager.destroyEnvironment( NAME ) ).thenReturn( true );
        command.doExecute();
    }
}
