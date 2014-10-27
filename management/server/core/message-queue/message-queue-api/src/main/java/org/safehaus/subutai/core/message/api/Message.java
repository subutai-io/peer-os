package org.safehaus.subutai.core.message.api;


/**
 * Queue Message
 */
public interface Message
{
    /**
     * Return message payload. Receiver needs to cast it to required target object
     *
     * @return payload
     */
    public Object getPayload() throws MessageException;

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
