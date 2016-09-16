package io.subutai.common.quota;


import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.subutai.common.resource.ByteUnit;
import io.subutai.common.resource.ByteValueResource;
import io.subutai.common.resource.ContainerResourceType;


/**
 * Container RAM resource class
 */
public class ContainerRamResource extends ContainerResource<ByteValueResource>
{
    public ContainerRamResource( final ByteValueResource value )
    {
        super( ContainerResourceType.RAM, value );
    }


    public ContainerRamResource()
    {
        this( new ByteValueResource( BigDecimal.ZERO ) );
    }


    public ContainerRamResource( double value, ByteUnit unit )
    {

        this( new ByteValueResource( ByteValueResource.toBytes( BigDecimal.valueOf( value ), unit ) ) );
    }


    /**
     * Usually used to write value to CLI
     */
    @JsonIgnore
    public String getWriteValue()
    {
        BigDecimal v = resource.convert( ByteUnit.MB );
        return String.format( "%d", v.intValue() );
    }


    /**
     * Usually used to display resource value
     */
    @JsonIgnore
    public String getPrintValue()
    {
        return String.format( "%s%s", resource.convert( ByteUnit.MB ), ByteUnit.MB.getAcronym() );
    }


    @JsonIgnore
    public double doubleValue( ByteUnit unit )
    {
        return getResource().getValue( unit ).doubleValue();
    }
}
