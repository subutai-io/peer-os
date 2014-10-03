package org.safehaus.subutai.core.environment.cli;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;


/**
 * Created by bahadyr on 9/25/14.
 */
@RunWith(MockitoJUnitRunner.class)
public class BuildEnvironmentCommandTest
{
    BuildEnvironmentCommand buildEnvironmentCommand;
    @Mock
    EnvironmentManager manager;


    @Before
    public void setUp() throws Exception
    {
        buildEnvironmentCommand = new BuildEnvironmentCommand();
        buildEnvironmentCommand.setEnvironmentManager( manager );
    }


    @Test
    public void test() throws Exception
    {

        buildEnvironmentCommand.doExecute();
    }
}
