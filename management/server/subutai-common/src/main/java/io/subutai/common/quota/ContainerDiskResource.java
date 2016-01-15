package io.subutai.common.quota;


import java.math.BigDecimal;

import org.codehaus.jackson.annotate.JsonIgnore;

import io.subutai.common.resource.ByteUnit;
import io.subutai.common.resource.ByteValueResource;
import io.subutai.common.resource.ContainerResourceType;


/**
 * Container HDD resource class
 */
public abstract class ContainerDiskResource extends ContainerResource<ByteValueResource>
{
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
        BigDecimal v = resource.convert( ByteUnit.MB );
        return String.format( "%d", v.intValue() );
    }


    /**
     * Usually used to display resource value
     */
    @JsonIgnore
    @Override
    public String getPrintValue()
    {
        return String.format( "%s%s", resource.convert( ByteUnit.MB ), ByteUnit.MB.getAcronym() );
    }
}
