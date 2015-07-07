package io.subutai.core.broker.api;


/**
 * Text message listener
 */
public interface TextMessageListener extends MessageListener
{
    /**
     * Triggered on incoming text message
     *
     * @param message - incoming message
     */
    public void onMessage( String message );
}
