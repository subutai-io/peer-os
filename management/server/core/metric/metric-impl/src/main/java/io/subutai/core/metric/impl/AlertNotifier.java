package io.subutai.core.metric.impl;


import io.subutai.common.metric.Alert;
import io.subutai.common.metric.ContainerHostMetric;
import io.subutai.core.metric.api.AlertListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;


/**
 * Notifies listener on notifyOnAlert
 */
public class AlertNotifier implements Runnable
{
    protected Logger LOG = LoggerFactory.getLogger( AlertNotifier.class );

    protected Alert alert;
    protected AlertListener listener;


    public AlertNotifier( final Alert alert, final AlertListener listener )
    {
        Preconditions.checkNotNull( alert, "Alert is null" );
        Preconditions.checkNotNull( listener, "Listener is null" );

        this.alert = alert;
        this.listener = listener;
    }


    @Override
    public void run()
    {
        try
        {
            listener.onAlert( alert );
        }
        catch ( Exception e )
        {
            LOG.error( String.format( "Error notifying %s on %s", listener.getSubscriberId(), alert ), e );
        }
    }
}
