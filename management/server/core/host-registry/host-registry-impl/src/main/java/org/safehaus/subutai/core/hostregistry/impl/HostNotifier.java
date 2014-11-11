package org.safehaus.subutai.core.hostregistry.impl;


import org.safehaus.subutai.core.hostregistry.api.HostInfo;
import org.safehaus.subutai.core.hostregistry.api.HostListener;


/**
 * Notifies listener on host heartbeat
 */
public class HostNotifier implements Runnable
{
    private HostListener listener;
    private HostInfo info;


    public HostNotifier( final HostListener listener, final HostInfo info )
    {
        this.listener = listener;
        this.info = info;
    }


    @Override
    public void run()
    {
        listener.onHeartbeat( info );
    }
}
