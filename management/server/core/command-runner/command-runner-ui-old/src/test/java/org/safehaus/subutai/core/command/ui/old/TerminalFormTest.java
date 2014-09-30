package org.safehaus.subutai.core.command.ui.old;


import java.util.concurrent.ExecutorService;

import org.junit.Test;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.dispatcher.api.CommandDispatcher;

import com.vaadin.ui.TextArea;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


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


    @Test
    public void shouldShutDownExecutor()
    {

        TerminalForm terminalForm = new TerminalForm( mock( CommandDispatcher.class ), mock( AgentManager.class ) );
        ExecutorService executorService = mock(ExecutorService.class);
        terminalForm.setExecutor( executorService );

        terminalForm.dispose();

        verify( executorService ).shutdown();
    }


    @Test
    public void shouldWriteToTextArea()
    {

        TerminalForm terminalForm = new TerminalForm( mock( CommandDispatcher.class ), mock( AgentManager.class ) );
        TextArea textArea = mock( TextArea.class );
        when( textArea.getValue() ).thenReturn( DUMMY_OUTPUT );

        terminalForm.setCommandOutputTxtArea( textArea );


        terminalForm.addOutput( DUMMY_OUTPUT );


        verify( textArea ).setValue( String.format( "%s%s", DUMMY_OUTPUT, DUMMY_OUTPUT ) );
    }


    @Test( expected = NullPointerException.class )
    public void sendButtonListenerConstructorShouldFailOnForm()
    {

        new SendButtonListener( mock( TerminalForm.class ), mock( AgentManager.class ), mock( CommandDispatcher.class ),
                null );
    }
}
