package io.subutai.bazaar.share.quota;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;

import io.subutai.bazaar.share.resource.ContainerResourceType;


public class Quota
{
    @JsonProperty( "resource" )
    private ContainerResource resource;

    @JsonProperty( "threshold" )
    private Integer threshold;


    public Quota( @JsonProperty( "resource" ) final ContainerResource resource,
                  @JsonProperty( "threshold" ) final Integer threshold )
    {
        Preconditions.checkNotNull( resource );
        if ( threshold != null )
        {
            Preconditions.checkArgument( threshold >= 0 );
            Preconditions.checkArgument( threshold <= 100 );
        }

        this.resource = resource;
        this.threshold = threshold;
    }


    public ContainerResource getResource()
    {
        return resource;
    }


    public Integer getThreshold()
    {
        return threshold;
    }


    @JsonIgnore
    public ContainerCpuResource getAsCpuResource()
    {
        if ( resource.getContainerResourceType() == ContainerResourceType.CPU )
        {
            return ( ContainerCpuResource ) resource;
        }
        throw new IllegalStateException( "Could not get as CPU resource." );
    }


    @JsonIgnore
    public ContainerCpuSetResource getAsCpuSetResource()
    {
        if ( resource.getContainerResourceType() == ContainerResourceType.CPUSET )
        {
            return ( ContainerCpuSetResource ) resource;
        }
        throw new IllegalStateException( "Could not get as CPU resource." );
    }


    @JsonIgnore
    public ContainerRamResource getAsRamResource()
    {
        if ( resource.getContainerResourceType() == ContainerResourceType.RAM )
        {
            return ( ContainerRamResource ) resource;
        }
        throw new IllegalStateException( "Could not get as RAM resource." );
    }


    @JsonIgnore
    public ContainerNetResource getAsNetResource()
    {
        if ( resource.getContainerResourceType() == ContainerResourceType.NET )
        {
            return ( ContainerNetResource ) resource;
        }
        throw new IllegalStateException( "Could not get as NET resource." );
    }


    @JsonIgnore
    public ContainerDiskResource getAsDiskResource()
    {
        if ( resource.getContainerResourceType() == ContainerResourceType.DISK )
        {
            return ( ContainerDiskResource ) resource;
        }
        throw new IllegalStateException( "Could not get as disk resource." );
    }


    @Override
    public String toString()
    {
        return "Quota{" + "resource=" + resource + ", threshold=" + threshold + '}';
    }
}
