package io.subutai.core.metric.impl;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import io.subutai.common.peer.AlertListener;
import io.subutai.common.peer.AlertPack;


/**
 * Notifies listener on notifyOnAlert
 */
public class AlertNotifier implements Runnable
{
    protected Logger LOG = LoggerFactory.getLogger( AlertNotifier.class );

    protected AlertPack alert;
    protected AlertListener listener;


    public AlertNotifier( final AlertPack alert, final AlertListener listener )
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
            LOG.debug( String.format( "Alert package '%s' handled by '%s'.", alert.getResource().getId(),
                    listener.getTemplateName() ) );
        }
        catch ( Exception e )
        {
            LOG.error( String.format( "Alert package '%s' handling by '%s' failed.",
                    alert.getResource().getId(), listener.getTemplateName() ),e );
        }
    }
}
