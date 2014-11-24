package org.safehaus.subutai.core.metric.impl;


import org.safehaus.subutai.core.peer.api.Payload;
import org.safehaus.subutai.core.peer.api.RequestListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MonitoringActivationListener extends RequestListener
{

    private static final Logger LOG = LoggerFactory.getLogger( RemoteMetricRequestListener.class.getName() );

    private MonitorImpl monitor;


    protected MonitoringActivationListener( MonitorImpl monitor )
    {
        super( RecipientType.MONITORING_ACTIVATION_RECIPIENT.name() );

        this.monitor = monitor;
    }


    @Override
    public Object onRequest( final Payload payload ) throws Exception
    {
        MonitoringActivationRequest request = payload.getMessage( MonitoringActivationRequest.class );

        if ( request != null )
        {

        }
        else
        {
            LOG.warn( "Null request" );
        }
        return null;
    }
}
