package io.subutai.common.resource;


import java.math.BigDecimal;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;


/**
 * Resource value class
 */
public class ResourceValue
{
    @JsonProperty( "value" )
    private BigDecimal value;
    @JsonProperty( "measureUnit" )
    private MeasureUnit measureUnit;


    public ResourceValue( @JsonProperty( "value" ) final BigDecimal value,
                          @JsonProperty( "measureUnit" ) final MeasureUnit measureUnit )
    {
        this.value = value;
        this.measureUnit = measureUnit;
    }


    public ResourceValue( final String value, final MeasureUnit measureUnit )
    {
        this.value = new BigDecimal( value );
        this.measureUnit = measureUnit;
    }


    public MeasureUnit getMeasureUnit()
    {
        return measureUnit;
    }


    protected ResourceValue convert( final MeasureUnit unit )
    {
        if ( unit.equals( measureUnit ) )
        {
            return this;
        }
        else
        {
            BigDecimal inBytes = measureUnit.getMultiplicator().multiply( value );
            return new ResourceValue( inBytes.divide( unit.getMultiplicator(), BigDecimal.ROUND_UP ), unit );
        }
    }


    public BigDecimal getValue( final MeasureUnit unit )
    {
        return convert( unit ).value;
    }


    @Override
    public String toString()
    {
        return String.format( "%s%s", value != null ? value.toString() : "UNDEFINED",
                measureUnit != null ? measureUnit.getAcronym() : "UNDEFINED" );
    }


    /**
     * Usually used to write value to CLI
     */
    @JsonIgnore
    public String getWriteValue( MeasureUnit measureUnit )
    {
        if ( measureUnit == null || value == null )
        {
            throw new IllegalStateException( "Measure unit or resource value is null." );
        }
        if ( measureUnit == MeasureUnit.UNLIMITED )
        {
            throw new IllegalStateException( "Could not write UNLIMITED value." );
        }

        ResourceValue v = convert( measureUnit );
        return String.format( "%d", value.intValue() );
    }


    /**
     * Usually used to display resource value
     */
    @JsonIgnore
    public String getPrintValue()
    {
        if ( measureUnit == null || value == null )
        {
            throw new IllegalStateException( "Measure unit or resource value is null." );
        }
        if ( measureUnit == MeasureUnit.PERCENT )
        {
            return value.toString();
        }
        return String.format( "%s%s", value, measureUnit.getAcronym() );
    }
}
