package io.subutai.bazaar.share.resource;


import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * RAM resource class
 */
public class RamResource extends Resource<ByteValueResource>
{
    public RamResource( @JsonProperty( "value" ) final BigDecimal value, @JsonProperty( "cost" ) final Double cost )
    {
        super( new ByteValueResource( value ), ResourceType.RAM, cost );
    }


    @Override
    public String getWriteValue()
    {
        return String.format( "%fMB", resourceValue.convert( ByteUnit.MB ) );
    }


    @Override
    public String getPrintValue()
    {
        return String.format( "%fMB", resourceValue.convert( ByteUnit.MB ) );
    }
}
