package io.subutai.hub.share.dto.metrics;


import com.fasterxml.jackson.annotation.JsonProperty;


public class MemoryDto
{
    @JsonProperty( "active" )
    private double active = 0.0D;

    @JsonProperty( "cached" )
    private double cached = 0.0D;

    @JsonProperty( "free" )
    private double memFree = 0.0D;

    @JsonProperty( "buffers" )
    private double buffers = 0.0D;


    public double getActive()
    {
        return active;
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
}
