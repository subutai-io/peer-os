package io.subutai.bazaar.share.resource;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * Resource of resource host
 */
public abstract class Resource<T extends ResourceValue>
{
    @JsonProperty( "resourceType" )
    protected ResourceType resourceType;
    @JsonProperty( "resourceValue" )
    protected T resourceValue;
    @JsonProperty( "cost" )
    protected Double cost;


    public Resource( @JsonProperty( "resourceValue" ) final T value,
                     @JsonProperty( "resourceType" ) final ResourceType resourceType,
                     @JsonProperty( "cost" ) final Double cost )
    {
        this.resourceValue = value;
        this.resourceType = resourceType;
        this.cost = cost;
    }


    public ResourceType getResourceType()
    {
        return resourceType;
    }


    public T getResourceValue()
    {
        return resourceValue;
    }


    public Double getCost()
    {
        return cost;
    }


    @JsonIgnore
    abstract String getWriteValue();

    @JsonIgnore
    abstract String getPrintValue();
}
