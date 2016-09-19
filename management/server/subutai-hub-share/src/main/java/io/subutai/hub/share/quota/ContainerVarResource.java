package io.subutai.hub.share.quota;


import java.math.BigDecimal;

import io.subutai.hub.share.resource.ByteValueResource;
import io.subutai.hub.share.resource.ContainerResourceType;


/**
 * Container VAR resource class
 */
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
}
