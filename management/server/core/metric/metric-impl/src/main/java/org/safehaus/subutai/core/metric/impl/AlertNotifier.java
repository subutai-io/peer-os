package org.safehaus.subutai.core.metric.impl;


import org.safehaus.subutai.core.metric.api.ContainerHostMetric;
import org.safehaus.subutai.core.metric.api.MetricListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;


/**
 * Notifies listener on alert
 */
public class AlertNotifier implements Runnable
{
    protected Logger LOG = LoggerFactory.getLogger( AlertNotifier.class.getName() );

    protected ContainerHostMetric metric;
    protected MetricListener listener;


    public AlertNotifier( final ContainerHostMetric metric, final MetricListener listener )
    {
        Preconditions.checkNotNull( metric, "Metric is null" );
        Preconditions.checkNotNull( listener, "Listener is null" );

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
