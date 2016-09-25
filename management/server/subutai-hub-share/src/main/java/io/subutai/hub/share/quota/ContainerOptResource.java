package io.subutai.hub.share.quota;


import java.math.BigDecimal;

import io.subutai.hub.share.resource.ByteValueResource;
import io.subutai.hub.share.resource.ContainerResourceType;
import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * Container OPT resource class
 */
public class ContainerOptResource extends ContainerDiskResource
{
    public ContainerOptResource( @JsonProperty( value = "resourceValue" ) final ByteValueResource value )
    {
        super( ContainerResourceType.OPT, value );
    }


    public ContainerOptResource()
    {
        this( new ByteValueResource( BigDecimal.ZERO ) );
    }
}
