package io.subutai.core.metric.impl;


import io.subutai.common.metric.Alert;
import io.subutai.core.metric.api.MonitorException;
import io.subutai.common.peer.Payload;
import io.subutai.common.peer.RequestListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Listens to alerts from remote peers
 */
public class RemoteAlertListener extends RequestListener
{
    private static final Logger LOG = LoggerFactory.getLogger( RemoteAlertListener.class.getName() );

    protected MonitorImpl monitor;


    public RemoteAlertListener( MonitorImpl monitor )
    {
        super( RecipientType.ALERT_RECIPIENT.name() );
        this.monitor = monitor;
    }


    @Override
    public Object onRequest( final Payload payload )
    {
        Alert containerHostMetric = payload.getMessage( Alert.class );

//        try
//        {
////            monitor.notifyOnAlert( containerHostMetric );
//        }
//        catch ( MonitorException e )
//        {
//            LOG.error( "Error in RemoteAlertListener.onMessage", e );
//        }
//
        return null;
    }
}
