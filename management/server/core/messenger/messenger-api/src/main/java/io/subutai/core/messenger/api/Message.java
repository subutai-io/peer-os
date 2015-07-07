package io.subutai.core.messenger.api;


import java.util.UUID;


/**
 * Message
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
     * Returns source peer id of message
     *
     * @return - source peer id
     */
    public UUID getSourcePeerId();

    /**
     * Returns original payload
     *
     * @param clazz - payload class
     *
     * @return - payload
     */
    public <T> T getPayload( Class<T> clazz );

    /**
     * Returns sender of message or null if it was not set by calling party
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


    public String getPayload();
}
