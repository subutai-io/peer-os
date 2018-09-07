package io.subutai.bazaar.share.resource;


import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * Resource value class
 */
public class ByteValueResource extends NumericValueResource
{
    public ByteValueResource( final String value )
    {
        super( value );
    }


    public ByteValueResource( @JsonProperty( "value" ) final BigDecimal value )
    {
        super( value );
    }


    public ByteValueResource( final String value, final ByteUnit byteUnit )
    {
        super( toBytes( value, byteUnit ) );
    }


    public static BigDecimal toBytes( final BigDecimal value, final ByteUnit unit )
    {
        if ( unit.equals( ByteUnit.BYTE ) )
        {
            return value;
        }
        else
        {
            return unit.getMultiplier().multiply( value );
        }
    }


    public static BigDecimal toBytes( final String value, final ByteUnit unit )
    {
        final BigDecimal d = new BigDecimal( value );
        if ( unit.equals( ByteUnit.BYTE ) )
        {
            return d;
        }
        else
        {
            return unit.getMultiplier().multiply( d );
        }
    }


    public BigDecimal convert( final ByteUnit unit )
    {
        if ( unit.equals( ByteUnit.BYTE ) )
        {
            return this.getValue();
        }
        else
        {
            return this.getValue().divide( unit.getMultiplier(), BigDecimal.ROUND_UP );
        }
    }


    public BigDecimal getValue( final ByteUnit unit )
    {
        return convert( unit );
    }


    @Override
    public String toString()
    {
        return String.format( "%s byte(s)", getValue() );
    }
}
