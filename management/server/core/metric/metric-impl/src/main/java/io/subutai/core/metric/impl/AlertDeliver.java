package io.subutai.core.metric.impl;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import io.subutai.common.peer.AlertEvent;
import io.subutai.common.peer.Peer;


/**
 * Delivers alerts to owner peer
 */
public class AlertDeliver implements Runnable
{
    protected Logger LOG = LoggerFactory.getLogger( AlertDeliver.class );

    protected AlertEvent alert;
    protected Peer peer;


    public AlertDeliver( Peer peer, final AlertEvent alert )
    {
        Preconditions.checkNotNull( alert, "Alert is null" );
        Preconditions.checkNotNull( peer, "Peer is null" );

        this.alert = alert;
        this.peer = peer;
    }


    @Override
    public void run()
    {

        try
        {
            peer.alert( alert );
            alert.setDelivered( true );
            LOG.debug( String.format( "Alert package '%s' delivered to '%s'.", alert.getResource().getId(),
                    alert.getPeerId() ) );
        }
        catch ( Exception e )
        {
            LOG.warn( String.format( "Delivering alert package '%s' to '%s' failed. Error: %s",
                    alert.getResource().getId(), alert.getPeerId(), e.getMessage() ) );
        }
    }
}
