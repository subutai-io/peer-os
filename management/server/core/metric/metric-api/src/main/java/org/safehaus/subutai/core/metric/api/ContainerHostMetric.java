package org.safehaus.subutai.core.metric.api;


import org.safehaus.subutai.common.protocol.Container;


/**
 * Interface for ContainerHostMetric
 */
public interface ContainerHostMetric extends Metric
{
    public Container getContainer();  //TODO return here ContainerHost
}
