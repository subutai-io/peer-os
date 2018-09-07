package io.subutai.bazaar.share.quota;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.subutai.bazaar.share.parser.CpuResourceValueParser;
import io.subutai.bazaar.share.resource.ContainerResourceType;
import io.subutai.bazaar.share.resource.NumericValueResource;


/**
 * Container CPU resource class
 */
@JsonSerialize( using = ContainerResourceSerializer.class )
@JsonTypeName( "cpu" )
public class ContainerCpuResource extends ContainerResource<NumericValueResource>
{

    public ContainerCpuResource( final NumericValueResource resourceValue )
    {
        super( ContainerResourceType.CPU, resourceValue );
    }


    public ContainerCpuResource( final Double resourceValue )
    {
        super( ContainerResourceType.CPU, new NumericValueResource( resourceValue ) );
    }


    public ContainerCpuResource( final int cpuValue )
    {
        super( ContainerResourceType.CPU, new NumericValueResource( cpuValue ) );
    }


    @JsonCreator
    public ContainerCpuResource( @JsonProperty( "value" ) final String value )
    {
        super( ContainerResourceType.CPU, value );
    }


    /**
     * Usually used to write value to CLI
     */
    @Override
    public String getWriteValue()
    {
        return String.format( "%d", resource.getValue().intValue() );
    }


    /**
     * Usually used to display resource value
     */
    @Override
    public String getPrintValue()
    {
        return String.format( "%s%%", resource.getValue().intValue() );
    }


    @Override
    protected NumericValueResource parse( final String value )
    {
        return CpuResourceValueParser.getInstance().parse( value );
    }
}
