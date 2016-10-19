package io.subutai.hub.share.quota;


import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.subutai.hub.share.resource.ByteValueResource;
import io.subutai.hub.share.resource.ContainerResourceType;


/**
 * Container ROOTFS resource class
 */
@JsonSerialize( using = ContainerResourceSerializer.class )
@JsonTypeName( "rootfs" )
public class ContainerRootfsResource extends ContainerDiskResource
{

    public ContainerRootfsResource( final ByteValueResource value )
    {
        super( ContainerResourceType.ROOTFS, value );
    }


    public ContainerRootfsResource()
    {
        this( new ByteValueResource( BigDecimal.ZERO ) );
    }


    @JsonCreator
    public ContainerRootfsResource( @JsonProperty( "value" ) final String value )
    {
        super( ContainerResourceType.ROOTFS, value );
    }
}
