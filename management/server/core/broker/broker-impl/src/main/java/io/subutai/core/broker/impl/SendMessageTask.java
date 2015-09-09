package io.subutai.core.broker.impl;


import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.activemq.ActiveMQConnectionFactory;

import io.subutai.common.settings.Common;


public class SendMessageTask implements Runnable
{
    private static final Logger LOG = LoggerFactory.getLogger( SendMessageTask.class.getName() );

    private final ActiveMQConnectionFactory connectionFactory;
    private Object message;
    private final String topic;


    public SendMessageTask( final ActiveMQConnectionFactory connectionFactory, final String topic,
                            final Object message )
    {
        this.connectionFactory = connectionFactory;
        this.topic = topic;
        this.message = message;
    }


    @Override
    public void run()
    {
        Connection connection = null;
        Session session = null;
        MessageProducer producer = null;

        try
        {
            connection = connectionFactory.createConnection();
            connection.start();
            session = connection.createSession( false, Session.AUTO_ACKNOWLEDGE );
            Destination destination = session.createTopic( topic );
            producer = session.createProducer( destination );
            producer.setDeliveryMode( DeliveryMode.PERSISTENT );
            producer.setTimeToLive( Common.BROKER_MESSAGE_TIMEOUT_SEC * 1000 );

            Message msg;
            if ( message instanceof String )
            {

                msg = session.createTextMessage( ( String ) message );
            }
            else
            {
                msg = session.createBytesMessage();
                ( ( BytesMessage ) msg ).writeBytes( ( byte[] ) message );
            }

            producer.send( msg );
        }
        catch ( Exception e )
        {
            LOG.error( "Error in sendMessage", e );
        }
        finally
        {
            if ( producer != null )
            {
                try
                {
                    producer.close();
                }
                catch ( JMSException e )
                {
                    //ignore
                }
            }

            if ( session != null )
            {
                try
                {
                    session.close();
                }
                catch ( JMSException e )
                {
                    //ignore
                }
            }

            if ( connection != null )
            {
                try
                {
                    connection.close();
                }
                catch ( JMSException e )
                {
                    //ignore
                }
            }
        }
    }
}
