package org.safehaus.subutai.core.broker.api;


/**
 * Marker interface for all message listeners
 */
public interface MessageListener
{
    /**
     * Topic to which this listener subscribes
     *
     * @return - topic to subscribe to
     */
    public Topic getTopic();
}
