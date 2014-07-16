package org.safehaus.subutai.impl.fstracker;


import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

import org.safehaus.subutai.shared.protocol.Response;
import org.safehaus.subutai.shared.protocol.enums.ResponseType;

import org.apache.activemq.command.ActiveMQMessage;
import org.apache.activemq.command.RemoveInfo;


class TestMessageListener implements MessageListener {


    @Override
    public void onMessage( Message message ) {

        try {
            if ( message instanceof BytesMessage ) {
//                System.out.println( ">> BytesMessage" );
                handleBytesMessage( (BytesMessage) message );
            }
            else if ( message instanceof ActiveMQMessage ) {
//                System.out.println( ">> ActiveMQMessage" );
//                printMessage((ActiveMQMessage) message);
            }
        }
        catch ( Exception e ) {
            e.printStackTrace();
        }
    }


    private static void handleBytesMessage( BytesMessage msg ) throws Exception {

        byte[] msg_bytes = new byte[( int ) msg.getBodyLength()];
        msg.readBytes( msg_bytes );
        String jsonCmd = new String( msg_bytes, "UTF-8" );

        System.out.println( "inotify response: " + jsonCmd );


        /*if ( response != null ) {
            if ( response.getType() != ResponseType.HEARTBEAT_RESPONSE ) {
                LOG.log( Level.INFO, "\nReceived {0}",
                        CommandJson.getJson( CommandJson.getCommand( jsonCmd ) ) );
            }
            else {
                LOG.log( Level.INFO, "Heartbeat from {0}", response.getHostname() );
            }
            response.setTransportId( ( ( ActiveMQMessage ) message ).getProducerId().toString() );
            notifyListeners( response );
        }
        else {
            LOG.log( Level.WARNING, "Could not parse response{0}", jsonCmd );
        }*/

    }


    private static void printMessage( ActiveMQMessage message ) throws JMSException {

//        Response agentDisconnect = new Response();
//        agentDisconnect.setType( ResponseType.AGENT_DISCONNECT );
//        agentDisconnect.setTransportId( ( ( RemoveInfo ) message.getDataStructure() ).getObjectId().toString() );


//        System.out.println( " --- ");
//        System.out.println( ">> message: " + message );
//        System.out.println( ">> dataStructure: " + message.getDataStructure() );
//        System.out.println( " --- ");


/*        if ( message.getDataStructure() instanceof RemoveInfo ) {
            Response agentDisconnect = new Response();
            agentDisconnect.setType( ResponseType.AGENT_DISCONNECT );
            agentDisconnect
                    .setTransportId( ( ( RemoveInfo ) aMsg.getDataStructure() ).getObjectId().toString() );
            notifyListeners( agentDisconnect );
        }

        Enumeration e = message.getAllPropertyNames();

        while ( e.hasMoreElements() ) {
            System.out.println( "name: " + e.nextElement() );
        }*/


    }

}
