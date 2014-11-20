package org.safehaus.subutai.core.broker.api;


/**
 * Byte message listener
 */
public interface ByteMessageListener extends MessageListener
{
    public void onMessage( byte[] message );
}
