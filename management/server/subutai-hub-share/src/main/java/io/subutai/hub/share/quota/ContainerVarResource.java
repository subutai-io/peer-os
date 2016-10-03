package io.subutai.hub.share.quota;


import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.subutai.hub.share.resource.ByteValueResource;
import io.subutai.hub.share.resource.ContainerResourceType;


/**
 * Container VAR resource class
 */

@JsonSerialize( using = ContainerResourceSerializer.class )
@JsonTypeName( "var" )
public class ContainerVarResource extends ContainerDiskResource
{

    public ContainerVarResource( final ByteValueResource value )
    {
        super( ContainerResourceType.VAR, value );
    }


    public ContainerVarResource()
    {
        this( new ByteValueResource( BigDecimal.ZERO ) );
    }


    @JsonCreator
    public ContainerVarResource( @JsonProperty( "value" ) final String value )
    {
        super( ContainerResourceType.VAR, value );
    }
}
