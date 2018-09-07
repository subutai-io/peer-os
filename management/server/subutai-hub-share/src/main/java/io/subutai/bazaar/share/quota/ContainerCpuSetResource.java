package io.subutai.bazaar.share.quota;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.subutai.bazaar.share.parser.CpuSetResourceValueParser;
import io.subutai.bazaar.share.resource.ContainerResourceType;
import io.subutai.bazaar.share.resource.StringValueResource;


/**
 * Container CPU set resource class
 */
@JsonSerialize( using = ContainerResourceSerializer.class )
@JsonTypeName( "cpuSet" )
public class ContainerCpuSetResource extends ContainerResource<StringValueResource>
{

    public ContainerCpuSetResource( final StringValueResource resourceValue )
    {
        super( ContainerResourceType.CPUSET, resourceValue );
    }


    @JsonCreator
    public ContainerCpuSetResource( @JsonProperty( "value" ) final String value )
    {
        super( ContainerResourceType.CPUSET, value );
    }


    /**
     * Usually used to write value to CLI
     */
    @Override
    public String getWriteValue()
    {
        return resource.getValue();
    }


    /**
     * Usually used to display resource value
     */
    @Override
    public String getPrintValue()
    {
        return String.format( "%s", resource.getValue() );
    }


    @Override
    protected StringValueResource parse( final String value )
    {
        return CpuSetResourceValueParser.getInstance().parse( value );
    }
}
