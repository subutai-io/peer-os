/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.communicationmanager;

import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.safehaus.kiskis.mgmt.api.communicationmanager.CommandJson;
import org.safehaus.kiskis.mgmt.api.communicationmanager.ResponseListener;
import org.safehaus.kiskis.mgmt.shared.protocol.CommandFactory;
import org.safehaus.kiskis.mgmt.shared.protocol.Request;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.OutputRedirection;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.RequestType;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.ResponseType;

/**
 *
 * @author dilshat
 */
public class CommunicationManagerImplTest {

    private CommunicationManagerImpl communicationManagerImpl = null;

    public CommunicationManagerImplTest() {
    }

    @BeforeClass
    public static void setUpClass() {

    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        communicationManagerImpl = new CommunicationManagerImpl();
        communicationManagerImpl.setAmqBindAddress("0.0.0.0");
        communicationManagerImpl.setAmqPort(61616);
        communicationManagerImpl.setAmqBrokerCertificateName("dummy");
        communicationManagerImpl.setAmqBrokerCertificatePwd("dummy");
        communicationManagerImpl.setAmqBrokerTrustStoreName("dummy");
        communicationManagerImpl.setAmqBrokerTrustStorePwd("dummy");
        communicationManagerImpl.setAmqMaxSenderPoolSize(1);
        communicationManagerImpl.setAmqMaxPooledConnections(1);
        communicationManagerImpl.setAmqServiceQueue("SERVICE_QUEUE");
        communicationManagerImpl.init();
    }

    @After
    public void tearDown() {
        if (communicationManagerImpl != null) {
            communicationManagerImpl.destroy();
        }
    }

    @Test
    public void testInit() {

        assertTrue(communicationManagerImpl.broker.isStarted());
    }

    @Test
    public void testAddListener() {

        communicationManagerImpl.addListener(new ResponseListener() {

            public void onResponse(Response response) {

            }
        });

        assertFalse(communicationManagerImpl.communicationMessageListener.getListeners().isEmpty());
    }

    @Test
    public void testRemoveListener() {
        ResponseListener listener = new ResponseListener() {

            public void onResponse(Response response) {

            }
        };
        communicationManagerImpl.addListener(listener);

        communicationManagerImpl.removeListener(listener);

        assertTrue(communicationManagerImpl.communicationMessageListener.getListeners().isEmpty());
    }

    public static Request getRequestTemplate() {
        return CommandFactory.newRequest(
                RequestType.EXECUTE_REQUEST, // type
                null, //                        !! agent uuid
                null, //                        source
                null, //                        !! task uuid 
                1, //                           !! request sequence number
                "/", //                         cwd
                "pwd", //                        program
                OutputRedirection.RETURN, //    std output redirection 
                OutputRedirection.RETURN, //    std error redirection
                null, //                        stdout capture file path
                null, //                        stderr capture file path
                "root", //                      runas
                null, //                        arg
                null, //                        env vars
                30); //  
    }

    @Test
    public void testSendRequest() throws JMSException {
        Connection connection = null;
        UUID uuid = UUID.randomUUID();
        //setup listener
        connection = communicationManagerImpl.pooledConnectionFactory.createConnection();
        connection.start();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Destination testQueue = session.createQueue(uuid.toString());
        MessageConsumer consumer = session.createConsumer(testQueue);

        Request request = getRequestTemplate();

        request.setUuid(uuid);

        communicationManagerImpl.sendRequest(request);

        TextMessage txtMsg = (TextMessage) consumer.receive();
        String jsonCmd = txtMsg.getText();
        Request request2 = CommandJson.getRequest(jsonCmd);

        assertEquals(request.getUuid(), request2.getUuid());

    }

    private static class TestResponseListener implements ResponseListener {

        private final Object signal = new Object();
        private Response response;

        public void onResponse(Response response) {

            this.response = response;

            synchronized (signal) {
                signal.notify();
            }
        }
    }

    @Test
    public void testMessageReception() throws JMSException, InterruptedException {
        Connection connection = null;

        TestResponseListener responseListener = new TestResponseListener();
        communicationManagerImpl.addListener(responseListener);

        UUID uuid = UUID.randomUUID();
        //setup listener

        connection = communicationManagerImpl.pooledConnectionFactory.createConnection();
        connection.start();
        final Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Destination testQueue = session.createQueue("SERVICE_QUEUE");
        final MessageProducer producer = session.createProducer(testQueue);

        final Response response = new Response();
        response.setUuid(uuid);
        Thread t = new Thread(new Runnable() {

            public void run() {
                try {
                    producer.send(session.createTextMessage(CommandJson.getResponse(response)));
                } catch (JMSException ex) {
                    Logger.getLogger(CommunicationManagerImplTest.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        t.start();
        synchronized (responseListener.signal) {
            responseListener.signal.wait();
        }

        assertEquals(response.getUuid(), responseListener.response.getUuid());

    }

}
