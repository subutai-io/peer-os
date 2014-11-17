package org.safehaus.subutai.core.hostregistry.api;


/**
 * Notifies listener on host heartbeat
 */
public interface HostListener
{
    public void onHeartbeat( ResourceHostInfo resourceHostInfo );
}
