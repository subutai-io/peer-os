package io.subutai.common.quota;


import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import io.subutai.common.resource.ContainerResourceType;


public class Quota
{
    @JsonProperty( "resource" )
    private ContainerResource resource;

    @JsonProperty( "threshold" )
    private Integer threshold;


    public Quota( @JsonProperty( "resource" ) final ContainerResource resource,
                  @JsonProperty( "threshold" ) final Integer threshold )
    {
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
    public ContainerRamResource getAsRamResource()
    {
        if ( resource.getContainerResourceType() == ContainerResourceType.RAM )
        {
            return ( ContainerRamResource ) resource;
        }
        throw new IllegalStateException( "Could not get as RAM resource." );
    }


    @JsonIgnore
    public ContainerDiskResource getAsDiskResource()
    {
        if ( resource.getContainerResourceType() == ContainerResourceType.OPT
                || resource.getContainerResourceType() == ContainerResourceType.HOME
                || resource.getContainerResourceType() == ContainerResourceType.ROOTFS
                || resource.getContainerResourceType() == ContainerResourceType.VAR )
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
