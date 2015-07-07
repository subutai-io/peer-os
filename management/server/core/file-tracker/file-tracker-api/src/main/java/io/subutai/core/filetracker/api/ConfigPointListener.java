package io.subutai.core.filetracker.api;


import org.safehaus.subutai.common.peer.Host;


/**
 * Interface to be implemented by clients wishing to be notified about config point change events
 */
public interface ConfigPointListener
{
    /**
     * Triggered on i_notify event from some host
     *
     * @param host - source host
     * @param eventType - type of event
     * @param configPoint - config point which generated the event
     */
    public void onConfigPointChangeEvent( Host host, InotifyEventType eventType, String configPoint );
}