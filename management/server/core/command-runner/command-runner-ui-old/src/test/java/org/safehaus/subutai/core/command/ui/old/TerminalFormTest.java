package org.safehaus.subutai.core.command.ui.old;


import org.junit.Test;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.dispatcher.api.CommandDispatcher;

import static org.mockito.Mockito.mock;


/**
 * Test for TerminalForm
 */
public class TerminalFormTest
{
    private static final String DUMMY_OUTPUT = "some dumy output";


    @Test( expected = NullPointerException.class )
    public void constructorShouldFailOnNullCommandRunner()
    {

        new TerminalForm( null, mock( AgentManager.class ) );
    }


    @Test( expected = NullPointerException.class )
    public void constructorShouldFailOnNullAgentManager()
    {

        new TerminalForm( mock( CommandDispatcher.class ), null );
    }


    @Test( expected = NullPointerException.class )
    public void sendButtonListenerConstructorShouldFailOnForm()
    {

        new SendButtonListener( mock( TerminalForm.class ), mock( AgentManager.class ), mock( CommandDispatcher.class ),
                null );
    }
}
