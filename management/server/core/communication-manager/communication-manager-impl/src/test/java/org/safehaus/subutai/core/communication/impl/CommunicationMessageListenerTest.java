package org.safehaus.subutai.core.communication.impl;


import javax.jms.BytesMessage;
import javax.jms.JMSException;

import org.junit.Test;
import org.mockito.Mockito;

import org.apache.activemq.command.ActiveMQMessage;
import org.apache.activemq.command.RemoveInfo;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Test for CommunicationMessageListener
 */
public class CommunicationMessageListenerTest
{
    private static final String ERR_MSG = "oops";
    CommunicationMessageListener communicationMessageListener = new CommunicationMessageListener();


    @Test
    public void shouldProcessByteMessageAndException() throws JMSException
    {

        BytesMessage message = mock( BytesMessage.class );
        Mockito.doThrow( new JMSException( ERR_MSG ) ).when( message ).getBodyLength();
        communicationMessageListener.onMessage( message );
    }


    @Test
    public void shouldProcessAmqMessage()
    {

        ActiveMQMessage message = mock( ActiveMQMessage.class );
        when( message.getDataStructure() ).thenReturn( new RemoveInfo() );
        communicationMessageListener.onMessage( message );
    }
}
