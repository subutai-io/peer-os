package io.subutai.core.hostregistry.impl;


import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.host.ContainerHostInfo;
import io.subutai.common.host.ResourceHostInfo;
import io.subutai.common.metric.QuotaAlertValue;
import io.subutai.common.peer.HostNotFoundException;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.ResourceHost;
import io.subutai.common.peer.ResourceHostException;
import io.subutai.common.util.ServiceLocator;
import io.subutai.core.hostregistry.api.HostListener;


/**
 * Notifies listener on host heartbeat
 */
public class HostNotifier implements Runnable
{
    private static final Logger LOG = LoggerFactory.getLogger( HostNotifier.class.getName() );
    private static final String ERR_MSG_TEMPLATE = "Error notifying host listener: {}";

    private HostListener listener;
    private ResourceHostInfo oldRhInfo;
    private ResourceHostInfo newRhInfo;
    private Set<QuotaAlertValue> alerts;
    private boolean rhDisconnected = false;


    HostNotifier( final HostListener listener, final ResourceHostInfo oldRhInfo, final ResourceHostInfo newRhInfo,
                  final Set<QuotaAlertValue> alerts )
    {
        this.listener = listener;
        this.oldRhInfo = oldRhInfo;
        this.newRhInfo = newRhInfo;
        this.alerts = alerts;
    }


    HostNotifier( final HostListener listener, final ResourceHostInfo oldRhInfo )
    {
        this.listener = listener;
        this.oldRhInfo = oldRhInfo;
        this.rhDisconnected = true;
    }


    @Override
    public void run()
    {
        processHeartbeat( oldRhInfo, newRhInfo, alerts );
    }


    private void processHeartbeat( ResourceHostInfo oldRhInfo, ResourceHostInfo newRhInfo, Set<QuotaAlertValue> alerts )
    {
        if ( rhDisconnected )
        {
            // notify on RH disconnection
            try
            {
                listener.onRhDisconnected( oldRhInfo );
            }
            catch ( Exception e )
            {
                LOG.warn( ERR_MSG_TEMPLATE, e );
            }

            return;
        }


        // 0. notify on heartbeat
        try
        {
            listener.onHeartbeat( newRhInfo, alerts );
        }
        catch ( Exception e )
        {
            LOG.warn( ERR_MSG_TEMPLATE, e );
        }


        if ( oldRhInfo == null )
        {
            // notify on RH connection
            try
            {
                listener.onRhConnected( newRhInfo );
            }
            catch ( Exception e )
            {
                LOG.warn( ERR_MSG_TEMPLATE, e );
            }

            return;
        }


        for ( final ContainerHostInfo newContainerInfo : newRhInfo.getContainers() )
        {
            boolean containerExistedBefore = false;

            for ( final ContainerHostInfo oldContainerInfo : oldRhInfo.getContainers() )
            {
                if ( newContainerInfo.getId().equalsIgnoreCase( oldContainerInfo.getId() ) )
                {
                    // 1. check if container state has changed
                    if ( newContainerInfo.getState() != oldContainerInfo.getState() )
                    {
                        try
                        {
                            listener.onContainerStateChanged( newContainerInfo, oldContainerInfo.getState(),
                                    newContainerInfo.getState() );
                        }
                        catch ( Exception e )
                        {
                            LOG.warn( ERR_MSG_TEMPLATE, e.getMessage() );
                        }
                    }

                    // 2. check if container hostname has changed
                    if ( !newContainerInfo.getHostname().equalsIgnoreCase( oldContainerInfo.getHostname() ) )
                    {
                        try
                        {
                            listener.onContainerHostnameChanged( newContainerInfo, oldContainerInfo.getHostname(),
                                    newContainerInfo.getHostname() );
                        }
                        catch ( Exception e )
                        {
                            LOG.warn( ERR_MSG_TEMPLATE, e.getMessage() );
                        }
                    }


                    containerExistedBefore = true;

                    break;
                }
            }

            // 3. notify that container has been created
            if ( !containerExistedBefore )
            {
                try
                {
                    listener.onContainerCreated( newContainerInfo );
                }
                catch ( Exception e )
                {
                    LOG.warn( ERR_MSG_TEMPLATE, e.getMessage() );
                }
            }
        }

        // 4. check if container has been destroyed
        for ( final ContainerHostInfo oldContainerInfo : oldRhInfo.getContainers() )
        {
            boolean containerStillExists = false;

            for ( final ContainerHostInfo newContainerInfo : newRhInfo.getContainers() )
            {
                if ( oldContainerInfo.getId().equalsIgnoreCase( newContainerInfo.getId() ) )
                {
                    containerStillExists = true;

                    break;
                }
            }

            //check on RH once more to make sure
            if ( !containerStillExists )
            {
                LocalPeer localPeer = ServiceLocator.lookup( LocalPeer.class );
                try
                {
                    ResourceHost resourceHost = localPeer.getResourceHostByContainerId( oldContainerInfo.getId() );

                    try
                    {
                        containerStillExists = resourceHost.lxcExists( oldContainerInfo.getContainerName() );
                    }
                    catch ( ResourceHostException e )
                    {
                        //just in case skip container removal in this round since we can not check
                        containerStillExists = true;
                    }
                }
                catch ( HostNotFoundException ignore )
                {
                    //no-op
                }
            }

            if ( !containerStillExists )
            {
                try
                {
                    listener.onContainerDestroyed( oldContainerInfo );
                }
                catch ( Exception e )
                {
                    LOG.warn( ERR_MSG_TEMPLATE, e.getMessage() );
                }
            }
        }
    }
}
