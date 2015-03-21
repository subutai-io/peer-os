package org.safehaus.subutai.common.metric;


import java.util.Date;

import org.safehaus.subutai.common.peer.Host;


public class HistoricalMetric {

    private Date timestamp;
    private double value;
    private Host host;
    private MetricType metricType;


    public HistoricalMetric( Host host, MetricType metricType, int timestamp, double value ) {
        this.host = host;
        this.metricType = metricType;
        this.timestamp = new Date( ( long ) timestamp * 1000 );
        this.value = value;
    }

    public Date getTimestamp() {
        return timestamp;
    }


    public double getValue() {
        return value;
    }


    public Host getHost() {
        return host;
    }


    public MetricType getMetricType() {
        return metricType;
    }
}
