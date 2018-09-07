package io.subutai.bazaar.share.dto.metrics;


import com.fasterxml.jackson.annotation.JsonProperty;


public class MemoryDto
{
    @JsonProperty( "total" )
    private Double total = 0.0D;

    @JsonProperty( "available" )
    private Double available = 0.0D;

    @JsonProperty( "active" )
    private double active = 0.0D;

    @JsonProperty( "cached" )
    private double cached = 0.0D;

    @JsonProperty( "free" )
    private double memFree = 0.0D;

    @JsonProperty( "buffers" )
    private double buffers = 0.0D;

    @JsonProperty( "rss" )
    private double rss = 0.0D;


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


    public double getRss()
    {
        return rss;
    }


    public void setRss( final double rss )
    {
        this.rss = rss;
    }


    @Override
    public String toString()
    {
        return "MemoryDto{" + "total=" + total + ", available=" + available + ", active=" + active + ", cached="
                + cached + ", memFree=" + memFree + ", buffers=" + buffers + ", rss=" + rss + '}';
    }
}
