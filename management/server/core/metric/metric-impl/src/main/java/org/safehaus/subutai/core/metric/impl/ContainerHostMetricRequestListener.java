package org.safehaus.subutai.core.metric.impl;


import java.util.Set;

import org.safehaus.subutai.core.messenger.api.Message;
import org.safehaus.subutai.core.messenger.api.MessageException;
import org.safehaus.subutai.core.messenger.api.MessageListener;
import org.safehaus.subutai.core.messenger.api.Messenger;
import org.safehaus.subutai.core.metric.api.ContainerHostMetric;
import org.safehaus.subutai.core.peer.api.Peer;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ContainerHostMetricRequestListener extends MessageListener
{
    private static final Logger LOG = LoggerFactory.getLogger( ContainerHostMetricRequestListener.class.getName() );

    private Messenger messenger;
    private PeerManager peerManager;
    private MonitorImpl monitor;


    protected ContainerHostMetricRequestListener( MonitorImpl monitor, Messenger messenger, PeerManager peerManager )
    {
        super( RecipientType.METRIC_REQUEST_RECIPIENT.name() );

        this.monitor = monitor;
        this.messenger = messenger;
        this.peerManager = peerManager;
    }


    @Override
    public void onMessage( final Message message )
    {
        ContainerHostMetricRequest request = message.getPayload( ContainerHostMetricRequest.class );


        Set<ContainerHostMetricImpl> metrics = monitor.getLocalContainerHostMetrics( request.getEnvironmentId() );

        if ( !metrics.isEmpty() )
        {
            try
            {
                Peer sourcePeer = peerManager.getPeer( message.getSourcePeerId() );
                Message response =
                        messenger.createMessage( new ContainerHostMetricResponse( request.getId(), metrics ) );
                messenger.sendMessage( sourcePeer, response, RecipientType.METRIC_RESPONSE_RECIPIENT.name(),
                        Constants.METRIC_REQUEST_TIMEOUT );
            }
            catch ( MessageException e )
            {
                LOG.error( "Error in ContainerHostMetricRequestListener.onMessage", e );
            }
        }
        else
        {
            LOG.warn( String.format( "Could not get metrics about environment %s", request.getEnvironmentId() ) );
        }
    }
}
