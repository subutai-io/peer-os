package io.subutai.core.metric.impl;


import java.util.Set;

import org.safehaus.subutai.core.peer.api.Payload;
import org.safehaus.subutai.core.peer.api.RequestListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RemoteMetricRequestListener extends RequestListener
{
    private static final Logger LOG = LoggerFactory.getLogger( RemoteMetricRequestListener.class.getName() );

    private MonitorImpl monitor;


    public RemoteMetricRequestListener( MonitorImpl monitor )
    {
        super( RecipientType.METRIC_REQUEST_RECIPIENT.name() );

        this.monitor = monitor;
    }


    @Override
    public Object onRequest( final Payload payload ) throws Exception
    {
        ContainerHostMetricRequest request = payload.getMessage( ContainerHostMetricRequest.class );

        if ( request != null )
        {
            Set<ContainerHostMetricImpl> metrics = monitor.getLocalContainerHostsMetrics( request.getEnvironmentId() );

            if ( !metrics.isEmpty() )
            {
                return new ContainerHostMetricResponse( metrics );
            }
            else
            {
                LOG.warn( String.format( "Could not get metrics about environment %s", request.getEnvironmentId() ) );
            }
        }
        else
        {
            LOG.warn( "Null request" );
        }
        return null;
    }
}
