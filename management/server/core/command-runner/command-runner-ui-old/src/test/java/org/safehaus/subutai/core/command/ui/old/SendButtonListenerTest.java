package org.safehaus.subutai.core.command.ui.old;


import org.junit.Test;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.dispatcher.api.CommandDispatcher;

import static org.mockito.Mockito.mock;


/**
 * Test for SendButtonListener
 */
public class SendButtonListenerTest
{


    @Test( expected = NullPointerException.class )
    public void sendButtonListenerConstructorShouldFailOnForm()
    {

        new SendButtonListener( mock( TerminalForm.class ), mock( AgentManager.class ), mock( CommandDispatcher.class ),
                null );
    }



}
