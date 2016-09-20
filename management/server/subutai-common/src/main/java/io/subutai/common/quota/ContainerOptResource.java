package io.subutai.common.quota;


import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.subutai.common.resource.ByteValueResource;
import io.subutai.common.resource.ContainerResourceType;


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
