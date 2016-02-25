package io.subutai.common.protocol;


import org.codehaus.jackson.annotate.JsonProperty;


/**
 * Ping distance class
 */
public class PingDistance
{
    @JsonProperty( "sourceIp" )
    private String sourceIp;
    @JsonProperty( "targetIp" )
    private String targetIp;
    @JsonProperty( "min" )
    private Double min;
    @JsonProperty( "avg" )
    private Double avg;
    @JsonProperty( "max" )
    private Double max;
    @JsonProperty( "mdev" )
    private Double mdev;


    public PingDistance( @JsonProperty( "sourceIp" ) final String sourceIp,
                         @JsonProperty( "targetIp" ) final String targetIp, @JsonProperty( "min" ) final Double min,
                         @JsonProperty( "avg" ) final Double avg, @JsonProperty( "max" ) final Double max,
                         @JsonProperty( "mdev" ) final Double mdev )
    {
        this.sourceIp = sourceIp;
        this.targetIp = targetIp;
        this.min = min;
        this.avg = avg;
        this.max = max;
        this.mdev = mdev;
    }


    public String getSourceIp()
    {
        return sourceIp;
    }


    public String getTargetIp()
    {
        return targetIp;
    }


    public Double getMin()
    {
        return min;
    }


    public Double getAvg()
    {
        return avg;
    }


    public Double getMax()
    {
        return max;
    }


    public Double getMdev()
    {
        return mdev;
    }


    public boolean isValid()
    {
        return min != null && avg != null && max != null && mdev != null;
    }
}
