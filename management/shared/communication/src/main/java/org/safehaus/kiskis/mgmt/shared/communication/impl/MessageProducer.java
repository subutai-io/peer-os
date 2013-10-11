/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.shared.communication.impl;

import com.google.gson.Gson;
import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.Session;
import javax.jms.TextMessage;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.safehaus.kiskis.mgmt.shared.protocol.elements.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.elements.ResponseType;

/**
 *
 * @author bahadyr
 */
public class MessageProducer {

    public static void thread(Runnable runnable, boolean daemon) {
        Thread brokerThread = new Thread(runnable);
        brokerThread.setDaemon(daemon);
        brokerThread.start();
    }

    public static class HelloWorldProducer implements Runnable {

        public void run() {
            try {
                ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://127.0.0.1:61616");

                Connection connection = connectionFactory.createConnection("karaf", "karaf");
                connection.start();

                Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

                Destination destination = session.createQueue("SERVICE_QUEUE");

                javax.jms.MessageProducer producer = session.createProducer(destination);
                producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

//                String text = "Hello world! From: " + Thread.currentThread().getName() + " : " + this.hashCode();
                Gson gson = new Gson();
                Response response = new Response();
                response.setExitCode(1);
                response.setRequestSequenceNumber(1l);
                response.setResponseSequenceNumber(1l);
                response.setStdErr("1");
                response.setStdOut("1");
                response.setType(ResponseType.REGISTRATION_REQUEST);
                response.setUuid(java.util.UUID.randomUUID().toString());
                String json = gson.toJson(response);
                TextMessage message = session.createTextMessage(json);
                System.out.println(message);
//                System.out.println("Sent message: " + message.hashCode() + " : " + Thread.currentThread().getName());
                producer.send(message);

                session.close();
                connection.close();
            } catch (Exception e) {
                System.out.println("Caught: " + e);
//                e.printStackTrace();
            }
        }
    }
}
