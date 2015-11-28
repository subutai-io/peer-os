package io.subutai.core.hostregistry.impl;


import java.util.HashSet;
import java.util.Set;

import io.subutai.common.host.HostId;
import io.subutai.common.host.ResourceHostInfo;
import io.subutai.common.host.ResourceHostInfoModel;
import io.subutai.common.metric.ResourceAlert;
import io.subutai.common.resource.MeasureUnit;
import io.subutai.common.resource.ResourceType;
import io.subutai.common.resource.ResourceValue;


/**
 * Heartbeat response from resource host
 */
public class HeartBeat
{
    ResourceHostInfoModel response;
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
            for ( ResourceHostInfoModel.Alert a : response.getAlerts() )
            {
                processCpuAlert( a );
                processRamAlert( a );
                processHddAlert( a );
            }
        }

        return alerts;
    }


    private void processCpuAlert( final ResourceHostInfoModel.Alert a )
    {
        if ( a.getCpu() != null )
        {
            ResourceAlert cpuAlert = new ResourceAlert( new HostId( a.getId() ), ResourceType.CPU,
                    new ResourceValue( a.getCpu().getCurrent(), MeasureUnit.PERCENT ),
                    new ResourceValue( a.getCpu().getQuota(), MeasureUnit.PERCENT ) );
            alerts.add( cpuAlert );
        }
    }


    private void processRamAlert( final ResourceHostInfoModel.Alert a )
    {
        if ( a.getRam() != null )
        {
            ResourceAlert cpuAlert = new ResourceAlert( new HostId( a.getId() ), ResourceType.RAM,
                    new ResourceValue( a.getRam().getCurrent(), MeasureUnit.MB ),
                    new ResourceValue( a.getRam().getQuota(), MeasureUnit.MB ) );
            alerts.add( cpuAlert );
        }
    }


    private void processHddAlert( final ResourceHostInfoModel.Alert a )
    {
        if ( a.getRam() != null )
        {
            ResourceAlert cpuAlert = new ResourceAlert( new HostId( a.getId()), ResourceType.RAM,
                    new ResourceValue( a.getRam().getCurrent(), MeasureUnit.MB ),
                    new ResourceValue( a.getRam().getQuota(), MeasureUnit.MB ) );
            alerts.add( cpuAlert );
        }
    }
}
