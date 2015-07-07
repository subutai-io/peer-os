package io.subutai.core.metric.impl;


import java.util.Set;
import java.util.UUID;

import io.subutai.common.peer.ContainerHost;
import io.subutai.common.util.CollectionUtil;
import io.subutai.core.metric.api.MonitoringSettings;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;


public class MonitoringActivationRequest
{
    private Set<UUID> containerHostsIds;
    private MonitoringSettings monitoringSettings;


    public MonitoringActivationRequest( final Set<ContainerHost> containerHosts,
                                        final MonitoringSettings monitoringSettings )
    {
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( containerHosts ) );
        Preconditions.checkNotNull( monitoringSettings );

        this.containerHostsIds = Sets.newHashSet();
        for ( ContainerHost containerHost : containerHosts )
        {
            containerHostsIds.add( containerHost.getId() );
        }

        this.monitoringSettings = monitoringSettings;
    }


    public Set<UUID> getContainerHostsIds()
    {
        return containerHostsIds;
    }


    public MonitoringSettings getMonitoringSettings()
    {
        return monitoringSettings;
    }
}
