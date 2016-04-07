package io.subutai.common.quota;


import org.codehaus.jackson.annotate.JsonIgnore;

import io.subutai.common.resource.ContainerResourceType;
import io.subutai.common.resource.ResourceValue;


public abstract class ContainerResource<T extends ResourceValue>
{
    private ContainerResourceType containerResourceType;
    protected T resource;

    public ContainerResource( final ContainerResourceType containerResourceType, final T resource )
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
