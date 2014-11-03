package org.safehaus.subutai.core.metric.impl;


import org.safehaus.subutai.core.peer.api.Payload;
import org.safehaus.subutai.core.peer.api.RequestListener;


public class RemoteMetricRequestListener extends RequestListener
{
    protected RemoteMetricRequestListener()
    {
        super( RecipientType.METRIC_REQUEST_RECIPIENT.name() );
    }


    @Override
    public Object onRequest( final Payload payload ) throws Exception
    {
        //        ContainerHostMetricRequest metricRequest = (ContainerHostMetricRequest) request;
        //
        //
        //        Set<ContainerHostMetricImpl> metrics = monitor.getLocalContainerHostMetrics( request
        // .getEnvironmentId() );
        //
        //        if ( !metrics.isEmpty() )
        //        {
        //            try
        //            {
        //                Peer sourcePeer = peerManager.getPeer( message.getSourcePeerId() );
        //                Message response =
        //                        messenger.createMessage( new ContainerHostMetricResponse( request.getId(),
        // metrics ) );
        //                messenger.sendMessage( sourcePeer, response, RecipientType.METRIC_RESPONSE_RECIPIENT.name(),
        //                        Constants.METRIC_REQUEST_TIMEOUT );
        //            }
        //            catch ( MessageException e )
        //            {
        //                LOG.error( "Error in ContainerHostMetricRequestListener.onMessage", e );
        //            }
        //        }
        //        else
        //        {
        //            LOG.warn( String.format( "Could not get metrics about environment %s",
        // request.getEnvironmentId() ) );
        //        }
        return null;
    }
}
