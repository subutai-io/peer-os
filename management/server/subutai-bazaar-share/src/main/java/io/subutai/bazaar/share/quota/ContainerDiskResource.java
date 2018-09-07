package io.subutai.bazaar.share.quota;


import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.subutai.bazaar.share.parser.DiskResourceValueParser;
import io.subutai.bazaar.share.resource.ByteUnit;
import io.subutai.bazaar.share.resource.ByteValueResource;
import io.subutai.bazaar.share.resource.ContainerResourceType;


/**
 * Container HDD resource class
 */
@JsonSerialize( using = ContainerResourceSerializer.class )
@JsonTypeName( "disk" )
public class ContainerDiskResource extends ContainerResource<ByteValueResource>
{
    public ContainerDiskResource( final ByteValueResource value )
    {
        super( ContainerResourceType.DISK, value );
    }


    public ContainerDiskResource( final double value, final ByteUnit unit )
    {
        this( new ByteValueResource( ByteValueResource.toBytes( BigDecimal.valueOf( value ), unit ) ) );
    }


    @JsonCreator
    public ContainerDiskResource( @JsonProperty( "value" ) final String value )
    {
        super( ContainerResourceType.DISK, value );
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


    public long longValue( final ByteUnit unit )
    {
        return getResource().convert( unit ).longValue();
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
