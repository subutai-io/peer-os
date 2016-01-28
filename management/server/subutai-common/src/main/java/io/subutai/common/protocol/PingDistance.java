package io.subutai.common.protocol;


import org.codehaus.jackson.annotate.JsonProperty;


/**
 * Ping distance class
 */
public class PingDistance
{
    @JsonProperty( "min" )
    private Double min;
    @JsonProperty( "avg" )
    private Double avg;
    @JsonProperty( "max" )
    private Double max;
    @JsonProperty( "mdev" )
    private Double mdev;


    public PingDistance( @JsonProperty( "min" ) final double min, @JsonProperty( "avg" ) final double avg,
                         @JsonProperty( "max" ) final double max, @JsonProperty( "mdev" ) final double mdev )
    {
        this.min = min;
        this.avg = avg;
        this.max = max;
        this.mdev = mdev;
    }


    public double getMin()
    {
        return min;
    }


    public double getAvg()
    {
        return avg;
    }


    public double getMax()
    {
        return max;
    }


    public double getMdev()
    {
        return mdev;
    }


    public boolean isValid()
    {
        return min != null && avg != null && max != null && mdev != null;
    }
}
