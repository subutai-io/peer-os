package io.subutai.common.metric;


import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import io.subutai.common.host.HostId;
import io.subutai.common.resource.ResourceType;
import io.subutai.common.resource.ResourceValue;


/**
 * Exceeded quota class
 */
public class ExceededQuota
{
    @JsonProperty( "hostId" )
    protected HostId hostId;
    @JsonProperty( "resourceType" )
    protected ResourceType resourceType;
    @JsonProperty( "currentValue" )
    protected ResourceValue currentValue;
    @JsonProperty( "quotaValue" )
    protected ResourceValue quotaValue;



    public ExceededQuota( @JsonProperty( "hostId" ) final HostId hostId,
                          @JsonProperty( "resourceType" ) final ResourceType resourceType,
                          @JsonProperty( "currentValue" ) final ResourceValue currentValue,
                          @JsonProperty( "quotaValue" ) final ResourceValue quotaValue )
    {
        this.hostId = hostId;
        this.resourceType = resourceType;
        this.currentValue = currentValue;
        this.quotaValue = quotaValue;
    }


    public HostId getHostId()
    {
        return hostId;
    }


    public ResourceType getResourceType()
    {
        return resourceType;
    }


    public ResourceValue getCurrentValue()
    {
        return currentValue;
    }


    public ResourceValue getQuotaValue()
    {
        return quotaValue;
    }


    @JsonIgnore
    public String getDescription()
    {
        return String.format( "%s/%s", currentValue.getPrintValue(), quotaValue.getPrintValue() );
    }


    @Override
    public String toString()
    {
        final StringBuffer sb = new StringBuffer( "ResourceAlert{" );
        sb.append( "hostId=" ).append( hostId );
        sb.append( ", resourceType=" ).append( resourceType );
        sb.append( ", currentValue=" ).append( currentValue );
        sb.append( ", quotaValue=" ).append( quotaValue );
        sb.append( '}' );
        return sb.toString();
    }
}
