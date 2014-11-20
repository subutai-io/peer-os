package org.safehaus.subutai.core.broker.api;


/**
 * Text message listener
 */
public interface TextMessageListener extends MessageListener
{
    public void onMessage( String message );
}
