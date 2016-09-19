package io.subutai.hub.share.quota;


import java.math.BigDecimal;

import io.subutai.hub.share.resource.ByteValueResource;
import io.subutai.hub.share.resource.ContainerResourceType;


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
