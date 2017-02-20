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
 * Container HOME resource class
 */
@JsonSerialize( using = ContainerResourceSerializer.class )
@JsonTypeName( "home" )
public class ContainerHomeResource extends ContainerDiskResource
{

    public ContainerHomeResource( final ByteValueResource value )
    {
        super( ContainerResourceType.HOME, value );
    }


    @JsonCreator
    public ContainerHomeResource( @JsonProperty( "value" ) final String value )
    {
        super( ContainerResourceType.HOME, value );
    }


    public ContainerHomeResource()
    {
        this( new ByteValueResource( BigDecimal.ZERO ) );
    }


    public ContainerHomeResource( final double value, final ByteUnit unit )
    {
        super( ContainerResourceType.HOME, value, unit );
    }
}
