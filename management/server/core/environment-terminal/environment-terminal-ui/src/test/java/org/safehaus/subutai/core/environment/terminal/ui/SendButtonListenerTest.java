package org.safehaus.subutai.core.environment.terminal.ui;


import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.command.Response;
import org.safehaus.subutai.common.host.HostInfo;
import org.safehaus.subutai.common.peer.ContainerHost;
import io.subutai.core.executor.api.CommandExecutor;

import com.google.common.collect.Sets;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class SendButtonListenerTest
{
    private static final UUID ID = UUID.randomUUID();
    private static final String STRING_VALUE = "VALUE";

    @Mock
    CommandExecutor commandExecutor;
    @Mock
    TerminalForm terminalForm;
    @Mock
    EnvironmentTree environmentTree;
    @Mock
    TextField textField;

    @Mock
    TextField timeoutTextField;

    @Mock
    TextField workDirTxtFld;

    @Mock
    ContainerHost containerHost;
    @Mock
    ComboBox comboBox;
    @Mock
    CheckBox checkBox;
    @Mock
    Label label;
    @Mock
    AtomicInteger taskCount;
    @Mock
    RequestBuilder requestBuilder;
    @Mock
    Response response;
    @Mock
    CommandResult commandResult;

    @Mock
    ExecutorService executorService;

    Set<HostInfo> resourceHosts = Sets.newHashSet();
    Set<ContainerHost> containerHosts = Sets.newHashSet();


    SendButtonListener listener;


    @Before
    public void setUp() throws Exception
    {
        listener = new SendButtonListener( terminalForm );

        terminalForm.environmentTree = environmentTree;
        terminalForm.programTxtFld = textField;
        terminalForm.timeoutTxtFld = timeoutTextField;
        terminalForm.workDirTxtFld = workDirTxtFld;
        terminalForm.daemonChk = checkBox;
        terminalForm.indicator = label;
        terminalForm.taskCount = taskCount;

        containerHosts.add( containerHost );
        when( containerHost.getId() ).thenReturn( ID );

        when( environmentTree.getSelectedContainers() ).thenReturn( Sets.<ContainerHost>newHashSet() );
    }


    @Test
    public void testButtonClick() throws Exception
    {
        listener.buttonClick( null );

        verify( terminalForm ).show( "Please, select container(s)" );


        when( environmentTree.getSelectedContainers() ).thenReturn( containerHosts );


        listener.buttonClick( null );

        verify( terminalForm ).show( "Please, enter command" );
    }


    //    @Test
    //    public void testAddIfHostConnected() throws Exception
    //    {
    //        Set<HostInfo> connectedHosts = Sets.newHashSet();
    //
    //        environmentListener.addIfHostConnected( connectedHosts, resourceHostInfo );
    //
    //        assertTrue( connectedHosts.contains( resourceHostInfo ) );
    //
    //
    //        when( containerHostInfo.getStatus() ).thenReturn( ContainerHostState.RUNNING );
    //
    //        environmentListener.addIfHostConnected( connectedHosts, containerHostInfo );
    //
    //        assertTrue( connectedHosts.contains( containerHostInfo ) );
    //
    //
    //        when( containerHostInfo.getStatus() ).thenReturn( ContainerHostState.STOPPED );
    //
    //        environmentListener.addIfHostConnected( connectedHosts, containerHostInfo );
    //
    //        verify( terminalForm ).addOutput( anyString() );
    //
    //
    //        HostDisconnectedException exception = mock( HostDisconnectedException.class );
    //        doThrow( exception ).when( hostRegistry ).getContainerHostInfoById( ID );
    //
    //        environmentListener.addIfHostConnected( connectedHosts, containerHostInfo );
    //
    //        verify( exception ).printStackTrace( any( PrintStream.class ) );
    //    }


    @Test
    public void testExecuteCommand() throws Exception
    {

        when( textField.getValue() ).thenReturn( "pwd" );
        when( timeoutTextField.getValue() ).thenReturn( "30" );
        when( workDirTxtFld.getValue() ).thenReturn( "/" );
        when( checkBox.getValue() ).thenReturn( true );

        listener.setExecutor( executorService );

        listener.executeCommand( containerHosts );

        verify( executorService ).execute( isA( SendButtonListener.ExecuteCommandTask.class ) );

        listener.executeCommand( Sets.newHashSet( containerHost, containerHost ) );

        verify( executorService, times( 2 ) ).execute( isA( SendButtonListener.ExecuteCommandTask.class ) );
    }


    @Test
    public void testCheckRequest() throws Exception
    {
        when( timeoutTextField.getValue() ).thenReturn( "0" );

        assertFalse( listener.checkRequest() );

        when( timeoutTextField.getValue() ).thenReturn( "30" );

        assertTrue( listener.checkRequest() );
    }


    @Test
    public void testExecuteException() throws Exception
    {

        //        environmentListener.execute( requestBuilder, resourceHosts );
        //
        //        verify( commandExecutor ).executeAsync( eq( ID ), isA( RequestBuilder.class ), isA( CommandCallback
        // .class ) );
        //
        //        CommandException exception = mock( CommandException.class );
        //
        //        doThrow( exception ).when( commandExecutor )
        //                            .executeAsync( eq( ID ), isA( RequestBuilder.class ), isA( CommandCallback
        // .class ) );
        //
        //        environmentListener.execute( requestBuilder, resourceHosts );
        //
        //        verify( exception ).printStackTrace( any( PrintStream.class ) );
    }


    @Test
    public void testGetHostInfo() throws Exception
    {
        //        when( hostRegistry.getHostInfoById( ID ) ).thenReturn( containerHostInfo );
        //
        //        assertEquals( containerHostInfo, environmentListener.getHostById( ID ) );
        //
        //
        //        HostDisconnectedException exception = mock( HostDisconnectedException.class );
        //        doThrow( exception ).when( hostRegistry ).getHostInfoById( ID );
        //
        //        assertNull( environmentListener.getHostById( ID ) );
    }


    @Test
    public void displayResponse() throws Exception
    {
        when( response.getStdOut() ).thenReturn( STRING_VALUE );
        when( response.getStdErr() ).thenReturn( STRING_VALUE );
        when( commandResult.hasCompleted() ).thenReturn( true );
        when( response.getExitCode() ).thenReturn( 1 );


        //        environmentListener.displayResponse( hostInfo, response, commandResult );
        //
        //        verify( terminalForm ).addOutput( anyString() );
        //
        //        when( commandResult.hasCompleted() ).thenReturn( false );
        //        when( commandResult.hasTimedOut() ).thenReturn( true );
        //
        //        environmentListener.displayResponse( hostInfo, response, commandResult );
        //
        //        verify( terminalForm, times( 2 ) ).addOutput( anyString() );
    }


}
