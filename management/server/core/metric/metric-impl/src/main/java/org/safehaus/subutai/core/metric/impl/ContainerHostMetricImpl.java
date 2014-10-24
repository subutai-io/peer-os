package org.safehaus.subutai.core.metric.impl;


import java.util.UUID;

import org.safehaus.subutai.core.metric.api.ContainerHostMetric;

import com.google.common.base.Preconditions;


/**
 * Implementation of ContainerHostMetric
 */
public class ContainerHostMetricImpl extends ContainerHostMetric
{

    //this constructor is used in tests only since metrics are deserialized from json bypassing constructor
    public ContainerHostMetricImpl( String hostname, UUID environmentId, Double availableRam, Double usedRam,
                                    Double totalRam, Double availableDisk, Double usedDisk, Double totalDisk,
                                    Double cpuLoad5 )
    {

        this.hostname = hostname;
        this.environmentId = environmentId;
        this.availableRam = availableRam;
        this.usedRam = usedRam;
        this.totalRam = totalRam;
        this.availableDisk = availableDisk;
        this.usedDisk = usedDisk;
        this.totalDisk = totalDisk;
        this.cpuLoad5 = cpuLoad5;
    }


    protected void setEnvironmentId( final UUID environmentId )
    {
        Preconditions.checkNotNull( environmentId, "Invalid environment id" );

        this.environmentId = environmentId;
    }
}
