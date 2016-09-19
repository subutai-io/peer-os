package io.subutai.hub.share.quota;


import java.math.BigDecimal;

import io.subutai.hub.share.resource.ByteValueResource;
import io.subutai.hub.share.resource.ContainerResourceType;


/**
 * Container OPT resource class
 */
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
}
