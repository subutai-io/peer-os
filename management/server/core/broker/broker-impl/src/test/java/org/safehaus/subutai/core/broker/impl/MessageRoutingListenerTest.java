package org.safehaus.subutai.core.broker.impl;


import java.io.PrintStream;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Topic;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.core.broker.api.MessageListener;

import com.google.common.collect.Sets;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class MessageRoutingListenerTest
{
    @Mock
    Set<MessageListener> listeners;

    @Mock
    Map<org.safehaus.subutai.core.broker.api.Topic, ExecutorService> notifiers;

    @Mock
    MessageListener listener;

    @Mock
    Message message;
    @Mock
    ExecutorService notifier;


    MessageRoutingListener router;


    @Before
    public void setUp() throws Exception
    {
        router = new MessageRoutingListener();
        router.listeners = listeners;
        router.notifiers = notifiers;
        when( notifiers.get( any( org.safehaus.subutai.core.broker.api.Topic.class ) ) ).thenReturn( notifier );
    }


    @Test
    public void testAddListener() throws Exception
    {
        router.addListener( listener );

        verify( listeners ).add( listener );
    }


    @Test
    public void testRemoveListener() throws Exception
    {
        router.removeListener( listener );

        verify( listeners ).remove( listener );
    }


    @Test
    public void testNotifyListener() throws Exception
    {

        router.notifyListener( org.safehaus.subutai.core.broker.api.Topic.RESPONSE_TOPIC, listener, message );

        ArgumentCaptor<MessageNotifier> captor = ArgumentCaptor.forClass( MessageNotifier.class );

        verify( notifier ).execute( captor.capture() );

        assertEquals( listener, captor.getValue().listener );
        assertEquals( message, captor.getValue().message );
    }


    @Test
    public void testOnMessage() throws Exception
    {

        Topic topic = mock( Topic.class );
        when( message.getJMSDestination() ).thenReturn( topic );
        when( listener.getTopic() ).thenReturn( org.safehaus.subutai.core.broker.api.Topic.HEARTBEAT_TOPIC );
        when( topic.getTopicName() ).thenReturn( org.safehaus.subutai.core.broker.api.Topic.HEARTBEAT_TOPIC.name() );
        router.listeners = Sets.newHashSet( listener );

        router.onMessage( message );

        verify( notifier ).execute( isA( MessageNotifier.class ) );

        JMSException exception = mock( JMSException.class );
        doThrow( exception ).when( message ).getJMSDestination();

        router.onMessage( message );

        verify( exception ).printStackTrace( any( PrintStream.class ) );
    }


    @Test
    public void testDispose() throws Exception
    {

        router.dispose();

        verify( notifier, times( org.safehaus.subutai.core.broker.api.Topic.values().length ) ).shutdown();
    }
}
