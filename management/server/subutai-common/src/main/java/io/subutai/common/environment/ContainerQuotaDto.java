package io.subutai.common.environment;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.subutai.bazaar.share.quota.ContainerCpuResource;
import io.subutai.bazaar.share.quota.ContainerDiskResource;
import io.subutai.bazaar.share.quota.ContainerQuota;
import io.subutai.bazaar.share.quota.ContainerRamResource;
import io.subutai.bazaar.share.quota.ContainerSize;
import io.subutai.bazaar.share.quota.Quota;
import io.subutai.bazaar.share.resource.ContainerResourceType;


@JsonIgnoreProperties( ignoreUnknown = true )
public class ContainerQuotaDto
{
    @JsonProperty( value = "containerSize" )
    private ContainerSize containerSize;

    @JsonProperty( value = "cpuQuota" )
    private String cpu;

    @JsonProperty( value = "ramQuota" )
    private String ram;

    @JsonProperty( value = "diskQuota" )
    private String disk;


    public ContainerQuotaDto( @JsonProperty( value = "containerSize" ) final ContainerSize containerSize,
                              @JsonProperty( value = "cpuQuota" ) final String cpu,
                              @JsonProperty( value = "ramQuota" ) final String ram,
                              @JsonProperty( value = "diskQuota" ) final String disk )
    {
        this.containerSize = containerSize;
        this.cpu = cpu;
        this.ram = ram;
        this.disk = disk;
    }


    public ContainerQuotaDto( final ContainerQuota quota )
    {
        this.containerSize = quota.getContainerSize();

        if ( quota.get( ContainerResourceType.CPU ) != null )
        {
            this.cpu = quota.get( ContainerResourceType.CPU ).getAsCpuResource().getWriteValue();
        }

        if ( quota.get( ContainerResourceType.RAM ) != null )
        {
            this.ram = quota.get( ContainerResourceType.RAM ).getAsRamResource().getWriteValue();
        }

        if ( quota.get( ContainerResourceType.DISK ) != null )
        {
            this.disk = quota.get( ContainerResourceType.DISK ).getAsDiskResource().getWriteValue();
        }
    }


    public ContainerSize getContainerSize()
    {
        return containerSize;
    }


    public String getCpu()
    {
        return cpu;
    }


    public String getRam()
    {
        return ram;
    }


    public String getDisk()
    {
        return disk;
    }


    @JsonIgnore
    public ContainerQuota getContainerQuota()
    {
        ContainerQuota quota = new ContainerQuota( this.containerSize );
        if ( this.containerSize == ContainerSize.CUSTOM )
        {
            quota.add( new Quota( new ContainerCpuResource( this.getCpu() ), 0 ) );
            quota.add( new Quota( new ContainerRamResource( this.getRam() ), 0 ) );
            quota.add( new Quota( new ContainerDiskResource( this.getDisk() ), 0 ) );
        }
        return quota;
    }
}
