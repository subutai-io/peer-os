package io.subutai.common.resource;


import java.math.BigDecimal;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;


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


    @Override
    public BigDecimal getValue()
    {
        return value;
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
}
