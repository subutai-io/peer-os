package io.subutai.common.quota;


import java.math.BigDecimal;

import io.subutai.hub.share.resource.ByteValueResource;
import io.subutai.hub.share.resource.ContainerResourceType;
import com.fasterxml.jackson.annotation.JsonProperty;

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
