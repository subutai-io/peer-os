package org.safehaus.subutai.core.filetracker.impl;


import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.common.enums.ResponseType;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.Response;
import org.safehaus.subutai.common.protocol.ResponseListener;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.common.protocol.RequestBuilder;
import org.safehaus.subutai.core.communication.api.CommunicationManager;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Created by bahadyr on 9/25/14.
 */
public class FileTrackerImplTest
{

    private final Set<ResponseListener> listeners = new HashSet<>();
    private CommandRunner commandRunner;
    private CommunicationManager communicationManager;

    private FileTrackerImpl fileTracker;


    @Before
    public void setupClasses()
    {
        commandRunner = mock( CommandRunner.class );
        communicationManager = mock( CommunicationManager.class );

        fileTracker = new FileTrackerImpl();
        fileTracker.setCommandRunner( commandRunner );
        fileTracker.setCommunicationManager( communicationManager );
    }


    @Test( expected = NullPointerException.class )
    public void shouldThrowNullPointerExceptionOnSetCommandRunner()
    {
        fileTracker.setCommandRunner( null );
    }


    @Test( expected = NullPointerException.class )
    public void shouldThrowNullPointerExceptionOnSetCommunicationManager()
    {
        fileTracker.setCommunicationManager( null );
    }


    @Test
    public void shouldAccessCommunicationManagerAndCallAddListenerOnInit()
    {
        fileTracker.init();
        verify( communicationManager ).addListener( fileTracker );
    }


    @Test
    public void shouldAccessCommunicationManagerAndCallRemoveListenerOnDestroy()
    {
        fileTracker.destroy();
        verify( communicationManager ).removeListener( fileTracker );
    }


    @Test
    public void shouldAddRemoveListenersFromListenersSet()
    {
        ResponseListener listener = mock( ResponseListener.class );
        fileTracker.addListener( listener );
        fileTracker.removeListener( listener );
    }


    @Test
    public void shouldAccessCommandRunnerOnCreateConfigPoints()
    {
        Command command = mock( Command.class );
        when( commandRunner.createCommand( any( RequestBuilder.class ), anySetOf( Agent.class ) ) )
                .thenReturn( command );
        fileTracker.createConfigPoints( mock( Agent.class ), new String[] { "configPoints" } );
        verify( commandRunner ).createCommand( any( RequestBuilder.class ), anySetOf( Agent.class ) );
    }


    @Test
    public void shouldAccessCommandRunnerOnRemoveConfigPoints()
    {
        Command command = mock( Command.class );
        when( commandRunner.createCommand( any( RequestBuilder.class ), anySetOf( Agent.class ) ) )
                .thenReturn( command );
        fileTracker.removeConfigPoints( mock( Agent.class ), new String[] { "configPoints" } );
        verify( commandRunner ).createCommand( any( RequestBuilder.class ), anySetOf( Agent.class ) );
    }


    @Test
    public void shouldAccessCommandRunnerCreateCommandOnListConfigPoints()
    {
        Command command = mock( Command.class );
        when( commandRunner.createCommand( any( RequestBuilder.class ), anySetOf( Agent.class ) ) )
                .thenReturn( command );
        fileTracker.listConfigPoints( mock( Agent.class ) );
        verify( commandRunner ).createCommand( any( RequestBuilder.class ), anySetOf( Agent.class ) );
    }


    @Test
    public void shouldCallInterfaceMethodOnResponse()
    {
        ResponseListener listener = mock( ResponseListener.class );
        Response response = mock( Response.class );
        when( response.getType() ).thenReturn( ResponseType.INOTIFY_ACTION_RESPONSE );
        fileTracker.addListener( listener );
        fileTracker.onResponse( response );
        verify( listener ).onResponse( response );
    }
}
