package org.safehaus.subutai.core.metric.impl;


import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.core.metric.api.MonitoringSettings;
import org.safehaus.subutai.common.peer.ContainerHost;
import org.safehaus.subutai.core.peer.api.Payload;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.peer.api.RequestListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;


public class MonitoringActivationListener extends RequestListener
{

    private static final Logger LOG = LoggerFactory.getLogger( RemoteMetricRequestListener.class.getName() );

    private MonitorImpl monitor;
    private PeerManager peerManager;


    protected MonitoringActivationListener( MonitorImpl monitor, PeerManager peerManager )
    {
        super( RecipientType.MONITORING_ACTIVATION_RECIPIENT.name() );

        this.monitor = monitor;
        this.peerManager = peerManager;
    }


    @Override
    public Object onRequest( final Payload payload ) throws Exception
    {
        MonitoringActivationRequest request = payload.getMessage( MonitoringActivationRequest.class );

        if ( request != null )
        {
            MonitoringSettings monitoringSettings = request.getMonitoringSettings();
            Set<UUID> containerIds = request.getContainerHostsIds();
            Set<ContainerHost> containerHosts = Sets.newHashSet();

            for ( UUID id : containerIds )
            {
                containerHosts.add( ( ContainerHost ) peerManager.getLocalPeer().bindHost( id ) );
            }

            monitor.activateMonitoringAtLocalContainers( containerHosts, monitoringSettings );
        }
        else
        {
            LOG.warn( "Null request" );
        }
        return null;
    }
}
