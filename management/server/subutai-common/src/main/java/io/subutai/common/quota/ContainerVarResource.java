package io.subutai.common.quota;


import java.math.BigDecimal;

import io.subutai.common.resource.ByteValueResource;
import io.subutai.common.resource.ContainerResourceType;


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
