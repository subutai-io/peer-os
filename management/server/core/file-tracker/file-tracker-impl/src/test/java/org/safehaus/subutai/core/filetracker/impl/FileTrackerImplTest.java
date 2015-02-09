package org.safehaus.subutai.core.filetracker.impl;


import java.io.PrintStream;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.command.CommandCallback;
import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandUtil;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.peer.Host;
import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.common.util.UUIDUtil;
import org.safehaus.subutai.core.broker.api.Broker;
import org.safehaus.subutai.core.broker.api.BrokerException;
import org.safehaus.subutai.core.broker.api.Topic;
import org.safehaus.subutai.core.filetracker.api.ConfigPointListener;
import org.safehaus.subutai.core.filetracker.api.FileTrackerException;
import org.safehaus.subutai.core.filetracker.api.InotifyEventType;
import org.safehaus.subutai.core.peer.api.LocalPeer;
import org.safehaus.subutai.core.peer.api.PeerManager;

import com.google.common.collect.Sets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class FileTrackerImplTest
{
    private static final UUID ID = UUIDUtil.generateRandomUUID();
    private static final String CONFIG_POINT = "/etc/approx";
    private static final InotifyEventType EVENT_TYPE = InotifyEventType.CREATE_FOLDER;
    private static final String INOTIFY_RESPONSE = String.format(
            "{ \"response\": {" + "  \"type\": \"INOTIFY_EVENT\"," + "  \"id\": \"%s\","
                    + "  \"configPoint\":\"%s\", \"dateTime\":\"18.11.2014 11:42:39\", \"eventType\":\"%s\"  } }", ID,
            CONFIG_POINT, EVENT_TYPE );
    @Mock
    Broker broker;
    @Mock
    PeerManager peerManager;
    @Mock
    JsonUtil jsonUtil;
    @Mock
    CommandUtil commandUtil;
    @Mock
    ExecutorService notifier;
    @Mock
    ConfigPointListener listener;
    Set<ConfigPointListener> listeners = Sets.newHashSet( listener );
    @Mock
    Host host;
    Set<String> configPoints = Sets.newHashSet( CONFIG_POINT );
    @Mock
    LocalPeer localPeer;


    FileTrackerImpl fileTracker;


    @Before
    public void setUp() throws Exception
    {
        fileTracker = new FileTrackerImpl( broker, peerManager );
        fileTracker.notifier = notifier;
        fileTracker.commandUtil = commandUtil;
        fileTracker.jsonUtil = jsonUtil;
        fileTracker.listeners = listeners;
        when( peerManager.getLocalPeer() ).thenReturn( localPeer );
        when( localPeer.bindHost( ID ) ).thenReturn( host );
    }


    private void throwCommandException() throws CommandException
    {
        doThrow( new CommandException( "" ) ).when( commandUtil )
                                             .execute( any( RequestBuilder.class ), any( Host.class ) );
    }


    @Test
    public void testConstructor() throws Exception
    {
        try
        {
            new FileTrackerImpl( null, peerManager );
            fail( "Expected NullPointerException" );
        }
        catch ( NullPointerException e )
        {
        }
        try
        {
            new FileTrackerImpl( broker, null );
            fail( "Expected NullPointerException" );
        }
        catch ( NullPointerException e )
        {
        }
    }


    @Test
    public void testInit() throws Exception
    {
        fileTracker.init();

        verify( broker ).addByteMessageListener( fileTracker );


        doThrow( new BrokerException( "" ) ).when( broker ).addByteMessageListener( fileTracker );

        try
        {
            fileTracker.init();
            fail( "Expected FileTrackerException" );
        }
        catch ( FileTrackerException e )
        {
        }
    }


    @Test
    public void testDestroy() throws Exception
    {
        fileTracker.destroy();

        verify( broker ).removeMessageListener( fileTracker );
        verify( notifier ).shutdown();
    }


    @Test
    public void testAddRemoveListener() throws Exception
    {

        fileTracker.addListener( listener );

        assertTrue( listeners.contains( listener ) );

        fileTracker.removeListener( listener );

        assertFalse( listeners.contains( listener ) );
    }


    @Test( expected = FileTrackerException.class )
    public void testCreateConfigPoints() throws Exception
    {
        fileTracker.createConfigPoints( host, configPoints );

        verify( commandUtil ).execute( isA( RequestBuilder.class ), isA( Host.class ) );

        throwCommandException();

        fileTracker.createConfigPoints( host, configPoints );
    }


    @Test( expected = FileTrackerException.class )
    public void testRemoveConfigPoints() throws Exception
    {
        fileTracker.removeConfigPoints( host, configPoints );

        verify( commandUtil ).execute( isA( RequestBuilder.class ), isA( Host.class ) );

        throwCommandException();

        fileTracker.removeConfigPoints( host, configPoints );
    }


    @Test( expected = FileTrackerException.class )
    public void testListConfigPoints() throws Exception
    {
        fileTracker.listConfigPoints( host );

        verify( host ).execute( isA( RequestBuilder.class ), isA( CommandCallback.class ) );


        doThrow( new CommandException( "" ) ).when( host )
                                             .execute( isA( RequestBuilder.class ), isA( CommandCallback.class ) );

        fileTracker.listConfigPoints( host );
    }


    @Test
    public void testGetTopic() throws Exception
    {

        assertEquals( Topic.INOTIFY_TOPIC, fileTracker.getTopic() );
    }


    @Test
    public void testOnMessage() throws Exception
    {
        when( jsonUtil.from( anyString(), any( Class.class ) ) ).thenCallRealMethod();

        fileTracker.onMessage( INOTIFY_RESPONSE.getBytes() );

        verify( peerManager ).getLocalPeer();
        verify( notifier ).execute( isA( Runnable.class ) );


        RuntimeException exception = mock( RuntimeException.class );
        doThrow( exception ).when( jsonUtil ).from( anyString(), any( Class.class ) );

        fileTracker.onMessage( INOTIFY_RESPONSE.getBytes() );

        verify( exception ).printStackTrace( any( PrintStream.class ) );
    }
}