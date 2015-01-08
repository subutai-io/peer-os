package org.safehaus.subutai.core.messenger.api;


import java.util.UUID;

import org.safehaus.subutai.core.peer.api.Peer;


/**
 * Messenger API
 */
public interface Messenger
{

    /**
     * Creates message with specified payload
     *
     * @param payload - object to send. Object must not be of interface type and must not contain any interface types
     *
     * @return - message
     */
    public Message createMessage( Object payload );


    /**
     * Sends message to recipient
     *
     * @param peer - target peer
     * @param message - message to send
     * @param recipient - recipient of message
     * @param timeToLive - time-to-live (in seconds) of message within which message is retried to be sent to recipient.
     * After ttl expires and message is still not sent, it expires
     */
    public void sendMessage( final Peer peer, final Message message, final String recipient, final int timeToLive )
            throws MessageException;

    /**
     * Returns status of message
     *
     * @param messageId - id of message
     *
     * @return - status of message
     */
    public MessageStatus getMessageStatus( UUID messageId ) throws MessageException;

    /**
     * Add listener of messages
     *
     * @param listener - listener
     */
    public void addMessageListener( MessageListener listener );

    /**
     * Remove listener
     *
     * @param listener - listener
     */
    public void removeMessageListener( MessageListener listener );
}
