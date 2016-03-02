package io.subutai.common.metric;


import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import io.subutai.common.host.HostId;
import io.subutai.common.resource.ByteValueResource;
import io.subutai.common.resource.ContainerResourceType;
import io.subutai.common.resource.ResourceValue;


/**
 * Exceeded quota class
 */
public class ExceededQuota
{
    @JsonProperty( "hostId" )
    protected final HostId hostId;
    @JsonProperty( "resourceType" )
    protected final ContainerResourceType containerResourceType;
    @JsonProperty( "currentValue" )
    protected final ResourceValue currentValue;
    @JsonProperty( "quotaValue" )
    protected final ResourceValue quotaValue;


    public ExceededQuota( @JsonProperty( "hostId" ) final HostId hostId,
                          @JsonProperty( "resourceType" ) final ContainerResourceType containerResourceType,
                          @JsonProperty( "currentValue" ) final ResourceValue currentValue,
                          @JsonProperty( "quotaValue" ) final ResourceValue quotaValue )
    {
        this.hostId = hostId;
        this.containerResourceType = containerResourceType;
        this.currentValue = currentValue;
        this.quotaValue = quotaValue;
    }


    public HostId getHostId()
    {
        return hostId;
    }


    public ContainerResourceType getContainerResourceType()
    {
        return containerResourceType;
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
        return String.format( "%s/%s", currentValue, quotaValue );
    }


    @Override
    public String toString()
    {
        final StringBuffer sb = new StringBuffer( "ResourceAlert{" );
        sb.append( "hostId=" ).append( hostId );
        sb.append( ", resourceType=" ).append( containerResourceType );
        sb.append( ", currentValue=" ).append( currentValue );
        sb.append( ", quotaValue=" ).append( quotaValue );
        sb.append( '}' );
        return sb.toString();
    }
}
