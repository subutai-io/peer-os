package org.safehaus.subutai.core.message.api;


import java.io.Serializable;
import java.util.UUID;


/**
 * Message Queue API.
 */
public interface Queue
{

    /**
     * Creates message with specified payload
     *
     * @param payload - object to send
     *
     * @return - message envelope
     */
    public Message createMessage( Serializable payload );

    /**
     * Sends message to recipient
     *
     * @param message - message to send
     * @param recipient - recipient of message
     * @param ttl - time-to-live of message within which message is retried to be sent to recipient. After ttl expires
     * and message is still not sent, it expires
     *
     * @return -id of message
     */
    public UUID sendMessage( Message message, String recipient, long ttl );

    /**
     * Sends message to recipient
     *
     * @param message - message to send
     * @param recipient - recipient of message
     * @param ttl - time-to-live of message within which message is retried to be sent to recipient. After ttl expires
     * and message is still not sent, it expires
     * @param sender - sender of message for reference by recipient
     *
     * @return -id of message
     */
    public UUID sendMessage( Message message, String recipient, long ttl, String sender );

    /**
     * Returns status of message in queue
     *
     * @param messageId - id of message
     *
     * @return - status of message
     */
    public MessageStatus getMessageStatus( UUID messageId );
}
