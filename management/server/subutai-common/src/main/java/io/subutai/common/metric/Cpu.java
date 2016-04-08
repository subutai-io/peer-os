package io.subutai.common.metric;


import org.codehaus.jackson.annotate.JsonProperty;

import com.google.gson.annotations.Expose;


public class Cpu
{
    @Expose
    @JsonProperty( "model" )
    String model = "UNKNOWN";
    @Expose
    @JsonProperty( "idle" )
    Double idle = 0.0;
    @Expose
    @JsonProperty( "coreCount" )
    int coreCount = 0;
    @Expose
    @JsonProperty( "frequency" )
    double frequency = 0.0;


    public Cpu( @JsonProperty( "model" ) final String model, @JsonProperty( "idle" ) final Double idle,
                @JsonProperty( "coreCount" ) final int coreCount, @JsonProperty( "frequency" ) final double frequency )
    {
        this.model = model;
        this.idle = idle;
        this.coreCount = coreCount;
        this.frequency = frequency;
    }


    public String getModel()
    {
        return model;
    }


    public void setModel( final String model )
    {
        this.model = model;
    }


    public Double getIdle()
    {
        return idle;
    }


    public void setIdle( final Double idle )
    {
        this.idle = idle;
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


    @Override
    public String toString()
    {
        return "CPU{" + "model='" + model + '\'' + ", idle=" + idle + ", coreCount=" + coreCount + '}';
    }
}
