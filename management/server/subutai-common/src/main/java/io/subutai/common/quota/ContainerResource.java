package io.subutai.common.quota;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import io.subutai.common.resource.ContainerResourceType;
import io.subutai.common.resource.ResourceValue;


@JsonTypeInfo( use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type" )
@JsonSubTypes( {
        @JsonSubTypes.Type( value = ContainerCpuResource.class, name = "CpuResource" ),
        @JsonSubTypes.Type( value = ContainerRamResource.class, name = "RamResource" ),
        @JsonSubTypes.Type( value = ContainerDiskResource.class, name = "DiskResource" ),
        @JsonSubTypes.Type( value = ContainerHomeResource.class, name = "HomeResource" ),
        @JsonSubTypes.Type( value = ContainerOptResource.class, name = "OptResource" ),
        @JsonSubTypes.Type( value = ContainerVarResource.class, name = "VarResource" ),
        @JsonSubTypes.Type( value = ContainerRootfsResource.class, name = "RootfsResource" ),
} )
public abstract class ContainerResource<T extends ResourceValue>
{
    @JsonProperty( value = "resourceType" )
    private ContainerResourceType containerResourceType;
    @JsonProperty( value = "resourceValue" )
    protected T resource;


    public ContainerResource( @JsonProperty( value = "resourceType" ) final ContainerResourceType containerResourceType,
                              @JsonProperty( value = "resourceValue" ) final T resource )
    {
        this.containerResourceType = containerResourceType;
        this.resource = resource;
    }


    public T getResource() {return resource;}


    @JsonIgnore
    abstract public String getWriteValue();

    @JsonIgnore
    abstract public String getPrintValue();


    public ContainerResourceType getContainerResourceType() {return containerResourceType;}


    @Override
    public String toString()
    {
        return "ContainerResource{" + "containerResourceType=" + containerResourceType + ", resource=" + resource + '}';
    }
}
