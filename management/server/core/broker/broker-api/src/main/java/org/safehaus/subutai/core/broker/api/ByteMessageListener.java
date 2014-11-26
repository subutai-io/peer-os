package org.safehaus.subutai.core.broker.api;


/**
 * Byte message listener
 */
public interface ByteMessageListener extends MessageListener
{
    /**
     * Triggered on incoming byte message
     *
     * @param message - incoming message
     */
    public void onMessage( byte[] message );
}
