package org.safehaus.subutai.core.message.api;


/**
 * Interface exposed by message queue for REST endpoint
 */
public interface MessageProcessor
{
    public void processMessage( String messageJson ) throws MessageException;
}
