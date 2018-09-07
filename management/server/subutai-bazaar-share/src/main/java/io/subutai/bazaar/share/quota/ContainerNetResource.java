package io.subutai.bazaar.share.quota;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.subutai.bazaar.share.parser.NetResourceValueParser;
import io.subutai.bazaar.share.resource.ContainerResourceType;
import io.subutai.bazaar.share.resource.NumericValueResource;


/**
 * Container network resource class
 */
@JsonSerialize( using = ContainerResourceSerializer.class )
@JsonTypeName( "net" )
public class ContainerNetResource extends ContainerResource<NumericValueResource>
{

    public ContainerNetResource( final NumericValueResource resourceValue )
    {
        super( ContainerResourceType.NET, resourceValue );
    }


    public ContainerNetResource( final int netValue )
    {
        super( ContainerResourceType.NET, new NumericValueResource( netValue ) );
    }


    @JsonCreator
    public ContainerNetResource( @JsonProperty( "value" ) final String value )
    {
        super( ContainerResourceType.NET, value );
    }


    public double doubleValue()
    {
        return resource.getValue().doubleValue();
    }


    /**
     * Usually used to write value to CLI
     */
    @Override
    public String getWriteValue()
    {
        return String.format( "%f", resource.getValue().doubleValue() );
    }


    /**
     * Usually used to display resource value
     */
    @Override
    public String getPrintValue()
    {
        return String.format( "%fKbps", resource.getValue().doubleValue() );
    }


    @Override
    protected NumericValueResource parse( final String value )
    {
        return NetResourceValueParser.getInstance().parse( value );
    }
}
