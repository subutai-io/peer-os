package io.subutai.hub.share.quota;


import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.subutai.hub.share.parser.DiskResourceValueParser;
import io.subutai.hub.share.resource.ByteUnit;
import io.subutai.hub.share.resource.ByteValueResource;
import io.subutai.hub.share.resource.ContainerResourceType;


/**
 * Container HDD resource class
 */
@JsonSerialize( using = ContainerResourceSerializer.class )
@JsonTypeName( "disk" )
public class ContainerDiskResource extends ContainerResource<ByteValueResource>
{
    public ContainerDiskResource( final ContainerResourceType type, final ByteValueResource value )
    {
        super( type, value );
    }


    public ContainerDiskResource( final ContainerResourceType containerResourceType, final String value )
    {
        super( containerResourceType, value );
    }


    /**
     * Usually used to write value to CLI
     */
    @Override
    public String getWriteValue()
    {
        BigDecimal v = resource.convert( ByteUnit.GB );
        return String.format( "%d", v.intValue() );
    }


    /**
     * Usually used to display resource value
     */
    @Override
    public String getPrintValue()
    {
        return String.format( "%s%s", resource.convert( ByteUnit.GB ), ByteUnit.GB.getAcronym() );
    }


    public double doubleValue( ByteUnit unit )
    {
        return getResource().getValue( unit ).doubleValue();
    }


    @Override
    protected ByteValueResource parse( final String value )
    {
        return DiskResourceValueParser.getInstance().parse( value );
    }
}
