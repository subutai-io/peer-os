package org.safehaus.subutai.core.broker.impl;


import java.io.PrintStream;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.TextMessage;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.core.broker.api.ByteMessageListener;
import org.safehaus.subutai.core.broker.api.TextMessageListener;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;


@RunWith( MockitoJUnitRunner.class )
public class MessageNotifierTest
{

    @Mock
    TextMessage textMessage;

    @Mock
    BytesMessage bytesMessage;

    @Mock
    TextMessageListener textMessageListener;

    @Mock
    ByteMessageListener byteMessageListener;

    MessageNotifier notifier;


    @Test
    public void testTextMessage() throws Exception
    {

        MessageNotifier notifier = new MessageNotifier( textMessageListener, textMessage );

        notifier.run();

        verify( textMessageListener ).onMessage( anyString() );
        verify( textMessage ).getText();
    }


    @Test
    public void testByteMessage() throws Exception
    {
        MessageNotifier notifier = new MessageNotifier( byteMessageListener, bytesMessage );

        notifier.run();

        verify( byteMessageListener ).onMessage( any( byte[].class ) );
        verify( bytesMessage ).getBodyLength();
        verify( bytesMessage ).readBytes( any( byte[].class ) );
    }


    @Test
    public void testTypeMismatch() throws Exception
    {
        MessageNotifier notifier = new MessageNotifier( byteMessageListener, textMessage );

        notifier.run();

        verifyZeroInteractions( textMessage );
    }


    @Test
    public void testException() throws Exception
    {
        MessageNotifier notifier = new MessageNotifier( byteMessageListener, bytesMessage );
        JMSException exception = mock( JMSException.class );
        doThrow( exception ).when( bytesMessage ).getBodyLength();

        notifier.run();

        verify( exception ).printStackTrace( any( PrintStream.class ) );
    }
}
