package io.subutai.common.quota;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.subutai.common.resource.ContainerResourceType;
import io.subutai.common.resource.NumericValueResource;


/**
 * Container CPU resource class
 */
public class ContainerCpuResource extends ContainerResource<NumericValueResource>
{
    public ContainerCpuResource( @JsonProperty( value = "resourceValue" )final NumericValueResource resourceValue )
    {
        super( ContainerResourceType.CPU, resourceValue );
    }

    /**
     * Usually used to write value to CLI
     */
    @JsonIgnore
    @Override
    public String getWriteValue()
    {
        return String.format( "%d", resource.getValue().intValue() );
    }


    /**
     * Usually used to display resource value
     */
    @JsonIgnore
    @Override
    public String getPrintValue()
    {
        return String.format( "%s%%", resource.getValue().intValue() );
    }
}
