package io.subutai.common.peer;


import java.util.Collections;
import java.util.Map;

import com.google.common.base.Preconditions;

import io.subutai.common.settings.Common;
import io.subutai.common.util.CollectionUtil;
import io.subutai.common.util.UnitUtil;


public class FitCheckResult
{
    public static final double ACCOMMODATION_OVERHEAD_FACTOR = 1.01;

    private final Map<ResourceHost, ResourceHostCapacity> availableResources;
    private final Map<ResourceHost, ResourceHostCapacity> requestedResources;
    private boolean canFit = true;
    private String description;


    public FitCheckResult( final Map<ResourceHost, ResourceHostCapacity> availableResources,
                           final Map<ResourceHost, ResourceHostCapacity> requestedResources )
    {
        Preconditions.checkArgument( !CollectionUtil.isMapEmpty( availableResources ) );
        Preconditions.checkArgument( !CollectionUtil.isMapEmpty( requestedResources ) );

        this.availableResources = availableResources;
        this.requestedResources = requestedResources;

        parse();
    }


    //returns capacity available on RH
    public Map<ResourceHost, ResourceHostCapacity> getAvailableResources()
    {
        return Collections.unmodifiableMap( availableResources );
    }


    //returns resources requested on RH
    public Map<ResourceHost, ResourceHostCapacity> getRequestedResources()
    {
        return Collections.unmodifiableMap( requestedResources );
    }


    public boolean canFit()
    {
        if ( !Common.CHECK_RH_LIMITS )
        {
            return true;
        }

        return canFit;
    }


    public String getDescription()
    {
        return description;
    }


    private void parse()
    {
        StringBuilder humanResult = new StringBuilder();

        for ( Map.Entry<ResourceHost, ResourceHostCapacity> resourceEntry : requestedResources.entrySet() )
        {
            ResourceHost resourceHost = resourceEntry.getKey();
            ResourceHostCapacity requestedCapacity = resourceEntry.getValue();

            ResourceHostCapacity availableCapacity = availableResources.get( resourceHost );

            if ( requestedCapacity.getRam() * FitCheckResult.ACCOMMODATION_OVERHEAD_FACTOR > availableCapacity
                    .getRam() )
            {
                humanResult.append( String.format(
                        "Requested RAM volume %.2fMB can not be provided on RH %s: available RAM volume is %"
                                + ".2fMB%n",
                        UnitUtil.convert( requestedCapacity.getRam(), UnitUtil.Unit.B, UnitUtil.Unit.MB ),
                        resourceHost.getHostname(),
                        UnitUtil.convert( availableCapacity.getRam(), UnitUtil.Unit.B, UnitUtil.Unit.MB ) ) );

                canFit = false;
            }

            if ( requestedCapacity.getDisk() * FitCheckResult.ACCOMMODATION_OVERHEAD_FACTOR > availableCapacity
                    .getDisk() )
            {
                humanResult.append( String.format(
                        "Requested DISK volume %.2fGB can not be provided on RH %s: available DISK volume is "
                                + "%.2fGB%n",
                        UnitUtil.convert( requestedCapacity.getDisk(), UnitUtil.Unit.B, UnitUtil.Unit.GB ),
                        resourceHost.getHostname(),
                        UnitUtil.convert( availableCapacity.getDisk(), UnitUtil.Unit.B, UnitUtil.Unit.GB ) ) );

                canFit = false;
            }

            if ( requestedCapacity.getCpu() * FitCheckResult.ACCOMMODATION_OVERHEAD_FACTOR > availableCapacity
                    .getCpu() )
            {
                humanResult.append(
                        String.format( "Requested CPU %.2f can not be provided on RH %s: available CPU is %.2f%n",
                                requestedCapacity.getCpu(), resourceHost.getHostname(), availableCapacity.getCpu() ) );

                canFit = false;
            }
        }


        description = humanResult.length() > 0 ? humanResult.toString() : "The requested resources can be allocated";
    }
}
