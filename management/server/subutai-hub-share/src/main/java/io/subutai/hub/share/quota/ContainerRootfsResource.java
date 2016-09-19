package io.subutai.hub.share.quota;


import java.math.BigDecimal;

import io.subutai.hub.share.resource.ByteValueResource;
import io.subutai.hub.share.resource.ContainerResourceType;


/**
 * Container ROOTFS resource class
 */
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
}
