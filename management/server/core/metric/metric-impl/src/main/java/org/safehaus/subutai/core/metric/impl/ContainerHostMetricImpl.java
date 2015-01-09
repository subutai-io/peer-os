package org.safehaus.subutai.core.metric.impl;


import java.util.UUID;

import org.safehaus.subutai.core.metric.api.ContainerHostMetric;

import com.google.common.base.Preconditions;


/**
 * Implementation of ContainerHostMetric
 */
public class ContainerHostMetricImpl extends ContainerHostMetric
{

    protected void setEnvironmentId( final UUID environmentId )
    {
        Preconditions.checkNotNull( environmentId, "Invalid environment id" );

        this.environmentId = environmentId;
    }


    protected void setHostId( final UUID hostId )
    {

        Preconditions.checkNotNull( host, "Invalid host id" );

        this.hostId = hostId;
    }
}
