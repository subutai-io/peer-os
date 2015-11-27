package io.subutai.common.metric;


import org.codehaus.jackson.annotate.JsonProperty;

import io.subutai.common.host.HostId;
import io.subutai.common.resource.ResourceType;
import io.subutai.common.resource.ResourceValue;


/**
 * Resource alert class
 */
public class ResourceAlert extends BaseAlert implements ResourceAlertValue
{
    @JsonProperty( "resourceType" )
    protected ResourceType resourceType;
    @JsonProperty( "currentValue" )
    protected ResourceValue currentValue;
    @JsonProperty( "quotaValue" )
    protected ResourceValue quotaValue;


    public ResourceAlert( @JsonProperty( "hostId" ) final HostId hostId,
                          @JsonProperty( "resourceType" ) final ResourceType resourceType,
                          @JsonProperty( "currentValue" ) final ResourceValue currentValue,
                          @JsonProperty( "quotaValue" ) final ResourceValue quotaValue )
    {
        this.hostId = hostId;
        this.resourceType = resourceType;
        this.currentValue = currentValue;
        this.quotaValue = quotaValue;
    }


    @Override
    public HostId getHostId()
    {
        return hostId;
    }


    @Override
    public ResourceType getResourceType()
    {
        return resourceType;
    }


    @Override
    public ResourceValue getCurrentValue()
    {
        return currentValue;
    }


    @Override
    public ResourceValue getQuotaValue()
    {
        return quotaValue;
    }


    @Override
    public String getDescription()
    {
        return String.format( "%s/%s", currentValue.getPrintValue(), quotaValue.getPrintValue() );
    }
}
