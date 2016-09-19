package io.subutai.common.quota;


import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.subutai.common.resource.ByteValueResource;
import io.subutai.common.resource.ContainerResourceType;


/**
 * Container ROOTFS resource class
 */
public class ContainerRootfsResource extends ContainerDiskResource
{
    public ContainerRootfsResource( @JsonProperty( value = "resourceValue" ) final ByteValueResource value )
    {
        super( ContainerResourceType.ROOTFS, value );
    }


    public ContainerRootfsResource()
    {
        this( new ByteValueResource( BigDecimal.ZERO ) );
    }
}
