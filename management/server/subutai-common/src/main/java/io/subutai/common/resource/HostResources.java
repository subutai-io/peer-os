package io.subutai.common.resource;


import java.math.BigDecimal;

import org.codehaus.jackson.annotate.JsonProperty;

import io.subutai.common.quota.ContainerQuota;


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
        diskValue = diskValue.add( containerQuota.getHome().getResource().getValue() );
        diskValue = diskValue.add( containerQuota.getOpt().getResource().getValue() );
        diskValue = diskValue.add( containerQuota.getRootfs().getResource().getValue() );
        diskValue = diskValue.add( containerQuota.getVar().getResource().getValue() );

        return allocate( containerQuota.getCpu().getResource().getValue(),
                containerQuota.getRam().getResource().getValue(), diskValue );
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
}
