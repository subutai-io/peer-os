package io.subutai.hub.share.quota;


import java.math.BigDecimal;

import io.subutai.hub.share.resource.ByteValueResource;
import io.subutai.hub.share.resource.ContainerResourceType;
import com.fasterxml.jackson.annotation.JsonProperty;


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
