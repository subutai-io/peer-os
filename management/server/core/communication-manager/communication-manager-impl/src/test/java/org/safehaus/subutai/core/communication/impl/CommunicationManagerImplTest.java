/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.core.communication.impl;


import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.common.protocol.Request;
import org.safehaus.subutai.common.protocol.Response;
import org.safehaus.subutai.common.protocol.ResponseListener;
import org.safehaus.subutai.core.communication.api.CommandJson;

import com.jayway.awaitility.Awaitility;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 * Test for CommunicationManager class
 */
public class CommunicationManagerImplTest
{

    private CommunicationManagerImpl communicationManagerImpl = null;
    private static final String SERVICE_TOPIC = "SERVICE_TOPIC";
    private static final String BROADCAST_TOPIC = "BROADCAST_TOPIC";
    private static final String VM_BROKER_URL = "vm://localhost?broker.persistent=false";
    private static final int amqMaxSenderPoolSize = 5;
    private static final int amqMaxMessageToAgentTtlSec = 30;
    private static final int aqMaxPooledConnections = 5;
    private static final boolean isPersistentMessages = false;


    @Before
    public void setUpClass() throws Exception
    {

        //init target object
        communicationManagerImpl = new CommunicationManagerImpl();
        communicationManagerImpl.setAmqMaxMessageToAgentTtlSec( amqMaxMessageToAgentTtlSec );
        communicationManagerImpl.setAmqUrl( VM_BROKER_URL );
        communicationManagerImpl.setPersistentMessages( isPersistentMessages );
        communicationManagerImpl.setAmqMaxSenderPoolSize( amqMaxSenderPoolSize );
        communicationManagerImpl.setAmqMaxPooledConnections( aqMaxPooledConnections );
        communicationManagerImpl.setAmqServiceTopic( SERVICE_TOPIC );
        communicationManagerImpl.setAmqBroadcastTopic( BROADCAST_TOPIC );
        communicationManagerImpl.init();
    }


    @Test
    public void testProperties()
    {
        assertEquals( communicationManagerImpl.getAmqMaxMessageToAgentTtlSec(), amqMaxMessageToAgentTtlSec );
        assertEquals( communicationManagerImpl.isPersistentMessages(), isPersistentMessages );
        assertEquals( communicationManagerImpl.getAmqBroadcastTopic(), BROADCAST_TOPIC );
    }


    @After
    public void tearDownClass() throws Exception
    {
        communicationManagerImpl.destroy();
    }


    @Test
    public void testAddListener()
    {
        ResponseListener listener = TestUtils.getResponseListener();

        communicationManagerImpl.addListener( listener );

        assertTrue( communicationManagerImpl.getListeners().contains( listener ) );
    }


    @Test
    public void testRemoveListener()
    {
        ResponseListener listener = TestUtils.getResponseListener();

        communicationManagerImpl.addListener( listener );

        communicationManagerImpl.removeListener( listener );

        assertFalse( communicationManagerImpl.getListeners().contains( listener ) );
    }


    @Test
    public void testSendRequest() throws JMSException
    {
        UUID uuid = UUID.randomUUID();
        Request request = TestUtils.getRequestTemplate( uuid );
        //setup listener

        MessageConsumer consumer = createConsumer( uuid.toString() );

        communicationManagerImpl.sendRequest( request );


        TextMessage txtMsg = ( TextMessage ) consumer.receive();
        String jsonCmd = txtMsg.getText();
        Request request2 = CommandJson.getRequestFromCommandJson( jsonCmd );

        assertEquals( request.getUuid(), request2.getUuid() );
    }


    @Test
    public void testMessageReception() throws JMSException
    {
        //setup listener
        final TestResponseListener responseListener = new TestResponseListener();
        communicationManagerImpl.addListener( responseListener );


        final Response response = new Response();
        Connection connection = communicationManagerImpl.createConnection();
        connection.start();
        final Session session = connection.createSession( false, Session.AUTO_ACKNOWLEDGE );
        Destination topic = session.createTopic( SERVICE_TOPIC );
        final MessageProducer producer = session.createProducer( topic );

        BytesMessage message = session.createBytesMessage();
        message.writeBytes( CommandJson.getResponseCommandJson( response ).getBytes() );
        producer.send( message );

        Awaitility.await().atMost( 2, TimeUnit.SECONDS ).with().pollInterval( 50, TimeUnit.MILLISECONDS ).and()
                  .pollDelay( 100, TimeUnit.MILLISECONDS ).until( new Callable<Boolean>()
        {

            public Boolean call() throws Exception
            {
                responseListener.signal.acquire();
                return true;
            }
        } );
    }


    @Test
    public void testBroadcastMessage() throws JMSException
    {
        UUID uuid = UUID.randomUUID();
        Request request = TestUtils.getRequestTemplate( uuid );
        //setup listener

        MessageConsumer consumer = createConsumer( communicationManagerImpl.getAmqBroadcastTopic() );

        communicationManagerImpl.sendBroadcastRequest( request );


        TextMessage txtMsg = ( TextMessage ) consumer.receive();
        String jsonCmd = txtMsg.getText();
        Request request2 = CommandJson.getRequestFromCommandJson( jsonCmd );

        assertEquals( request.getUuid(), request2.getUuid() );
    }


    private MessageConsumer createConsumer( String topicName ) throws JMSException
    {
        Connection connection = communicationManagerImpl.createConnection();
        connection.start();
        Session session = connection.createSession( false, Session.AUTO_ACKNOWLEDGE );
        Destination topic = session.createTopic( topicName );
        return session.createConsumer( topic );
    }


    private static class TestResponseListener implements ResponseListener
    {

        private final Semaphore signal = new Semaphore( 0 );


        public void onResponse( Response response )
        {


            signal.release();
        }
    }
}
