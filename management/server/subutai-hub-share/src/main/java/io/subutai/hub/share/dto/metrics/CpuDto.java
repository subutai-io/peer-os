package io.subutai.hub.share.dto.metrics;


import com.fasterxml.jackson.annotation.JsonProperty;


public class CpuDto
{
    @JsonProperty( "model" )
    private String model = "UNKNOWN";

    @JsonProperty( "coreCount" )
    private int coreCount = 0;

    @JsonProperty( "frequency" )
    private double frequency = 0.0;

    private double system = 0.0;

    private double idle = 0.0;

    private double iowait = 0.0;

    private double user = 0.0;

    private double nice = 0.0;


    public String getModel()
    {
        return model;
    }


    public void setModel( final String model )
    {
        this.model = model;
    }


    public int getCoreCount()
    {
        return coreCount;
    }


    public void setCoreCount( final int coreCount )
    {
        this.coreCount = coreCount;
    }


    public double getFrequency()
    {
        return frequency;
    }


    public void setFrequency( final double frequency )
    {
        this.frequency = frequency;
    }


    public double getSystem()
    {
        return system;
    }


    public void setSystem( final double system )
    {
        this.system = system;
    }


    public double getIdle()
    {
        return idle;
    }


    public void setIdle( final double idle )
    {
        this.idle = idle;
    }


    public double getIowait()
    {
        return iowait;
    }


    public void setIowait( final double iowait )
    {
        this.iowait = iowait;
    }


    public double getUser()
    {
        return user;
    }


    public void setUser( final double user )
    {
        this.user = user;
    }


    public double getNice()
    {
        return nice;
    }


    public void setNice( final double nice )
    {
        this.nice = nice;
    }
}
