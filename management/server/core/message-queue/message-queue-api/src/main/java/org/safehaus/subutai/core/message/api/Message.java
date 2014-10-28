package org.safehaus.subutai.core.message.api;


import java.util.UUID;


/**
 * Queue Message
 */
public interface Message
{

    /**
     * Returns id of message
     *
     * @return - id of message
     */
    public UUID getId();

    /**
     * Return message payload. Receiver needs to cast it to required target object
     *
     * @return payload
     */
    public <T> T getPayload( Class<T> clazz );

    /**
     * Returns sender of message
     *
     * @return - sender
     */
    public String getSender();


    /**
     * Sets sender of message
     *
     * @param sender - sender of message
     */
    public void setSender( String sender );
}
