package io.subutai.core.metric.impl;


import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import io.subutai.common.peer.ContainerHost;
import io.subutai.common.util.CollectionUtil;
import io.subutai.core.metric.api.MonitoringSettings;


public class MonitoringActivationRequest
{
    private Set<String> containerHostsIds;
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


    public Set<String> getContainerHostsIds()
    {
        return containerHostsIds;
    }


    public MonitoringSettings getMonitoringSettings()
    {
        return monitoringSettings;
    }
}
