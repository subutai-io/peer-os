package org.safehaus.subutai.core.executor.ui;


import java.io.PrintStream;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.command.CommandCallback;
import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.command.RequestType;
import org.safehaus.subutai.common.command.Response;
import org.safehaus.subutai.common.host.ContainerHostState;
import org.safehaus.subutai.common.host.HostInfo;
import org.safehaus.subutai.core.executor.api.CommandExecutor;
import org.safehaus.subutai.core.hostregistry.api.ContainerHostInfo;
import org.safehaus.subutai.core.hostregistry.api.HostDisconnectedException;
import org.safehaus.subutai.core.hostregistry.api.HostRegistry;
import org.safehaus.subutai.core.hostregistry.api.ResourceHostInfo;
import org.safehaus.subutai.server.ui.component.HostTree;

import com.google.common.collect.Sets;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class SendButtonListenerTest
{
    private static final UUID ID = UUID.randomUUID();
    private static final String STRING_VALUE = "VALUE";

    @Mock
    HostRegistry hostRegistry;
    @Mock
    CommandExecutor commandExecutor;
    @Mock
    TerminalForm terminalForm;
    @Mock
    HostTree hostTree;
    @Mock
    TextField textField;
    @Mock
    HostInfo hostInfo;
    @Mock
    ResourceHostInfo resourceHostInfo;
    @Mock
    ContainerHostInfo containerHostInfo;
    @Mock
    ComboBox comboBox;
    @Mock
    CheckBox checkBox;
    @Mock
    Label label;
    @Mock
    AtomicInteger atomicInteger;
    @Mock
    RequestBuilder requestBuilder;
    @Mock
    Response response;
    @Mock
    CommandResult commandResult;
    Set<HostInfo> resourceHosts = Sets.newHashSet();
    Set<HostInfo> containerHosts = Sets.newHashSet();


    SendButtonListener listener;


    @Before
    public void setUp() throws Exception
    {
        listener = new SendButtonListener( terminalForm, commandExecutor, hostRegistry );
        resourceHosts.add( resourceHostInfo );
        containerHosts.add( containerHostInfo );
        when( resourceHostInfo.getId() ).thenReturn( ID );
        when( containerHostInfo.getId() ).thenReturn( ID );
        when( hostRegistry.getResourceHostInfoById( ID ) ).thenReturn( resourceHostInfo );
        when( hostRegistry.getContainerHostInfoById( ID ) ).thenReturn( containerHostInfo );
        when( terminalForm.getRequestTypeCombo() ).thenReturn( comboBox );
        when( terminalForm.getTimeoutTxtFld() ).thenReturn( textField );
        when( terminalForm.getProgramTxtFld() ).thenReturn( textField );
        when( terminalForm.getWorkDirTxtFld() ).thenReturn( textField );
        when( terminalForm.getRunAsTxtFld() ).thenReturn( textField );
        when( terminalForm.getIndicator() ).thenReturn( label );
        when( terminalForm.getTaskCount() ).thenReturn( atomicInteger );
        when( terminalForm.getDaemonChk() ).thenReturn(checkBox);
        when( terminalForm.getHostTree() ).thenReturn( hostTree );
        when( hostTree.getSelectedHosts() ).thenReturn( Sets.<HostInfo>newHashSet() );
    }


    @Test
    public void testButtonClick() throws Exception
    {
        listener.buttonClick( null );

        verify( terminalForm ).addOutput( anyString() );


        when( hostTree.getSelectedHosts() ).thenReturn( Sets.newHashSet( hostInfo ) );
        when( terminalForm.getProgramTxtFld() ).thenReturn( textField );

        listener.buttonClick( null );

        verify( terminalForm, times( 2 ) ).addOutput( anyString() );


        when( textField.getValue() ).thenReturn( STRING_VALUE );
        when( hostTree.getSelectedHosts() ).thenReturn( resourceHosts );


        listener.buttonClick( null );

        verify( hostRegistry ).getResourceHostInfoById( ID );
    }


    @Test
    public void testAddIfHostConnected() throws Exception
    {
        Set<HostInfo> connectedHosts = Sets.newHashSet();

        listener.addIfHostConnected( connectedHosts, resourceHostInfo );

        assertTrue( connectedHosts.contains( resourceHostInfo ) );


        when( containerHostInfo.getStatus() ).thenReturn( ContainerHostState.RUNNING );

        listener.addIfHostConnected( connectedHosts, containerHostInfo );

        assertTrue( connectedHosts.contains( containerHostInfo ) );


        when( containerHostInfo.getStatus() ).thenReturn( ContainerHostState.STOPPED );

        listener.addIfHostConnected( connectedHosts, containerHostInfo );

        verify( terminalForm ).addOutput( anyString() );


        HostDisconnectedException exception = mock( HostDisconnectedException.class );
        doThrow( exception ).when( hostRegistry ).getContainerHostInfoById( ID );

        listener.addIfHostConnected( connectedHosts, containerHostInfo );

        verify( exception ).printStackTrace( any( PrintStream.class ) );
    }


    @Test
    public void testExecuteCommand() throws Exception
    {

        when( textField.getValue() ).thenReturn( "1" );
        when( comboBox.getValue() ).thenReturn( RequestType.TERMINATE_REQUEST );
        when( checkBox.getValue() ).thenReturn( true );
        when( terminalForm.getHostTree() ).thenReturn( hostTree );

        listener.executeCommand( resourceHosts );
        when( comboBox.getValue() ).thenReturn( RequestType.PS_REQUEST );
        listener.executeCommand( resourceHosts );

        verify( commandExecutor, times( 2 ) )
                .executeAsync( eq( ID ), isA( RequestBuilder.class ), isA( CommandCallback.class ) );
    }


    @Test
    public void testCheckRequest() throws Exception
    {
        when( comboBox.getValue() ).thenReturn( RequestType.TERMINATE_REQUEST );

        assertFalse( listener.checkRequest() );


        reset( comboBox );

        assertFalse( listener.checkRequest() );


        when( textField.getValue() ).thenReturn( "1" );

        assertTrue( listener.checkRequest() );
    }


    @Test
    public void testExecuteException() throws Exception
    {

        listener.execute( requestBuilder, resourceHosts );

        verify( commandExecutor ).executeAsync( eq( ID ), isA( RequestBuilder.class ), isA( CommandCallback.class ) );

        CommandException exception = mock( CommandException.class );

        doThrow( exception ).when( commandExecutor )
                            .executeAsync( eq( ID ), isA( RequestBuilder.class ), isA( CommandCallback.class ) );

        listener.execute( requestBuilder, resourceHosts );

        verify( exception ).printStackTrace( any( PrintStream.class ) );
    }


    @Test
    public void testGetHostInfo() throws Exception
    {
        when( hostRegistry.getHostInfoById( ID ) ).thenReturn( containerHostInfo );

        assertEquals( containerHostInfo, listener.getHostById( ID ) );


        HostDisconnectedException exception = mock( HostDisconnectedException.class );
        doThrow( exception ).when( hostRegistry ).getHostInfoById( ID );

        assertNull( listener.getHostById( ID ) );
    }


    @Test
    public void displayResponse() throws Exception
    {
        when( response.getStdOut() ).thenReturn( STRING_VALUE );
        when( response.getStdErr() ).thenReturn( STRING_VALUE );
        when( commandResult.hasCompleted() ).thenReturn( true );
        when( response.getExitCode() ).thenReturn( 1 );


        listener.displayResponse( hostInfo, response, commandResult );

        verify( terminalForm ).addOutput( anyString() );

        when( commandResult.hasCompleted() ).thenReturn( false );
        when( commandResult.hasTimedOut() ).thenReturn( true );

        listener.displayResponse( hostInfo, response, commandResult );

        verify( terminalForm, times( 2 ) ).addOutput( anyString() );
    }


    @Test
    public void testOnResponse() throws Exception
    {
        when( hostRegistry.getHostInfoById( ID ) ).thenReturn( hostInfo );
        when( commandResult.hasCompleted() ).thenReturn( true );
        when( terminalForm.getTaskCount() ).thenReturn( new AtomicInteger( 1 ) );
        when( terminalForm.isAttached() ).thenReturn( true );

        listener.onResponse( response, commandResult );

        verify( label ).setVisible( false );
    }
}
