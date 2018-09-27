package io.subutai.common.metric;


import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.subutai.common.host.HostId;
import io.subutai.bazaar.share.quota.ContainerCpuResource;
import io.subutai.bazaar.share.quota.ContainerDiskResource;
import io.subutai.bazaar.share.quota.ContainerRamResource;
import io.subutai.bazaar.share.quota.ContainerResource;
import io.subutai.bazaar.share.resource.ByteValueResource;
import io.subutai.bazaar.share.resource.ContainerResourceType;
import io.subutai.bazaar.share.resource.NumericValueResource;
import io.subutai.bazaar.share.resource.ResourceValue;


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


    public <T extends ContainerResource> T getContainerResourceQuota( final Class<T> format )
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
                case DISK:
                    result = new ContainerDiskResource( ( ByteValueResource ) quotaValue );
                    break;
                default:
                    // no-op
                    break;
            }

            return ( T ) result;
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
                                                          .multiply( BigDecimal.valueOf( 0.01 ) );
        }
        throw new UnsupportedOperationException( "Used value unsupported." );
    }


    @SuppressWarnings( "unchecked" )
    public <T extends ResourceValue> T getCurrentValue( final Class<T> format )
    {
        try
        {
            return ( T ) currentValue;
        }
        catch ( ClassCastException e )
        {
            LOG.warn( e.getMessage() );

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
        return "ResourceAlert{" + "hostId=" + hostId + ", resourceType=" + containerResourceType + ", currentValue="
                + currentValue + ", quotaValue=" + quotaValue + '}';
    }
}
