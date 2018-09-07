package io.subutai.bazaar.share.quota;


import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.subutai.bazaar.share.parser.RamResourceValueParser;
import io.subutai.bazaar.share.resource.ByteUnit;
import io.subutai.bazaar.share.resource.ByteValueResource;
import io.subutai.bazaar.share.resource.ContainerResourceType;


/**
 * Container RAM resource class
 */
@JsonSerialize( using = ContainerResourceSerializer.class )
@JsonTypeName( "ram" )
public class ContainerRamResource extends ContainerResource<ByteValueResource>
{

    public ContainerRamResource( final ByteValueResource value )
    {
        super( ContainerResourceType.RAM, value );
    }


    @JsonCreator
    public ContainerRamResource( @JsonProperty( "value" ) final String value )
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
    @Override
    public String getWriteValue()
    {
        BigDecimal v = resource.convert( ByteUnit.MB );
        return String.format( "%d", v.intValue() );
    }


    /**
     * Usually used to display resource value
     */
    @Override
    public String getPrintValue()
    {
        return String.format( "%s%s", resource.convert( ByteUnit.MB ), ByteUnit.MB.getAcronym() );
    }


    public double doubleValue( ByteUnit unit )
    {
        return getResource().getValue( unit ).doubleValue();
    }


    @Override
    protected ByteValueResource parse( final String value )
    {
        return RamResourceValueParser.getInstance().parse( value );
    }
}
