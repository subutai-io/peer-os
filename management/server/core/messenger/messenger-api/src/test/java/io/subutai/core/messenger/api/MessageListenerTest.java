package io.subutai.core.messenger.api;


import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import org.apache.commons.lang3.StringUtils;

import io.subutai.core.messenger.api.Message;
import io.subutai.core.messenger.api.MessageException;
import io.subutai.core.messenger.api.MessageListener;
import io.subutai.core.messenger.api.MessageStatus;
import io.subutai.core.messenger.api.MessengerException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;


public class MessageListenerTest
{
    private static final String RECIPIENT_NAME = "recipient";
    private static final String ERR_MSG = "OOPS";

    MessageListenerImpl messageListener = new MessageListenerImpl( RECIPIENT_NAME );


    static class MessageListenerImpl extends MessageListener
    {
        protected MessageListenerImpl( final String recipient )
        {
            super( recipient );
        }


        @Override
        public void onMessage( final Message message )
        {

        }
    }


    @Test( expected = IllegalArgumentException.class )
    public void testConstructor() throws Exception
    {
        new MessageListenerImpl( null );
    }


    @Test( expected = IllegalArgumentException.class )
    public void testConstructor2() throws Exception
    {
        new MessageListenerImpl( StringUtils.repeat( "s", MessageListener.MAX_RECIPIENT_ID_LEN + 1 ) );
    }


    @Test
    public void testGetRecipient() throws Exception
    {
        assertEquals( RECIPIENT_NAME, messageListener.getRecipient() );
    }


    @Test
    public void testEqualsHashCode() throws Exception
    {
        Map<MessageListener, MessageListener> map = new HashMap<>();
        MessageListener messageListener1 = new MessageListenerImpl( RECIPIENT_NAME );
        MessageListener messageListener2 = new MessageListenerImpl( "OOPS" );
        map.put( messageListener1, messageListener1 );

        assertEquals( messageListener, map.get( messageListener1 ) );
        assertEquals( messageListener1, messageListener1 );
        assertNotEquals( messageListener1, new Object() );
        assertNotEquals( messageListener1, messageListener2 );
    }


    @Test
    public void testMessageException() throws Exception
    {
        Exception nestedException = new Exception();

        MessageException messageException = new MessageException( nestedException );

        assertEquals( nestedException, messageException.getCause() );
    }


    @Test
    public void testMessengerException() throws Exception
    {
        Exception nestedException = new Exception();

        MessengerException messengerException = new MessengerException( nestedException );

        assertEquals( nestedException, messengerException.getCause() );
    }


    @Test
    public void testMessageException2() throws Exception
    {

        MessageException messageException = new MessageException( ERR_MSG );

        assertEquals( ERR_MSG, messageException.getMessage() );
    }


    @Test
    public void testMessageStatusEnum() throws Exception
    {
        assertTrue( MessageStatus.valueOf( "EXPIRED" ) == MessageStatus.EXPIRED );
    }
}
