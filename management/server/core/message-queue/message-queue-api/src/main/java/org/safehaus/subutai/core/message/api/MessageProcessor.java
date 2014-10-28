package org.safehaus.subutai.core.message.api;


/**
 * Interface exposed by messenger for REST endpoint
 */
public interface MessageProcessor
{
    public void processMessage( String envelope ) throws MessageException;
}
