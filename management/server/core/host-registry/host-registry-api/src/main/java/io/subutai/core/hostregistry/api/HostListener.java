package io.subutai.core.hostregistry.api;


import java.util.Set;

import io.subutai.common.host.ContainerHostInfo;
import io.subutai.common.host.ContainerHostState;
import io.subutai.common.host.HostInterfaceModel;
import io.subutai.common.host.ResourceHostInfo;
import io.subutai.common.metric.QuotaAlertValue;


/**
 * Notifies listener on host heartbeat
 */
public abstract class HostListener
{
    /**
     * Triggered on each heartbeat from any of connected resource hosts
     *
     * @param resourceHostInfo - resource host info of host from which this heartbeat came
     */
    public void onHeartbeat( ResourceHostInfo resourceHostInfo, Set<QuotaAlertValue> alerts )
    {
    }


    public void onContainerStateChanged( ContainerHostInfo containerInfo, ContainerHostState previousState,
                                  ContainerHostState currentState )
    {
    }


    public void onContainerHostnameChanged( ContainerHostInfo containerInfo, String previousHostname, String currentHostname )
    {
    }


    public void onContainerCreated( ContainerHostInfo containerInfo )
    {
    }


    public void onContainerNetInterfaceChanged( ContainerHostInfo containerInfo, HostInterfaceModel oldNetInterface,
                                         HostInterfaceModel newNetInterface )
    {
    }


    public void onContainerNetInterfaceAdded( ContainerHostInfo containerInfo, HostInterfaceModel netInterface )
    {
    }


    public void onContainerNetInterfaceRemoved( ContainerHostInfo containerInfo, HostInterfaceModel netInterface )
    {
    }


    public void onRhConnected( ResourceHostInfo resourceHostInfo )
    {
    }


    public void onRhDisconnected( ResourceHostInfo resourceHostInfo )
    {
    }
}
