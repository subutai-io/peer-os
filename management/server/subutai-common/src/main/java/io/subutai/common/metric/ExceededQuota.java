package io.subutai.common.metric;


import java.math.BigDecimal;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.host.HostId;
import io.subutai.common.quota.ContainerCpuResource;
import io.subutai.common.quota.ContainerHomeResource;
import io.subutai.common.quota.ContainerOptResource;
import io.subutai.common.quota.ContainerRamResource;
import io.subutai.common.quota.ContainerResource;
import io.subutai.common.quota.ContainerRootfsResource;
import io.subutai.common.quota.ContainerVarResource;
import io.subutai.common.resource.ByteValueResource;
import io.subutai.common.resource.ContainerResourceType;
import io.subutai.common.resource.NumericValueResource;
import io.subutai.common.resource.ResourceValue;


/**
 * Exceeded quota class
 */
public class ExceededQuota
{
    private static final Logger LOG = LoggerFactory.getLogger( ExceededQuota.class );

    @JsonProperty( "hostId" )
    protected final HostId hostId;
    @JsonProperty( "resourceType" )
    protected final ContainerResourceType containerResourceType;
    @JsonProperty( "currentValue" )
    protected final NumericValueResource currentValue;
    @JsonProperty( "quotaValue" )
    protected final ResourceValue quotaValue;
    @JsonProperty( "rhMetrics" )
    protected ResourceHostMetric resourceHostMetric;


    public ExceededQuota( @JsonProperty( "hostId" ) final HostId hostId,
                          @JsonProperty( "resourceType" ) final ContainerResourceType containerResourceType,
                          @JsonProperty( "currentValue" ) final NumericValueResource currentValue,
                          @JsonProperty( "quotaValue" ) final ResourceValue quotaValue,
                          @JsonProperty( "rhMetrics" ) final ResourceHostMetric resourceHostMetric )
    {
        this.hostId = hostId;
        this.containerResourceType = containerResourceType;
        this.currentValue = currentValue;
        this.quotaValue = quotaValue;
        this.resourceHostMetric = resourceHostMetric;
    }


    public HostId getHostId()
    {
        return hostId;
    }


    public ResourceHostMetric getResourceHostMetric()
    {
        return resourceHostMetric;
    }


    public void setResourceHostMetric( final ResourceHostMetric resourceHostMetric )
    {
        this.resourceHostMetric = resourceHostMetric;
    }


    public ContainerResourceType getContainerResourceType()
    {
        return containerResourceType;
    }


    public <T extends ContainerResource> T getContainerResource( final Class<T> format )
    {
        ContainerResource result = null;
        try
        {
            switch ( containerResourceType )
            {
                case CPU:
                    result = new ContainerCpuResource( ( NumericValueResource ) quotaValue );
                    break;
                case RAM:
                    result = new ContainerRamResource( ( ByteValueResource ) quotaValue );
                    break;
                case ROOTFS:
                    result = new ContainerRootfsResource( ( ByteValueResource ) quotaValue );
                    break;
                case HOME:
                    result = new ContainerHomeResource( ( ByteValueResource ) quotaValue );
                    break;
                case OPT:
                    result = new ContainerOptResource( ( ByteValueResource ) quotaValue );
                    break;
                case VAR:
                    result = new ContainerVarResource( ( ByteValueResource ) quotaValue );
                    break;
            }

            if ( result != null )
            {
                return ( T ) result;
            }
        }
        catch ( Exception e )
        {
            LOG.warn( e.getMessage() );
        }

        return null;
    }


    public double getPercentage()
    {
        return currentValue.getValue().doubleValue();
    }


    public BigDecimal getUsedValue()
    {
        if ( quotaValue instanceof NumericValueResource )
        {
            return ( ( NumericValueResource ) quotaValue ).getValue().multiply( currentValue.getValue() )
                                                          .multiply( new BigDecimal( 0.01 ) );
        }
        throw new UnsupportedOperationException( "Used value unsupported." );
    }


    @SuppressWarnings( "unchecked" )
    public <T> T getQuotaValue( final Class<T> format )
    {
        try
        {
            return ( T ) quotaValue;
        }
        catch ( ClassCastException cce )
        {
            return null;
        }
    }


    @SuppressWarnings( "unchecked" )
    public <T> T getCurrentValue( final Class<T> format )
    {
        try
        {
            return ( T ) currentValue;
        }
        catch ( ClassCastException cce )
        {
            return null;
        }
    }


    @Deprecated
    public ResourceValue getQuotaValue()
    {
        return quotaValue;
    }


    @Deprecated
    public ResourceValue getCurrentValue()
    {
        return currentValue;
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
