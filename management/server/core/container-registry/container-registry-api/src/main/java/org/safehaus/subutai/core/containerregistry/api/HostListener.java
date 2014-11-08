package org.safehaus.subutai.core.containerregistry.api;


/**
 * Notifies listener on host heartbeat
 */
public interface HostListener
{
    public void onHeartbeat( HostInfo hostInfo );
}
