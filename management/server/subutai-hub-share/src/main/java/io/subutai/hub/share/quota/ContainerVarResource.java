package io.subutai.common.quota;


import java.math.BigDecimal;

import io.subutai.hub.share.resource.ByteValueResource;
import io.subutai.hub.share.resource.ContainerResourceType;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Container VAR resource class
 */
public class ContainerVarResource extends ContainerDiskResource
{
    public ContainerVarResource( @JsonProperty( value = "resourceValue" ) final ByteValueResource value )
    {
        super( ContainerResourceType.VAR, value );
    }


    public ContainerVarResource()
    {
        this( new ByteValueResource( BigDecimal.ZERO ) );
    }
}
