package io.subutai.hub.share.dto.metrics;


import com.fasterxml.jackson.annotation.JsonProperty;


public class MemoryDto
{
    @JsonProperty( "total" )
    private Double total= 0.0D;

    @JsonProperty( "available" )
    private Double available= 0.0D;

    @JsonProperty( "active" )
    private double active = 0.0D;

    @JsonProperty( "cached" )
    private double cached = 0.0D;

    @JsonProperty( "free" )
    private double memFree = 0.0D;

    @JsonProperty( "buffers" )
    private double buffers = 0.0D;


    public Double getTotal()
    {
        return total;
    }


    public void setTotal( final Double total )
    {
        this.total = total;
    }


    public double getActive()
    {
        return active;
    }


    public Double getAvailable()
    {
        return available;
    }


    public void setAvailable( final Double available )
    {
        this.available = available;
    }


    public void setActive( final double active )
    {
        this.active = active;
    }


    public double getCached()
    {
        return cached;
    }


    public void setCached( final double cached )
    {
        this.cached = cached;
    }


    public double getMemFree()
    {
        return memFree;
    }


    public void setMemFree( final double memFree )
    {
        this.memFree = memFree;
    }


    public double getBuffers()
    {
        return buffers;
    }


    public void setBuffers( final double buffers )
    {
        this.buffers = buffers;
    }


    @Override
    public String toString()
    {
        final StringBuffer sb = new StringBuffer( "MemoryDto{" );
        sb.append( "total=" ).append( total );
        sb.append( ", available=" ).append( available );
        sb.append( ", active=" ).append( active );
        sb.append( ", cached=" ).append( cached );
        sb.append( ", memFree=" ).append( memFree );
        sb.append( ", buffers=" ).append( buffers );
        sb.append( '}' );
        return sb.toString();
    }
}
