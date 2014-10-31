package org.safehaus.subutai.core.metric.impl;


import org.safehaus.subutai.core.messenger.api.Message;
import org.safehaus.subutai.core.messenger.api.MessageListener;
import org.safehaus.subutai.core.metric.api.ContainerHostMetric;
import org.safehaus.subutai.core.metric.api.MonitorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Listens to alerts from remote peers
 */
public class RemoteAlertListener extends MessageListener
{
    public static final String ALERT_RECIPIENT = "alert";
    protected static Logger LOG = LoggerFactory.getLogger( RemoteAlertListener.class.getName() );

    protected MonitorImpl monitor;


    protected RemoteAlertListener( MonitorImpl monitor )
    {
        super( ALERT_RECIPIENT );
        this.monitor = monitor;
    }


    @Override
    public void onMessage( final Message message )
    {
        ContainerHostMetric containerHostMetric = message.getPayload( ContainerHostMetricImpl.class );
        try
        {
            monitor.alertThresholdExcess( containerHostMetric );
        }
        catch ( MonitorException e )
        {
            LOG.error( "Error in RemoteAlertListener.onMessage", e );
        }
    }
}
