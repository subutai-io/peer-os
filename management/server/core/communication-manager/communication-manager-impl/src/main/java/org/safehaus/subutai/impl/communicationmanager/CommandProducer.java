/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.impl.communicationmanager;


import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.safehaus.subutai.api.communicationmanager.CommandJson;
import org.safehaus.subutai.shared.protocol.Request;
import org.safehaus.subutai.shared.protocol.enums.RequestType;


/**
 * This class is used internally by CommunicationManagerImpl for sending requests to agents.
 */
class CommandProducer implements Runnable {

    private static final Logger LOG = Logger.getLogger( CommandProducer.class.getName() );
    private final Request command;
    private final CommunicationManagerImpl communicationManagerImpl;


    public CommandProducer( Request command, CommunicationManagerImpl communicationManagerImpl ) {
        this.communicationManagerImpl = communicationManagerImpl;
        this.command = command;
    }


    /**
     * Called by executor to send message to agent
     */
    public void run() {
        Connection connection = null;
        Session session = null;
        MessageProducer producer = null;
        try {
            connection = communicationManagerImpl.createConnection();
            connection.start();
            session = connection.createSession( false, Session.AUTO_ACKNOWLEDGE );
            Destination destination = session.createTopic( command.getUuid().toString() );
            producer = session.createProducer( destination );
            producer.setDeliveryMode( communicationManagerImpl.isPersistentMessages() ? DeliveryMode.PERSISTENT :
                                      DeliveryMode.NON_PERSISTENT );
            producer.setTimeToLive( communicationManagerImpl.getAmqMaxMessageToAgentTtlSec() * 1000 );
            String json = CommandJson.getJson( command );
            if ( !RequestType.HEARTBEAT_REQUEST.equals( command.getType() ) ) {
                LOG.log( Level.INFO, "\nSending: {0}", json );
            }
            TextMessage message = session.createTextMessage( json );
            producer.send( message );
        }
        catch ( JMSException ex ) {
            LOG.log( Level.SEVERE, "Error in CommandProducer.run", ex );
        }
        finally {
            if ( producer != null ) {
                try {
                    producer.close();
                }
                catch ( Exception e ) {
                }
            }
            if ( session != null ) {
                try {
                    session.close();
                }
                catch ( Exception e ) {
                }
            }
            if ( connection != null ) {
                try {
                    connection.close();
                }
                catch ( Exception e ) {
                }
            }
        }
    }
}
