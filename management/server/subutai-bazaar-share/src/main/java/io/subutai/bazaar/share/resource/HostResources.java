package io.subutai.bazaar.share.resource;


import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.subutai.bazaar.share.quota.ContainerQuota;


/**
 * Host resources
 */
public class HostResources
{
    @JsonProperty( "hostId" )
    private String hostId;
    @JsonProperty( "cpuLimit" )
    private CpuResource cpuLimit;
    @JsonProperty( "ramLimit" )
    private RamResource ramLimit;
    @JsonProperty( "diskLimit" )
    private DiskResource diskLimit;
    @JsonIgnore
    private boolean enabled = false;

    public HostResources( @JsonProperty( "hostId" ) final String hostId,
                          @JsonProperty( "cpuLimit" ) final CpuResource cpuLimit,
                          @JsonProperty( "ramLimit" ) final RamResource ramLimit,
                          @JsonProperty( "diskLimit" ) final DiskResource diskLimit )
    {
        this.hostId = hostId;
        this.cpuLimit = cpuLimit;
        this.ramLimit = ramLimit;
        this.diskLimit = diskLimit;
    }


    public String getHostId()
    {
        return hostId;
    }


    public CpuResource getCpuLimit()
    {
        return cpuLimit;
    }


    public RamResource getRamLimit()
    {
        return ramLimit;
    }


    public DiskResource getDiskLimit()
    {
        return diskLimit;
    }


    public boolean allocate( ContainerQuota containerQuota )
    {
        BigDecimal diskValue = BigDecimal.ZERO;
        diskValue = diskValue
                .add( containerQuota.get( ContainerResourceType.DISK ).getAsDiskResource().getResource().getValue() );

        return allocate( containerQuota.get( ContainerResourceType.CPU ).getAsCpuResource().getResource().getValue(),
                containerQuota.get( ContainerResourceType.RAM ).getAsRamResource().getResource().getValue(),
                diskValue );
    }


    private boolean allocate( BigDecimal cpu, BigDecimal ram, BigDecimal disk )
    {
        boolean allocated = this.cpuLimit.getResourceValue().allocate( cpu );
        if ( !allocated )
        {
            return false;
        }

        allocated = this.ramLimit.getResourceValue().allocate( ram );
        if ( !allocated )
        {
            this.cpuLimit.getResourceValue().release( cpu );
            return false;
        }
        allocated = this.diskLimit.getResourceValue().allocate( disk );
        if ( !allocated )
        {
            this.cpuLimit.getResourceValue().release( cpu );
            this.ramLimit.getResourceValue().release( ram );
            return false;
        }

        return true;
    }

    public boolean isEnabled()
     {
         return enabled;
     }


     public void enable()
     {
         this.enabled = true;
     }
}
