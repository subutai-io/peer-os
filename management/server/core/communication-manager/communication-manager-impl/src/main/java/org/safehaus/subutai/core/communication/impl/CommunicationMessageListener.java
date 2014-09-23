package org.safehaus.subutai.core.communication.impl;


import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.jms.BytesMessage;
import javax.jms.Message;
import javax.jms.MessageListener;

import org.safehaus.subutai.common.enums.ResponseType;
import org.safehaus.subutai.common.protocol.Response;
import org.safehaus.subutai.common.protocol.ResponseListener;
import org.safehaus.subutai.core.communication.api.CommandJson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.activemq.command.ActiveMQMessage;
import org.apache.activemq.command.RemoveInfo;


/**
 * Used internally by CommunicationManagerImpl to notify a response listener on a new message.
 */
class CommunicationMessageListener implements MessageListener
{

    private static final Logger LOG = LoggerFactory.getLogger( CommunicationMessageListener.class.getName() );

    private final Queue<ResponseListener> listeners = new ConcurrentLinkedQueue<>();


    /**
     * New message handler called by amq broker
     *
     * @param message - received message
     */
    @Override
    public void onMessage( Message message )
    {
        try
        {
            if ( message instanceof BytesMessage )
            {
                BytesMessage msg = ( BytesMessage ) message;

                byte[] msgBytes = new byte[( int ) msg.getBodyLength()];
                msg.readBytes( msgBytes );
                String jsonCmd = new String( msgBytes, "UTF-8" );
                Response response = CommandJson.getResponse( jsonCmd );

                if ( response != null )
                {
                    logResponse( response, jsonCmd );
                    response.setTransportId( ( ( ActiveMQMessage ) message ).getProducerId().toString() );
                    notifyListeners( response );
                }
                else
                {
                    LOG.warn( "Could not parse response{0}", jsonCmd );
                }
            }
            else if ( message instanceof ActiveMQMessage )
            {
                ActiveMQMessage aMsg = ( ActiveMQMessage ) message;

                if ( aMsg.getDataStructure() instanceof RemoveInfo )
                {
                    Response agentDisconnect = new Response();
                    agentDisconnect.setType( ResponseType.AGENT_DISCONNECT );
                    agentDisconnect
                            .setTransportId( ( ( RemoveInfo ) aMsg.getDataStructure() ).getObjectId().toString() );
                    notifyListeners( agentDisconnect );
                }
            }
        }
        catch ( Exception ex )
        {
            LOG.error( "Error in onMessage", ex );
        }
    }


    private void logResponse( Response response, String json )
    {
        if ( response.getType() != ResponseType.HEARTBEAT_RESPONSE )
        {
            LOG.info( "\nReceived {0}", CommandJson.getJson( CommandJson.getCommand( json ) ) );
        }
        else
        {
            LOG.info( "Heartbeat from {0}", response.getHostname() );
        }
    }


    /**
     * Notifies listeners on new response
     *
     * @param response - response to notify listeners
     */
    private void notifyListeners( Response response )
    {
        try
        {
            for ( Iterator<ResponseListener> it = listeners.iterator(); it.hasNext(); )
            {
                ResponseListener listener = it.next();
                if ( !notifyListener( listener, response ) )
                {
                    it.remove();
                }
            }
        }
        catch ( Exception ex )
        {
            LOG.error(  "Error in notifyListeners", ex );
        }
    }


    private boolean notifyListener( ResponseListener listener, Response response )
    {
        try
        {
            listener.onResponse( response );
            return true;
        }
        catch ( Exception e )
        {
            LOG.error(  "Error notifying message listeners", e );
        }
        return false;
    }


    /**
     * Adds response listener
     *
     * @param listener - listener to add
     */
    public void addListener( ResponseListener listener )
    {
        try
        {
            if ( !listeners.contains( listener ) )
            {
                listeners.add( listener );
            }
        }
        catch ( Exception ex )
        {
            LOG.error(  "Error to add a listener:", ex );
        }
    }


    /**
     * Removes response listener
     *
     * @param listener - - listener to remove
     */
    public void removeListener( ResponseListener listener )
    {
        try
        {
            listeners.remove( listener );
        }
        catch ( Exception ex )
        {
            LOG.error(  "Error in removeListener", ex );
        }
    }


    /**
     * Returns collection of listeners
     *
     * @return - listeners added
     */
    Collection<ResponseListener> getListeners()
    {
        return Collections.unmodifiableCollection( listeners );
    }


    /**
     * Disposes message listener
     */
    public void destroy()
    {
        listeners.clear();
    }
}
