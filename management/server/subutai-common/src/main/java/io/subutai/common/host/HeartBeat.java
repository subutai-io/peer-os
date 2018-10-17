package io.subutai.common.host;


import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.metric.ExceededQuota;
import io.subutai.common.metric.QuotaAlertValue;
import io.subutai.bazaar.share.resource.ByteUnit;
import io.subutai.bazaar.share.resource.ByteValueResource;
import io.subutai.bazaar.share.resource.ContainerResourceType;
import io.subutai.bazaar.share.resource.NumericValueResource;


/**
 * Heartbeat response from resource host
 */
public class HeartBeat
{
    private static final Logger LOG = LoggerFactory.getLogger( HeartBeat.class );
    ResourceHostInfoModel response;
    Set<QuotaAlertValue> alerts;


    public HeartBeat( final ResourceHostInfoModel response )
    {
        this.response = response;
    }


    public ResourceHostInfo getHostInfo()
    {
        return response;
    }


    public Set<QuotaAlertValue> getAlerts()
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

                QuotaAlertValue cpuAlert = new QuotaAlertValue(
                        new ExceededQuota( new HostId( a.getId() ), ContainerResourceType.CPU,
                                new NumericValueResource( a.getCpu().getCurrent() ),
                                new NumericValueResource( a.getCpu().getQuota() ), null ) );
                alerts.add( cpuAlert );
            }
            catch ( Exception e )
            {
                LOG.warn( "CPU alert parse error: {}. {}", e.getMessage(), a.getCpu() );
            }
        }
    }


    private void processRamAlert( final ResourceHostInfoModel.Alert a )
    {
        if ( a.getRam() != null )
        {
            try
            {
                QuotaAlertValue ramAlert = new QuotaAlertValue(
                        new ExceededQuota( new HostId( a.getId() ), ContainerResourceType.RAM,
                                new NumericValueResource( a.getRam().getCurrent() ),
                                new ByteValueResource( a.getRam().getQuota(), ByteUnit.MB ), null ) );

                alerts.add( ramAlert );
            }
            catch ( Exception e )
            {
                LOG.warn( "RAM alert parse error: {}. {}", e.getMessage(), a.getRam() );
            }
        }
    }


    private void processHddAlert( final ResourceHostInfoModel.Alert a )
    {
        if ( a.getHdd() != null )
        {

            for ( ResourceHostInfoModel.Hdd hdd : a.getHdd() )
            {
                try
                {
                    QuotaAlertValue hddAlert = new QuotaAlertValue( new ExceededQuota( new HostId( a.getId() ),
                            ContainerResourceType.parse( hdd.getPartition() ),
                            new NumericValueResource( hdd.getCurrent() ),
                            new ByteValueResource( hdd.getQuota(), ByteUnit.GB ), null ) );
                    alerts.add( hddAlert );
                }
                catch ( Exception e )
                {
                    LOG.warn( "HDD alert parse error: {}. {}", e.getMessage(), hdd );
                }
            }
        }
    }
}
