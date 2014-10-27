package org.safehaus.subutai.core.message.impl;


import java.io.Serializable;

import org.junit.Test;

import static org.junit.Assert.assertEquals;


/**
 * Test for MessageImpl
 */
public class MessageImplTest
{


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

        MessageImpl message = new MessageImpl( customObject );

        assertEquals( customObject, message.getPayload() );
    }
}
