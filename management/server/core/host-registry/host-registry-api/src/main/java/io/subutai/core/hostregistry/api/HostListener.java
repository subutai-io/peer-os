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
public interface HostListener
{
    /**
     * Triggered on each heartbeat from any of connected resource hosts
     *
     * @param resourceHostInfo - resource host info of host from which this heartbeat came
     */
    void onHeartbeat( ResourceHostInfo resourceHostInfo, Set<QuotaAlertValue> alerts );

    void onContainerStateChanged( ContainerHostInfo containerInfo, ContainerHostState previousState,
                                  ContainerHostState currentState );

    void onContainerHostnameChanged( ContainerHostInfo containerInfo, String previousHostname, String currentHostname );

    void onContainerCreated( ContainerHostInfo containerInfo );

    void onContainerNetInterfaceChanged( ContainerHostInfo containerInfo, HostInterfaceModel oldNetInterface,
                                         HostInterfaceModel newNetInterface );

    void onContainerNetInterfaceAdded( ContainerHostInfo containerInfo, HostInterfaceModel netInterface );

    void onContainerNetInterfaceRemoved( ContainerHostInfo containerInfo, HostInterfaceModel netInterface );
}
