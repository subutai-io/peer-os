package io.subutai.hub.share.quota;


import com.fasterxml.jackson.annotation.JsonIgnore;

import io.subutai.hub.share.resource.ContainerResourceType;
import io.subutai.hub.share.resource.NumericValueResource;


/**
 * Container CPU resource class
 */
public class ContainerCpuResource extends ContainerResource<NumericValueResource>
{
    public ContainerCpuResource( final NumericValueResource resourceValue )
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
