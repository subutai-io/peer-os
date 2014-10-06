package org.safehaus.subutai.core.command.ui.old;


import org.junit.Test;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.command.api.CommandRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;


/**
 * Test for CommandRunnerUI
 */
public class CommandRunnerUITest
{
    private CommandRunnerUI commandRunnerUI =
            new CommandRunnerUI( mock( CommandRunner.class ), mock( AgentManager.class ) );


    @Test(expected = NullPointerException.class)
    public void constructorShouldFailOnNullCommandRunner()
    {

        new CommandRunnerUI( null, mock( AgentManager.class ) );
    }


    @Test(expected = NullPointerException.class)
    public void constructorShouldFailOnNullAgentManager()
    {

        new CommandRunnerUI( mock( CommandRunner.class ), null );
    }


    @Test
    public void shouldBeCore()
    {

        assertTrue( commandRunnerUI.isCorePlugin() );
    }


    @Test
    public void shouldCreateComponent()
    {

        assertNotNull( commandRunnerUI.createComponent() );
    }


    @Test
    public void shouldReturnNameNImage()
    {


        assertEquals( CommandRunnerUI.MODULE_IMAGE, commandRunnerUI.getImage().getName() );
        assertEquals( CommandRunnerUI.MODULE_NAME, commandRunnerUI.getName() );
        assertEquals( CommandRunnerUI.MODULE_NAME, commandRunnerUI.getId() );
    }
}
