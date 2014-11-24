package org.safehaus.subutai.core.metric.impl;


import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.util.CollectionUtil;
import org.safehaus.subutai.core.metric.api.MonitoringSettings;
import org.safehaus.subutai.core.peer.api.ContainerHost;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;


public class MonitoringActivationRequest
{
    private Set<UUID> containerHostsIds;
    private MonitoringSettingsImpl monitoringSettings;


    public MonitoringActivationRequest( final Set<ContainerHost> containerHosts,
                                        final MonitoringSettings monitoringSettings )
    {
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( containerHosts ) );

        this.containerHostsIds = Sets.newHashSet();
        for ( ContainerHost containerHost : containerHosts )
        {
            containerHostsIds.add( containerHost.getId() );
        }

        if ( monitoringSettings != null )
        {
            this.monitoringSettings = new MonitoringSettingsImpl( monitoringSettings );
        }
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
