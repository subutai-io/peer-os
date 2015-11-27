package io.subutai.common.metric;


import io.subutai.common.host.HostId;
import io.subutai.common.peer.EnvironmentId;
import io.subutai.common.resource.MeasureUnit;
import io.subutai.common.resource.ResourceType;
import io.subutai.common.resource.ResourceValue;


/**
 * Environment container resource alert class
 */
public class EnvironmentContainerResourceAlert extends ResourceAlert
{
    private EnvironmentId environmentId;


    public EnvironmentContainerResourceAlert( final HostId hostId, final EnvironmentId environmentId,
                                              final ResourceType resourceType, final ResourceValue currentValue,
                                              final ResourceValue quotaValue )
    {
        super( hostId, resourceType, currentValue, quotaValue );
        this.environmentId = environmentId;
    }


    public EnvironmentContainerResourceAlert( final ResourceAlert alert, final EnvironmentId environmentId )
    {
        super( alert.getHostId(), alert.getResourceType(), alert.getCurrentValue(), alert.getQuotaValue() );
        this.environmentId = environmentId;
    }


    public EnvironmentId getEnvironmentId()
    {
        return environmentId;
    }
}
