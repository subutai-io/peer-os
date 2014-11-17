package org.safehaus.subutai.core.filetracker.impl;


import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.enums.RequestType;
import org.safehaus.subutai.common.enums.ResponseType;
import org.safehaus.subutai.common.protocol.Response;
import org.safehaus.subutai.common.protocol.ResponseListener;
import org.safehaus.subutai.core.communication.api.CommunicationManager;
import org.safehaus.subutai.core.filetracker.api.FileTrackerException;
import org.safehaus.subutai.core.peer.api.Host;

import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.*;


/**
 * Created by bahadyr on 9/25/14.
 */
public class FileTrackerImplTest
{

    private final Set<ResponseListener> listeners = new HashSet<>();
    private CommunicationManager communicationManager;

    private FileTrackerImpl fileTracker;
    private Host host;


    @Before
    public void setupClasses()
    {
        communicationManager = mock( CommunicationManager.class );

        fileTracker = new FileTrackerImpl();
        fileTracker.setCommunicationManager( communicationManager );
        host = mock(Host.class);
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


//    @Test
    //    public void shouldAccessCommandRunnerOnCreateConfigPoints() throws FileTrackerException
    //    {
    //        Command command = mock( Command.class );
    //        when( commandRunner.createCommand( any( RequestBuilder.class ), anySetOf( Agent.class ) ) )
    //                .thenReturn( command );
    //        fileTracker.createConfigPoints( mock( ManagementHost.class ), new String[] { "configPoints" } );
    //        verify( commandRunner ).createCommand( any( RequestBuilder.class ), anySetOf( Agent.class ) );
    //    }
    //
    //
    //    @Test
    //    public void shouldAccessCommandRunnerOnRemoveConfigPoints() throws FileTrackerException
    //    {
    //        Command command = mock( Command.class );
    //        when( commandRunner.createCommand( any( RequestBuilder.class ), anySetOf( Agent.class ) ) )
    //                .thenReturn( command );
    //        fileTracker.removeConfigPoints( mock( ManagementHost.class ), new String[] { "configPoints" } );
    //        verify( commandRunner ).createCommand( any( RequestBuilder.class ), anySetOf( Agent.class ) );
    //    }
    //
    //
    //    @Test
    //    public void shouldAccessCommandRunnerCreateCommandOnListConfigPoints() throws FileTrackerException
    //    {
    //        Command command = mock( Command.class );
    //        when( commandRunner.createCommand( any( RequestBuilder.class ), anySetOf( Agent.class ) ) )
    //                .thenReturn( command );
    //        fileTracker.listConfigPoints( mock( ManagementHost.class ) );
    //        verify( commandRunner ).createCommand( any( RequestBuilder.class ), anySetOf( Agent.class ) );
    //    }


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

    @Test
    public void testRemoveConfigPoints() throws Exception {
        String[] configPoints = {"test"};
        fileTracker.removeConfigPoints(host,configPoints);

        verify(host).execute( new RequestBuilder( "pwd" ).withType( RequestType.INOTIFY_REMOVE_REQUEST )
                .withConfPoints(configPoints));
    }

    @Test
    public void testCreateConfigPoints() throws  Exception {
        String[] configPoints = {"test"};
        fileTracker.createConfigPoints(host, configPoints);

        verify(host).execute( new RequestBuilder( "pwd" ).withType( RequestType.INOTIFY_CREATE_REQUEST )
                .withConfPoints( configPoints ) );

    }

    @Test
    public void testListConfigPoints() throws  Exception {
        fileTracker.listConfigPoints(host);
        verify(host).execute( new RequestBuilder( "pwd" ).withType( RequestType.INOTIFY_LIST_REQUEST ) );
        host.execute(new RequestBuilder("pwd").withType(RequestType.INOTIFY_LIST_REQUEST));
    }

    @Test ( expected = FileTrackerException.class )
    public void shouldThrowFileTrackerExceptionInListConfigPoints() throws FileTrackerException, CommandException {
        when(host.execute(any(RequestBuilder.class))).thenThrow(FileTrackerException.class);
        fileTracker.listConfigPoints(host);
    }

    @Test ( expected = FileTrackerException.class )
    public void shouldThrowFileTrackerExceptionInCreateConfigPoints() throws FileTrackerException, CommandException {
        String[] configPoints = {"test"};
        when(host.execute(any(RequestBuilder.class))).thenThrow(FileTrackerException.class);
        fileTracker.createConfigPoints(host,configPoints);
    }

    @Test ( expected = FileTrackerException.class )
    public void shouldThrowFileTrackerExceptionRemoveConfigPoints() throws FileTrackerException, CommandException {
        String[] configPoints = {"test"};
        when(host.execute(any(RequestBuilder.class))).thenThrow(FileTrackerException.class);
        fileTracker.removeConfigPoints(host,configPoints);
    }
}
