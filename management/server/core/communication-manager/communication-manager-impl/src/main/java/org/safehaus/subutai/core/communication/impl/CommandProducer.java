package org.safehaus.subutai.core.communication.impl;


import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.safehaus.subutai.common.enums.RequestType;
import org.safehaus.subutai.common.protocol.Request;
import org.safehaus.subutai.core.communication.api.CommandJson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class is used internally by CommunicationManagerImpl for sending requests to agents.
 */
class CommandProducer implements Runnable
{

    private static final Logger LOG = LoggerFactory.getLogger( CommandProducer.class.getName() );
    private final Request command;
    private final CommunicationManagerImpl communicationManagerImpl;
    private final boolean isBroadcast;


    public CommandProducer( Request command, CommunicationManagerImpl communicationManagerImpl )
    {
        this( command, communicationManagerImpl, false );
    }


    public CommandProducer( final Request command, final CommunicationManagerImpl communicationManagerImpl,
                            final boolean isBroadcast )
    {
        this.command = command;
        this.communicationManagerImpl = communicationManagerImpl;
        this.isBroadcast = isBroadcast;
    }


    /**
     * Called by executor to send message to agent
     */
    public void run()
    {
        Connection connection = null;
        Session session = null;
        MessageProducer producer = null;
        try
        {
            connection = communicationManagerImpl.createConnection();
            connection.start();
            session = connection.createSession( false, Session.AUTO_ACKNOWLEDGE );
            Destination destination = session.createTopic(
                    isBroadcast ? communicationManagerImpl.getAmqBroadcastTopic() : command.getUuid().toString() );
            producer = session.createProducer( destination );
            producer.setDeliveryMode( communicationManagerImpl.isPersistentMessages() ? DeliveryMode.PERSISTENT :
                                      DeliveryMode.NON_PERSISTENT );
            producer.setTimeToLive( communicationManagerImpl.getAmqMaxMessageToAgentTtlSec() * 1000 );
            String json = CommandJson.getRequestCommandJson( command );

            if ( !RequestType.HEARTBEAT_REQUEST.equals( command.getType() ) )
            {
                LOG.info( "\nSending: {}", json );
            }

            TextMessage message = session.createTextMessage( json );
            producer.send( message );
        }
        catch ( JMSException e )
        {
            LOG.error( "Error to send a message: ", e );
        }
        finally
        {
            closeSilently( producer );
            closeSilently( session );
            closeSilently( connection );
        }
    }


    private void closeSilently( MessageProducer producer )
    {
        if ( producer != null )
        {
            try
            {
                producer.close();
            }
            catch ( Exception ignore )
            {
            }
        }
    }


    private void closeSilently( Session session )
    {
        if ( session != null )
        {
            try
            {
                session.close();
            }
            catch ( Exception ignore )
            {
            }
        }
    }


    private void closeSilently( Connection connection )
    {
        if ( connection != null )
        {
            try
            {
                connection.close();
            }
            catch ( Exception ignore )
            {
            }
        }
    }
}
