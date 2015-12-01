package io.subutai.core.hostregistry.impl;


import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger LOG = LoggerFactory.getLogger( HeartBeat.class );
    ResourceHostInfoModel response;
    Set<ResourceAlert> alerts;


    public ResourceHostInfo getHostInfo()
    {
        return response;
    }


    public Set<ResourceAlert> getAlerts()
    {
        if ( alerts == null && response != null && response.getAlerts() != null )
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
            try
            {
                ResourceAlert cpuAlert = new ResourceAlert( new HostId( a.getId() ), ResourceType.CPU,
                        new ResourceValue( a.getCpu().getCurrent(), MeasureUnit.PERCENT ),
                        new ResourceValue( a.getCpu().getQuota(), MeasureUnit.PERCENT ) );
                alerts.add( cpuAlert );
            }
            catch ( Exception e )
            {
                LOG.warn( "CPU alert parse error: " + e.getMessage() );
            }
        }
    }


    private void processRamAlert( final ResourceHostInfoModel.Alert a )
    {
        if ( a.getRam() != null )
        {
            try
            {
                ResourceAlert ramAlert = new ResourceAlert( new HostId( a.getId() ), ResourceType.RAM,
                        new ResourceValue( a.getRam().getCurrent(), MeasureUnit.MB ),
                        new ResourceValue( a.getRam().getQuota(), MeasureUnit.MB ) );

                alerts.add( ramAlert );
            }
            catch ( Exception e )
            {
                LOG.warn( "RAM alert parse error: " + e.getMessage() );
            }
        }
    }


    private void processHddAlert( final ResourceHostInfoModel.Alert a )
    {
        if ( a.getRam() != null )
        {
            try
            {
                ResourceAlert cpuAlert = new ResourceAlert( new HostId( a.getId() ), ResourceType.RAM,
                        new ResourceValue( a.getRam().getCurrent(), MeasureUnit.MB ),
                        new ResourceValue( a.getRam().getQuota(), MeasureUnit.MB ) );
                alerts.add( cpuAlert );
            }
            catch ( Exception e )
            {
                LOG.warn( "HDD alert parse error: " + e.getMessage() );
            }
        }
    }
}
