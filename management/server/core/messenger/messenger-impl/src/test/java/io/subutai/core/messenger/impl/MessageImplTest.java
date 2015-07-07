package io.subutai.core.messenger.impl;


import java.io.Serializable;
import java.util.UUID;

import org.junit.Test;

import org.apache.commons.lang3.StringUtils;

import io.subutai.core.messenger.impl.MessageImpl;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doThrow;


/**
 * Test for MessageImpl
 */
public class MessageImplTest
{

    private static final UUID SOURCE_PEER_ID = UUID.randomUUID();
    private static final Object PAYLOAD = new Object();
    private static final String SENDER = "sender";
    MessageImpl message = new MessageImpl( SOURCE_PEER_ID, PAYLOAD );


    static class CustomObject implements Serializable
    {
        private int numValue;
        private String strValue;


        CustomObject( final int numValue, final String strValue )
        {
            this.numValue = numValue;
            this.strValue = strValue;
        }


        public int getNumValue()
        {
            return numValue;
        }


        public String getStrValue()
        {
            return strValue;
        }


        @Override
        public boolean equals( final Object o )
        {
            if ( this == o )
            {
                return true;
            }
            if ( !( o instanceof CustomObject ) )
            {
                return false;
            }

            final CustomObject that = ( CustomObject ) o;

            if ( numValue != that.numValue )
            {
                return false;
            }
            if ( !strValue.equals( that.strValue ) )
            {
                return false;
            }

            return true;
        }


        @Override
        public int hashCode()
        {
            int result = numValue;
            result = 31 * result + strValue.hashCode();
            return result;
        }
    }


    @Test
    public void testGetPayload() throws Exception
    {
        CustomObject customObject = new CustomObject( 123, "hello" );

        MessageImpl message = new MessageImpl( UUID.randomUUID(), customObject );

        assertEquals( customObject, message.getPayload( CustomObject.class ) );
    }


    @Test( expected = NullPointerException.class )
    public void constructorShouldFailOnNullSourcePeerId() throws Exception
    {
        new MessageImpl( null, PAYLOAD );
    }


    @Test( expected = NullPointerException.class )
    public void constructorShouldFailOnNullPayload() throws Exception
    {
        new MessageImpl( SOURCE_PEER_ID, null );
    }


    @Test
    public void testGetSourcePeerId() throws Exception
    {

        assertEquals( SOURCE_PEER_ID, message.getSourcePeerId() );
    }


    @Test
    public void testGetId() throws Exception
    {

        assertNotNull( message.getId() );
    }


    @Test
    public void testGetSetSender() throws Exception
    {

        message.setSender( SENDER );

        assertEquals( SENDER, message.getSender() );
    }


    @Test( expected = IllegalArgumentException.class )
    public void testLongSender() throws Exception
    {
        MessageImpl message = new MessageImpl( SOURCE_PEER_ID, PAYLOAD );

        message.setSender( StringUtils.repeat( "s", MessageImpl.MAX_SENDER_LEN + 1 ) );
    }


    @Test
    public void testToString() throws Exception
    {

        assertThat( message.toString(), containsString( SOURCE_PEER_ID.toString() ) );
        message.setSender( SENDER );
        assertThat( message.toString(), containsString( SENDER ) );
    }


}
