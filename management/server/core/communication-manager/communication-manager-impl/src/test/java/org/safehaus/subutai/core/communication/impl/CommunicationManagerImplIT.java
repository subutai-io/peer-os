/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.core.communication.impl;


import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.safehaus.subutai.common.protocol.Request;
import org.safehaus.subutai.common.protocol.Response;
import org.safehaus.subutai.common.protocol.ResponseListener;
import org.safehaus.subutai.core.communication.api.CommandJson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 * Test for CommunicationManager class <p/> TODO Add embedded broker for unit tests
 */
@Ignore
public class CommunicationManagerImplIT
{

    private static CommunicationManagerImpl communicationManagerImpl = null;


    @BeforeClass
    public static void setUpClass()
    {
        communicationManagerImpl = new CommunicationManagerImpl();
        communicationManagerImpl.setAmqMaxMessageToAgentTtlSec( 5 );
        communicationManagerImpl.setAmqUrl( "some-url" );
        communicationManagerImpl.setAmqMaxSenderPoolSize( 1 );
        communicationManagerImpl.setAmqMaxPooledConnections( 1 );
        communicationManagerImpl.setAmqServiceTopic( "SERVICE_QUEUE" );
        communicationManagerImpl.init();
    }


    @AfterClass
    public static void tearDownClass()
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
        Connection connection;
        UUID uuid = UUID.randomUUID();
        //setup listener
        connection = communicationManagerImpl.createConnection();
        connection.start();
        Session session = connection.createSession( false, Session.AUTO_ACKNOWLEDGE );
        Destination testQueue = session.createQueue( uuid.toString() );
        MessageConsumer consumer = session.createConsumer( testQueue );

        Request request = TestUtils.getRequestTemplate( uuid );

        communicationManagerImpl.sendRequest( request );

        TextMessage txtMsg = ( TextMessage ) consumer.receive();
        String jsonCmd = txtMsg.getText();
        Request request2 = CommandJson.getRequest( jsonCmd );

        assertEquals( request.getUuid(), request2.getUuid() );
    }


    @Test
    public void testMessageReception() throws JMSException, InterruptedException
    {
        Connection connection;

        TestResponseListener responseListener = new TestResponseListener();
        communicationManagerImpl.addListener( responseListener );

        //setup listener

        connection = communicationManagerImpl.createConnection();
        connection.start();
        final Session session = connection.createSession( false, Session.AUTO_ACKNOWLEDGE );
        Destination testQueue = session.createQueue( "SERVICE_QUEUE" );
        final MessageProducer producer = session.createProducer( testQueue );

        final Response response = new Response();

        Thread t = new Thread( new Runnable()
        {

            public void run()
            {
                try
                {
                    producer.send( session.createTextMessage( CommandJson.getResponse( response ) ) );
                }
                catch ( JMSException ex )
                {
                    Logger.getLogger( CommunicationManagerImplIT.class.getName() ).log( Level.SEVERE, null, ex );
                }
            }
        } );
        t.start();
        synchronized ( responseListener.signal )
        {
            responseListener.signal.wait();
        }

        assertEquals( response.getUuid(), responseListener.response.getUuid() );
    }


    private static class TestResponseListener implements ResponseListener
    {

        private final Object signal = new Object();
        private Response response;


        public void onResponse( Response response )
        {

            this.response = response;

            synchronized ( signal )
            {
                signal.notify();
            }
        }
    }
}
