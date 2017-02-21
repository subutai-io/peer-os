package io.subutai.hub.share.quota;


import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.subutai.hub.share.resource.ByteUnit;
import io.subutai.hub.share.resource.ByteValueResource;
import io.subutai.hub.share.resource.ContainerResourceType;


/**
 * Container OPT resource class
 */

@JsonSerialize( using = ContainerResourceSerializer.class )
@JsonTypeName( "opt" )
public class ContainerOptResource extends ContainerDiskResource
{
    public ContainerOptResource( final ByteValueResource value )
    {
        super( ContainerResourceType.OPT, value );
    }


    public ContainerOptResource()
    {
        this( new ByteValueResource( BigDecimal.ZERO ) );
    }


    @JsonCreator
    public ContainerOptResource( @JsonProperty( "value" ) final String value )
    {
        super( ContainerResourceType.OPT, value );
    }


    public ContainerOptResource( final double value, final ByteUnit unit )
    {
        super( ContainerResourceType.OPT, value, unit );
    }
}
