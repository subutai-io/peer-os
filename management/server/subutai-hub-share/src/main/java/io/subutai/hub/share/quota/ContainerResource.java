package io.subutai.hub.share.quota;


import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import io.subutai.hub.share.resource.ContainerResourceType;
import io.subutai.hub.share.resource.ResourceValue;


@JsonTypeInfo( use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type" )
@JsonSubTypes( {
        @JsonSubTypes.Type( value = ContainerCpuResource.class, name = "cpu" ),
        @JsonSubTypes.Type( value = ContainerRamResource.class, name = "ram" ),
        @JsonSubTypes.Type( value = ContainerDiskResource.class, name = "disk" ),
        @JsonSubTypes.Type( value = ContainerHomeResource.class, name = "home" ),
        @JsonSubTypes.Type( value = ContainerOptResource.class, name = "opt" ),
        @JsonSubTypes.Type( value = ContainerVarResource.class, name = "var" ),
        @JsonSubTypes.Type( value = ContainerRootfsResource.class, name = "rootfs" ),
        @JsonSubTypes.Type( value = ContainerNetResource.class, name = "net" ),
        @JsonSubTypes.Type( value = ContainerNetResource.class, name = "cpuset" ),
} )


public abstract class ContainerResource<T extends ResourceValue>
{
    private ContainerResourceType containerResourceType;
    protected T resource;


    public ContainerResource( final ContainerResourceType containerResourceType, final T resource )
    {
        this.containerResourceType = containerResourceType;
        this.resource = resource;
    }


    public ContainerResource( final ContainerResourceType containerResourceType, final String value )
    {
        this.containerResourceType = containerResourceType;
        this.resource = parse( value );
    }


    public T getResource() {return resource;}


    abstract public String getWriteValue();

    abstract public String getPrintValue();

    abstract protected T parse( String value );


    public ContainerResourceType getContainerResourceType() {return containerResourceType;}


    @Override
    public String toString()
    {
        return "ContainerResource{" + "containerResourceType=" + containerResourceType + ", resource=" + resource + '}';
    }
}
