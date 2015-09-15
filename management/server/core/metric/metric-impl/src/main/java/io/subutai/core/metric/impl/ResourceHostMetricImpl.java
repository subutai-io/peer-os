package io.subutai.core.metric.impl;


import com.google.common.base.Preconditions;

import io.subutai.common.metric.ResourceHostMetric;


/**
 * Implementation of ResourceHostMetric
 */
public class ResourceHostMetricImpl extends ResourceHostMetric
{

    protected void setPeerId( final String peerId )
    {
        Preconditions.checkNotNull( peerId, "Invalid peer id" );

        this.peerId = peerId;
    }


    protected void setHostId( final String hostId )
    {

        Preconditions.checkNotNull( host, "Invalid host id" );

        this.hostId = hostId;
    }
}
