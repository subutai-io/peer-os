package io.subutai.common.quota;


import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.subutai.common.resource.ByteValueResource;
import io.subutai.common.resource.ContainerResourceType;


/**
 * Container HOME resource class
 */
public class ContainerHomeResource extends ContainerDiskResource
{
    public ContainerHomeResource( @JsonProperty( value = "resourceValue" ) final ByteValueResource value )
    {
        super( ContainerResourceType.HOME, value );
    }


    public ContainerHomeResource()
    {
        this( new ByteValueResource( BigDecimal.ZERO ) );
    }
}
