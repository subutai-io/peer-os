package io.subutai.bazaar.share.resource;


import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * Numeric value resource
 */
public class NumericValueResource implements ResourceValue<BigDecimal>
{
    @JsonProperty( "value" )
    protected BigDecimal value;

    @JsonIgnore
    private BigDecimal allocatedValue = BigDecimal.ZERO;


    public NumericValueResource( @JsonProperty( "value" ) final BigDecimal value )
    {
        this.value = value;
    }


    public NumericValueResource( final String value )
    {
        this.value = new BigDecimal( value );
    }


    public NumericValueResource( final int value )
    {
        this.value = new BigDecimal( value );
    }


    public NumericValueResource( final double value )
    {
        this.value = BigDecimal.valueOf( value );
    }


    @Override
    public BigDecimal getValue()
    {
        return value;
    }


    public int intValue()
    {
        return value.intValue();
    }


    public double doubleValue()
    {
        return value.doubleValue();
    }


    @JsonIgnore
    public BigDecimal getAvailableValue()
    {
        return this.value.subtract( this.allocatedValue );
    }


    public boolean allocate( BigDecimal value )
    {
        if ( getAvailableValue().compareTo( value ) >= 0 )
        {
            this.allocatedValue = this.allocatedValue.add( value );
            return true;
        }
        else
        {
            return false;
        }
    }


    public boolean release( BigDecimal value )
    {
        if ( this.allocatedValue.compareTo( value ) >= 0 )
        {
            this.allocatedValue = this.allocatedValue.subtract( value );
            return true;
        }
        else
        {
            return false;
        }
    }


    @Override
    public String toString()
    {
        return "NumericValueResource{" + "value=" + value + ", allocatedValue=" + allocatedValue + '}';
    }
}
