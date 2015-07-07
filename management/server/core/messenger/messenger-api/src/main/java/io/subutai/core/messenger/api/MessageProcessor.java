package io.subutai.core.messenger.api;


/**
 * Interface exposed by messenger for REST endpoint
 */
public interface MessageProcessor
{
    public void processMessage( String envelope ) throws MessageException;
}
