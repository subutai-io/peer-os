package io.subutai.common.quota;


import java.math.BigDecimal;

import io.subutai.common.resource.ByteValueResource;
import io.subutai.common.resource.ContainerResourceType;


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
