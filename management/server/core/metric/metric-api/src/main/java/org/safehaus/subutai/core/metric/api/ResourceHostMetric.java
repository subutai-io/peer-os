package org.safehaus.subutai.core.metric.api;


import org.safehaus.subutai.common.protocol.Agent;


/**
 * Interface for ResourceHostMetric
 */
public interface ResourceHostMetric extends Metric
{
    public Agent getAgent();//TODO return here ResourceHost
}
