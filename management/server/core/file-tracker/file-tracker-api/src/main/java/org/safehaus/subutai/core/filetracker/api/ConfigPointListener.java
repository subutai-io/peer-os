package org.safehaus.subutai.core.filetracker.api;


import org.safehaus.subutai.core.peer.api.Host;


/**
 * Interface to be implemented by clients wishing to be notified about config point change events
 */
public interface ConfigPointListener
{
    public void onConfigPointChangeEvent( Host host, InotifyEventType eventType, String configPoint );
}