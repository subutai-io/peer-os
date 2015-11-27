package io.subutai.common.metric;


import io.subutai.common.resource.ResourceType;
import io.subutai.common.resource.ResourceValue;


/**
 * Alert value interface
 */
public interface ResourceAlertValue extends AlertValue
{
    ResourceType getResourceType();

    ResourceValue getCurrentValue();

    ResourceValue getQuotaValue();

    //    ResourceValue getMaxValue( MeasureUnit unit );
}
