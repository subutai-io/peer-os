package io.subutai.common.resource;


import java.math.BigDecimal;


/**
 * Common resource interface
 */
public interface Resource
{
    ResourceType getResourceType();

    MeasureUnit getMeasureUnit();

    ResourceValue getCurrentValue( MeasureUnit unit );

    ResourceValue getQuotaValue( MeasureUnit unit );

    ResourceValue getMaxValue( MeasureUnit unit );
}
