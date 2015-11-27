package io.subutai.core.hostregistry.impl;


import java.util.HashSet;
import java.util.Set;

import io.subutai.common.host.HostId;
import io.subutai.common.host.ResourceHostInfo;
import io.subutai.common.metric.ResourceAlert;
import io.subutai.common.resource.MeasureUnit;
import io.subutai.common.resource.ResourceType;
import io.subutai.common.resource.ResourceValue;


/**
 * Heartbeat response from resource host
 */
public class HeartBeat
{
    ResourceHostInfoImpl response;
    Set<ResourceAlert> alerts;


    public ResourceHostInfo getHostInfo()
    {
        return response;
    }


    public Set<ResourceAlert> getAlerts()
    {
        if ( alerts == null && response.getAlerts() != null )
        {
            alerts = new HashSet<>();
            for ( ResourceHostInfoImpl.Alert a : response.getAlerts() )
            {
                processCpuAlert( a );
                processRamAlert( a );
                processHddAlert( a );
            }
        }

        return alerts;
    }


    private void processCpuAlert( final ResourceHostInfoImpl.Alert a )
    {
        if ( a.cpu != null )
        {
            ResourceAlert cpuAlert = new ResourceAlert( new HostId( a.id ), ResourceType.CPU,
                    new ResourceValue( a.cpu.current, MeasureUnit.PERCENT ),
                    new ResourceValue( a.cpu.quota, MeasureUnit.PERCENT ) );
            alerts.add( cpuAlert );
        }
    }


    private void processRamAlert( final ResourceHostInfoImpl.Alert a )
    {
        if ( a.ram != null )
        {
            ResourceAlert cpuAlert = new ResourceAlert( new HostId( a.id ), ResourceType.RAM,
                    new ResourceValue( a.ram.current, MeasureUnit.MB ),
                    new ResourceValue( a.ram.quota, MeasureUnit.MB ) );
            alerts.add( cpuAlert );
        }
    }


    private void processHddAlert( final ResourceHostInfoImpl.Alert a )
    {
        if ( a.ram != null )
        {
            ResourceAlert cpuAlert = new ResourceAlert( new HostId( a.id ), ResourceType.RAM,
                    new ResourceValue( a.ram.current, MeasureUnit.MB ),
                    new ResourceValue( a.ram.quota, MeasureUnit.MB ) );
            alerts.add( cpuAlert );
        }
    }
}
