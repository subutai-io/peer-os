package io.subutai.core.metric.impl;


import com.google.common.base.Preconditions;

import io.subutai.common.metric.ContainerHostMetric;


/**
 * Implementation of ContainerHostMetric
 */
public class ContainerHostMetricImpl extends ContainerHostMetric
{

    protected void setEnvironmentId( final String environmentId )
    {
        Preconditions.checkNotNull( environmentId, "Invalid environment id" );

        this.environmentId = environmentId;
    }


    protected void setHostId( final String hostId )
    {

        Preconditions.checkNotNull( host, "Invalid host id" );

        this.hostId = hostId;
    }
}
