package org.safehaus.subutai.core.containerregistry.impl;


import org.safehaus.subutai.core.containerregistry.api.HostInfo;
import org.safehaus.subutai.core.containerregistry.api.HostListener;


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
