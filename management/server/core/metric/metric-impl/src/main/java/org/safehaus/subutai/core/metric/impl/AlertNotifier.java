package org.safehaus.subutai.core.metric.impl;


import org.safehaus.subutai.core.metric.api.ContainerHostMetric;
import org.safehaus.subutai.core.metric.api.MetricListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Notifies listener on alert
 */
public class AlertNotifier implements Runnable
{
    protected static final Logger LOG = LoggerFactory.getLogger( AlertNotifier.class.getName() );

    private final ContainerHostMetric metric;
    private final MetricListener listener;


    public AlertNotifier( final ContainerHostMetric metric, final MetricListener listener )
    {
        this.metric = metric;
        this.listener = listener;
    }


    @Override
    public void run()
    {
        try
        {
            listener.alertThresholdExcess( metric );
        }
        catch ( Exception e )
        {
            LOG.error( String.format( "Error notifying %s on %s", listener.getSubscriberId(), metric ), e );
        }
    }
}
