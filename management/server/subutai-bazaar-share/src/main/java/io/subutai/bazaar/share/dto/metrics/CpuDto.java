package io.subutai.bazaar.share.dto.metrics;


import com.fasterxml.jackson.annotation.JsonProperty;


public class CpuDto
{
    @JsonProperty( "model" )
    private String model = "UNKNOWN";

    @JsonProperty( "coreCount" )
    private int coreCount = 0;

    @JsonProperty( "frequency" )
    private double frequency = 0.0;

    @JsonProperty( "system" )
    private double system = 0.0;

    @JsonProperty( "idle" )
    private double idle = 0.0;

    @JsonProperty( "iowait" )
    private double iowait = 0.0;

    @JsonProperty( "user" )
    private double user = 0.0;

    @JsonProperty( "nice" )
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


    @Override
    public String toString()
    {
        final StringBuffer sb = new StringBuffer( "CpuDto{" );
        sb.append( "model='" ).append( model ).append( '\'' );
        sb.append( ", coreCount=" ).append( coreCount );
        sb.append( ", frequency=" ).append( frequency );
        sb.append( ", system=" ).append( system );
        sb.append( ", idle=" ).append( idle );
        sb.append( ", iowait=" ).append( iowait );
        sb.append( ", user=" ).append( user );
        sb.append( ", nice=" ).append( nice );
        sb.append( '}' );
        return sb.toString();
    }
}
