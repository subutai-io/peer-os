package org.safehaus.subutai.core.monitor.api;


import java.util.Date;


/**
 * Metric
 */
public class Metric
{
    private MetricType metricType;
    private Double value;
    private String units;
    private String host;
    private Date timestamp;
    private Long read;
    private Long write;


    public Metric( final MetricType metricType, final Double value, final String units, final String host,
                   final Date timestamp )
    {
        this.metricType = metricType;
        this.value = value;
        this.units = units;
        this.host = host;
        this.timestamp = timestamp;
    }


    public Metric( final MetricType metricType, final Long read, final Long write, final String host,
                   final Date timestamp )
    {
        this.metricType = metricType;
        this.host = host;
        this.timestamp = ( Date ) timestamp.clone();
        this.read = read;
        this.write = write;
    }


    public Long getRead()
    {
        return read;
    }


    public Long getWrite()
    {
        return write;
    }


    public MetricType getMetricType()
    {
        return metricType;
    }


    public Double getValue()
    {
        return value;
    }


    public String getUnits()
    {
        return units;
    }


    public String getHost()
    {
        return host;
    }


    public Date getTimestamp()
    {
        return ( Date ) timestamp.clone();
    }


    @Override
    public String toString()
    {
        return "SystemMetric{" +
                "metricType=" + metricType +
                ", value=" + value +
                ", units='" + units + '\'' +
                ", host='" + host + '\'' +
                ", timestamp=" + timestamp +
                ", read=" + read +
                ", write=" + write +
                '}';
    }
}
