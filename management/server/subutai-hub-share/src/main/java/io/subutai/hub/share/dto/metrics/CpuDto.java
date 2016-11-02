package io.subutai.hub.share.dto.metrics;


public class CpuDto
{
    private double system = 0.0;

    private double idle = 0.0;

    private double iowait = 0.0;

    private double user = 0.0;

    private double nice = 0.0;


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
