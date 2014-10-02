package org.safehaus.subutai.core.communication.impl;


import javax.jms.BytesMessage;
import javax.jms.JMSException;

import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.mock;


/**
 * Test for CommunicationMessageListener
 */
public class CommunicationMessageListenerTest
{
    private static final String ERR_MSG = "oops";
    CommunicationMessageListener communicationMessageListener = new CommunicationMessageListener();


    @Test
    public void shouldLogException() throws JMSException
    {

        BytesMessage message = mock( BytesMessage.class );
        Mockito.doThrow( new JMSException( ERR_MSG ) ).when( message ).getBodyLength();
        communicationMessageListener.onMessage( message );
    }
}
