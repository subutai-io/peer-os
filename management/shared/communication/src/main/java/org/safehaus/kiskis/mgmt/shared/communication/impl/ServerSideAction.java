package org.safehaus.kiskis.mgmt.shared.communication.impl;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.Session;
import javax.jms.TextMessage;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.safehaus.kiskis.mgmt.shared.protocol.interfaces.server.CommandSendInterface;
import org.safehaus.kiskismgmt.protocol.CommandJson;
import org.safehaus.kiskismgmt.protocol.Request;
import org.safehaus.kiskismgmt.protocol.Response;

public class ServerSideAction implements CommandSendInterface {

    @Override
    public Response sendCommandToAgent(Request request) {
        System.out.println("Command for Agent is send to ActiveMQ");
        thread(new HelloWorldProducer(request), false);
        return null;
    }

    public static void thread(Runnable runnable, boolean daemon) {
        Thread brokerThread = new Thread(runnable);
        brokerThread.setDaemon(daemon);
        brokerThread.start();
    }

    public static class HelloWorldProducer implements Runnable {

        Request request;

        public HelloWorldProducer(Request request) {
            this.request = request;
        }

        public void run() {
            try {
                System.out.println("SENDING RESPONSE TO AGENT VIA ACTIVEMQ " + request.getUuid());
                ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://127.0.0.1:61616");
                Connection connection = connectionFactory.createConnection("karaf", "karaf");
                connection.start();
                Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                Destination destination = session.createQueue(request.getUuid());
                javax.jms.MessageProducer producer = session.createProducer(destination);
                producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
                String json = CommandJson.getJson(request);
                TextMessage message = session.createTextMessage(json);
//                System.out.println(message);
//                System.out.println("Sent message: " + message.hashCode() + " : " + Thread.currentThread().getName());
                producer.send(message);
                System.out.println("MESSAGE SENT TO AGENT" + message.getText());
                session.close();
                connection.close();
            } catch (Exception e) {
                System.out.println("Caught: " + e);
//                e.printStackTrace();
            }
        }
    }
}
