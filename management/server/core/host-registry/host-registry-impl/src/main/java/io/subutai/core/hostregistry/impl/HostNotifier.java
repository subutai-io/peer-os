package io.subutai.core.hostregistry.impl;


import java.util.Set;

import io.subutai.common.host.ResourceHostInfo;
import io.subutai.common.metric.ResourceAlert;
import io.subutai.core.hostregistry.api.HostListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Notifies listener on host heartbeat
 */
public class HostNotifier implements Runnable
{
    private static final Logger LOG = LoggerFactory.getLogger( HostNotifier.class.getName() );

    private HostListener listener;
    private ResourceHostInfo info;
    private Set<ResourceAlert> alerts;


    public HostNotifier( final HostListener listener, final ResourceHostInfo info, final Set<ResourceAlert> alerts )
    {
        this.listener = listener;
        this.info = info;
        this.alerts = alerts;
    }


    @Override
    public void run()
    {
        try
        {
            listener.onHeartbeat( info, alerts );
        }
        catch ( Exception e )
        {
            LOG.error( "Error in run", e );
        }
    }
}
