package io.subutai.common.quota;


import java.math.BigDecimal;

import io.subutai.common.resource.ByteValueResource;
import io.subutai.common.resource.ContainerResourceType;


/**
 * Container HOME resource class
 */
public class ContainerHomeResource extends ContainerDiskResource
{
    public ContainerHomeResource( final ByteValueResource value )
    {
        super( ContainerResourceType.HOME, value );
    }


    public ContainerHomeResource()
    {
        this( new ByteValueResource( BigDecimal.ZERO ) );
    }
}
