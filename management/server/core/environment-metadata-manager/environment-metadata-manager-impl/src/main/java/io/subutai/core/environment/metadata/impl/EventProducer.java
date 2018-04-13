package io.subutai.core.environment.metadata.impl;


import java.net.URI;
import java.net.URISyntaxException;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.activemq.ActiveMQConnectionFactory;


public class EventProducer implements Runnable
{
    private static final Logger LOG = LoggerFactory.getLogger( EventProducer.class );
    private final String destination;
    private String message;

    private URI uri;
    private ActiveMQConnectionFactory connectionFactory;


    public EventProducer( final URI uri, final String destination, final String message ) throws URISyntaxException
    {
        this.uri = uri;
        this.destination = destination;
        this.message = message;
        // Create a ConnectionFactory
        connectionFactory = new ActiveMQConnectionFactory( uri );
    }


    @Override
    public void run()
    {
        // Create a Connection
        Connection connection = null;
        try
        {
            connection = connectionFactory.createConnection();

            connection.start();

            // Create a Session
            Session session = connection.createSession( false, Session.AUTO_ACKNOWLEDGE );

            // Create the destination (Topic or Queue)
            Destination destination = session.createTopic( this.destination );

            // Create a MessageProducer from the Session to the Topic or Queue
            MessageProducer producer = session.createProducer( destination );
            producer.setDeliveryMode( DeliveryMode.NON_PERSISTENT );

            // Create a messages
            TextMessage message = session.createTextMessage( this.message );

            // Tell the producer to send the message
            producer.send( message );

            // Clean up
            session.close();
            connection.close();
        }
        catch ( JMSException e )
        {
            LOG.error( e.getMessage(), e );
        }
    }
}
