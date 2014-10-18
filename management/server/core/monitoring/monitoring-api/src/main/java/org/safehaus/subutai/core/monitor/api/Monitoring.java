package org.safehaus.subutai.core.monitor.api;


import java.util.Date;
import java.util.List;
import java.util.Set;


public interface Monitoring
{

    public List<Metric> getMetrics( Set<String> hosts, Set<MetricType> metricTypes, Date startDate, Date endDate,
                                         int limit ) throws MonitorException;
}
