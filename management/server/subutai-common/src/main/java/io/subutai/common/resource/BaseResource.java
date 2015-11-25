package io.subutai.common.resource;


import java.math.BigDecimal;

import org.codehaus.jackson.annotate.JsonProperty;


/**
 * Base abstract resource class
 */
public class BaseResource implements Resource
{
    @JsonProperty( "resourceType" )
    protected ResourceType resourceType;
    @JsonProperty( "measureUnit" )
    protected MeasureUnit measureUnit;
    @JsonProperty( "currentValue" )
    protected ResourceValue currentValue;
    @JsonProperty( "quotaValue" )
    protected ResourceValue quotaValue;
    @JsonProperty( "maxValue" )
    protected ResourceValue maxValue;


    public BaseResource( @JsonProperty( "resourceType" ) final ResourceType resourceType,
                         @JsonProperty( "measureUnit" ) final MeasureUnit measureUnit,
                         @JsonProperty( "currentValue" ) final ResourceValue currentValue,
                         @JsonProperty( "quotaValue" ) final ResourceValue quotaValue,
                         @JsonProperty( "maxValue" ) final ResourceValue maxValue )
    {
        this.resourceType = resourceType;
        this.measureUnit = measureUnit;
        this.currentValue = currentValue;
        this.quotaValue = quotaValue;
        this.maxValue = maxValue;
    }


    @Override
    public ResourceType getResourceType()
    {
        return resourceType;
    }


    @Override
    public MeasureUnit getMeasureUnit()
    {
        return measureUnit;
    }


    @Override
    public ResourceValue getCurrentValue( final MeasureUnit unit )
    {
        return currentValue.convert( unit );
    }


    @Override
    public ResourceValue getQuotaValue( final MeasureUnit unit )
    {
        return quotaValue.convert( unit );
    }


    @Override
    public ResourceValue getMaxValue( final MeasureUnit unit )
    {
        return maxValue.convert( unit );
    }
}
