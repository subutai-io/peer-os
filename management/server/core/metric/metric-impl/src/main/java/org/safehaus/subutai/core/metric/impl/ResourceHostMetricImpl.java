package org.safehaus.subutai.core.metric.impl;


import java.util.UUID;

import org.safehaus.subutai.core.metric.api.ResourceHostMetric;

import com.google.common.base.Preconditions;


/**
 * Implementation of ResourceHostMetric
 */
public class ResourceHostMetricImpl extends ResourceHostMetric
{

    //this constructor is used in tests only since metrics are deserialized from json bypassing constructor
    public ResourceHostMetricImpl( String hostname, UUID peerId, Double availableRam, Double usedRam, Double totalRam,
                                   Double availableDisk, Double usedDisk, Double totalDisk, Double cpuLoad5 )
    {

        this.hostname = hostname;
        this.peerId = peerId;
        this.availableRam = availableRam;
        this.usedRam = usedRam;
        this.totalRam = totalRam;
        this.availableDisk = availableDisk;
        this.usedDisk = usedDisk;
        this.totalDisk = totalDisk;
        this.cpuLoad5 = cpuLoad5;
    }


    protected void setPeerId( final UUID peerId )
    {
        Preconditions.checkNotNull( peerId, "Invalid peer id" );

        this.peerId = peerId;
    }
}
