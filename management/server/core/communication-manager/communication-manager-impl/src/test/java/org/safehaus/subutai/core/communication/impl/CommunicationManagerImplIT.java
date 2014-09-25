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
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.core.communication.api.CommandJson;
import org.slf4j.LoggerFactory;

import com.jayway.awaitility.Awaitility;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 * Test for CommunicationManager class <p/> TODO Add embedded broker for unit tests
 */
public class CommunicationManagerImplIT
{
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger( CommunicationManagerImpl.class.getName() );

    private CommunicationManagerImpl communicationManagerImpl = null;
    private static final String SERVICE_QUEUE_NAME = "SERVICE_QUEUE";


    @Before
    public void setUpClass() throws Exception
    {

        //init target object
        communicationManagerImpl = new CommunicationManagerImpl();
        communicationManagerImpl.setAmqMaxMessageToAgentTtlSec( 30 );
        communicationManagerImpl.setAmqUrl( "vm://localhost?broker.persistent=false" );
        communicationManagerImpl.setPersistentMessages( false );
        communicationManagerImpl.setAmqMaxSenderPoolSize( 5 );
        communicationManagerImpl.setAmqMaxPooledConnections( 5 );
        communicationManagerImpl.setAmqServiceTopic( SERVICE_QUEUE_NAME );
        communicationManagerImpl.init();
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
        Request request2 = CommandJson.getRequest( jsonCmd );

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
        Destination topic = session.createTopic( SERVICE_QUEUE_NAME );
        final MessageProducer producer = session.createProducer( topic );

        BytesMessage message = session.createBytesMessage();
        message.writeBytes( CommandJson.getResponse( response ).getBytes() );
        producer.send( message );

        Awaitility.await().atMost( 1, TimeUnit.SECONDS ).with().pollInterval( 50, TimeUnit.MILLISECONDS ).and()
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

        MessageConsumer consumer = createConsumer( Common.BROADCAST_TOPIC );

        communicationManagerImpl.sendBroadcastRequest( request );


        TextMessage txtMsg = ( TextMessage ) consumer.receive();
        String jsonCmd = txtMsg.getText();
        Request request2 = CommandJson.getRequest( jsonCmd );

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
        private Response response;


        public void onResponse( Response response )
        {

            this.response = response;

            signal.release();
        }
    }
}
