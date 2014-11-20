package org.safehaus.subutai.core.command.ui.old;


import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.safehaus.subutai.common.enums.RequestType;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.Response;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.core.command.api.command.CommandCallback;
import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.server.ui.component.AgentTree;

import com.google.common.collect.Sets;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Test for SendButtonListener
 */
public class SendButtonListenerTest
{

    private static final String DUMMY_OUTPUT = "dummy output";
    private static final String SELECT_NODES_MSG = "Please, select nodes";
    private static final String ENTER_COMMAND_MSG = "Please, enter command";
    private TerminalForm terminalForm;
    private AgentTree agentTree;


    @Before
    public void setUp()
    {
        terminalForm = mock( TerminalForm.class );
        SendButtonListener sendButtonListener =
                new SendButtonListener( terminalForm, mock( AgentManager.class ), mock( CommandRunner.class ),
                        mock( ExecutorService.class ) );

        agentTree = mock( AgentTree.class );
        when( terminalForm.getAgentTree() ).thenReturn( agentTree );
    }


    @Test( expected = NullPointerException.class )
    public void constructorShouldFailOnNullExecutor()
    {

        new SendButtonListener( mock( TerminalForm.class ), mock( AgentManager.class ), mock( CommandRunner.class ),
                null );
    }


    @Test
    public void shouldCheckSelectedAgents()
    {

        SendButtonListener sendButtonListener =
                new SendButtonListener( terminalForm, mock( AgentManager.class ), mock( CommandRunner.class ),
                        mock( ExecutorService.class ) );
        when( agentTree.getSelectedAgents() ).thenReturn( Collections.<Agent>emptySet() );


        sendButtonListener.buttonClick( null );


        verify( terminalForm ).show( SELECT_NODES_MSG );
    }


    @Test
    public void shouldCheckEnteredCommand()
    {

        SendButtonListener sendButtonListener =
                new SendButtonListener( terminalForm, mock( AgentManager.class ), mock( CommandRunner.class ),
                        mock( ExecutorService.class ) );
        when( agentTree.getSelectedAgents() ).thenReturn( Sets.newHashSet( mock( Agent.class ) ) );
        TextField program = mock( TextField.class );
        when( terminalForm.getProgramTxtFld() ).thenReturn( program );


        sendButtonListener.buttonClick( null );


        verify( terminalForm ).show( ENTER_COMMAND_MSG );
    }


    @Test
    public void shouldSubmitCommand()
    {

        ExecutorService executorService = mock( ExecutorService.class );
        SendButtonListener sendButtonListener =
                new SendButtonListener( terminalForm, mock( AgentManager.class ), mock( CommandRunner.class ),
                        executorService );
        when( agentTree.getSelectedAgents() ).thenReturn( Sets.newHashSet( mock( Agent.class ) ) );
        TextField program = mock( TextField.class );
        when( terminalForm.getProgramTxtFld() ).thenReturn( program );
        when( program.getValue() ).thenReturn( "pwd" );
        ComboBox requestTypeCombo = mock( ComboBox.class );
        when( terminalForm.getRequestTypeCombo() ).thenReturn( requestTypeCombo );
        when( requestTypeCombo.getValue() ).thenReturn( RequestType.EXECUTE_REQUEST );
        TextField timeout = mock( TextField.class );
        when( timeout.getValue() ).thenReturn( "30" );
        when( terminalForm.getTimeoutTxtFld() ).thenReturn( timeout );
        when( terminalForm.getIndicator() ).thenReturn( mock( Label.class ) );
        TextField workDir = mock( TextField.class );
        when( terminalForm.getWorkDirTxtFld() ).thenReturn( workDir );
        when( terminalForm.getTaskCount() ).thenReturn( mock( AtomicInteger.class ) );

        sendButtonListener.buttonClick( null );


        verify( executorService ).execute( any( SendButtonListener.ExecuteCommandTask.class ) );
    }


    @Test
    public void shouldExecuteCommand() throws CommandException
    {
        Command command = mock( Command.class );
        when( terminalForm.getTaskCount() ).thenReturn( mock( AtomicInteger.class ) );
        when( terminalForm.getIndicator() ).thenReturn( mock( Label.class ) );

        SendButtonListener.ExecuteCommandTask executeCommandTask =
                new SendButtonListener.ExecuteCommandTask( command, mock( AgentManager.class ), terminalForm );


        executeCommandTask.run();

        verify( command ).execute( any( CommandCallback.class ) );
    }


    @Test
    public void shouldAddOutput() throws CommandException
    {
        Command command = mock( Command.class );
        when( terminalForm.getTaskCount() ).thenReturn( mock( AtomicInteger.class ) );
        when( terminalForm.getIndicator() ).thenReturn( mock( Label.class ) );

        SendButtonListener.ExecuteCommandTask executeCommandTask =
                new SendButtonListener.ExecuteCommandTask( command, mock( AgentManager.class ), terminalForm );

        Response response = mock( Response.class );
        when( response.getStdOut() ).thenReturn( DUMMY_OUTPUT );

        executeCommandTask.displayResponse( response );

        ArgumentCaptor<String> outputCaptor = ArgumentCaptor.forClass( String.class );
        verify( terminalForm ).addOutput( outputCaptor.capture() );

        assertThat( outputCaptor.getValue(), containsString( DUMMY_OUTPUT ) );
    }
}
