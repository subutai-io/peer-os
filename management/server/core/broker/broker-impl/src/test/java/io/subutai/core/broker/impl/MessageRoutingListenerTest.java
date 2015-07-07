package io.subutai.core.broker.impl;


import java.io.PrintStream;
import java.util.Set;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Sets;

import io.subutai.core.broker.api.ByteMessageListener;
import io.subutai.core.broker.api.MessageListener;
import io.subutai.core.broker.api.TextMessageListener;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class MessageRoutingListenerTest
{
    @Mock
    Set<MessageListener> listeners;

    @Mock
    ByteMessageListener byteMessageListener;
    @Mock
    TextMessageListener textMessageListener;

    @Mock
    BytesMessage bytesMessage;
    @Mock
    TextMessage textMessage;


    MessageRoutingListener router;


    @Before
    public void setUp() throws Exception
    {
        router = new MessageRoutingListener();
        router.listeners = listeners;
    }


    @Test
    public void testAddListener() throws Exception
    {
        router.addListener( byteMessageListener );

        verify( listeners ).add( byteMessageListener );
    }


    @Test
    public void testRemoveListener() throws Exception
    {
        router.removeListener( byteMessageListener );

        verify( listeners ).remove( byteMessageListener );
    }


    @Test
    public void testNotifyListener() throws Exception
    {
        when( bytesMessage.getBodyLength() ).thenReturn( 1L );

        router.notifyListener( byteMessageListener, bytesMessage );

        verify( byteMessageListener ).onMessage( isA( byte[].class ) );

        router.notifyListener( textMessageListener, textMessage );

        verify( textMessageListener ).onMessage( anyString() );

        reset( byteMessageListener );

        router.notifyListener( byteMessageListener, textMessage );

        verifyZeroInteractions( byteMessageListener );

        JMSException exception = mock( JMSException.class );
        doThrow( exception ).when( bytesMessage ).getBodyLength();

        router.notifyListener( byteMessageListener, bytesMessage );

        verify( exception ).printStackTrace( any( PrintStream.class ) );
    }


    @Test
    public void testOnMessage() throws Exception
    {

        Topic topic = mock( Topic.class );
        when( bytesMessage.getJMSDestination() ).thenReturn( topic );
        when( byteMessageListener.getTopic() ).thenReturn( io.subutai.core.broker.api.Topic.HEARTBEAT_TOPIC );
        when( topic.getTopicName() ).thenReturn( io.subutai.core.broker.api.Topic.HEARTBEAT_TOPIC.name() );
        Set<MessageListener> listenerSet = Sets.newHashSet();
        listenerSet.add( byteMessageListener );
        router.listeners = listenerSet;

        router.onMessage( bytesMessage );

        verify( byteMessageListener ).onMessage( isA( byte[].class ) );

        JMSException exception = mock( JMSException.class );
        doThrow( exception ).when( bytesMessage ).getJMSDestination();

        router.onMessage( bytesMessage );

        verify( exception ).printStackTrace( any( PrintStream.class ) );
    }
}
