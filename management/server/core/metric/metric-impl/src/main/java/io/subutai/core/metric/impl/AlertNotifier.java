package io.subutai.core.metric.impl;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import io.subutai.common.peer.AlertListener;
import io.subutai.common.peer.AlertEvent;


/**
 * Notifies listener on notifyOnAlert
 */
public class AlertNotifier implements Runnable
{
    protected Logger LOG = LoggerFactory.getLogger( AlertNotifier.class );

    protected AlertEvent alert;
    protected AlertListener listener;


    public AlertNotifier( final AlertEvent alert, final AlertListener listener )
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
            LOG.debug( String.format( "Alert listener '%s' notified about alert '%s'.", listener.getId(),
                    alert.getResource().getId() ) );
            listener.onAlert( alert );
        }
        catch ( Exception e )
        {
            LOG.debug( String.format( "Error on notifying '%s' notified about alert '%s'.", listener.getId(),
                    alert.getResource().getId() ) );
        }
    }
}
