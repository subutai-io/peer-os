package io.subutai.hub.share.quota;


import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.subutai.hub.share.resource.ByteUnit;
import io.subutai.hub.share.resource.ByteValueResource;
import io.subutai.hub.share.resource.ContainerResourceType;


/**
 * Container HDD resource class
 */
public class ContainerDiskResource extends ContainerResource<ByteValueResource>
{
    public ContainerDiskResource( final ByteValueResource value )
    {
        super( ContainerResourceType.ROOTFS, value );
    }


    public ContainerDiskResource( final ContainerResourceType type, final ByteValueResource value )
    {
        super( type, value );
    }


    /**
     * Usually used to write value to CLI
     */
    @JsonIgnore
    @Override
    public String getWriteValue()
    {
        BigDecimal v = resource.convert( ByteUnit.GB );
        return String.format( "%d", v.intValue() );
    }


    /**
     * Usually used to display resource value
     */
    @JsonIgnore
    @Override
    public String getPrintValue()
    {
        return String.format( "%s%s", resource.convert( ByteUnit.GB ), ByteUnit.GB.getAcronym() );
    }


    @JsonIgnore
    public double doubleValue( ByteUnit unit )
    {
        return getResource().getValue( unit ).doubleValue();
    }
}
