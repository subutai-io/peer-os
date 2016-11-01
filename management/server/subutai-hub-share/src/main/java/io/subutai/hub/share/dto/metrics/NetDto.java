package io.subutai.hub.share.dto.metrics;


public class NetDto
{
    private String iface;

    private double in = 0.0D;

    private double out = 0.0D;


    public NetDto( final String iface, final double in, final double out )
    {
        this.iface = iface;
        this.in = in;
        this.out = out;
    }


    public String getIface()
    {
        return iface;
    }


    public void setIface( final String iface )
    {
        this.iface = iface;
    }


    public double getIn()
    {
        return in;
    }


    public void setIn( final double in )
    {
        this.in = in;
    }


    public double getOut()
    {
        return out;
    }


    public void setOut( final double out )
    {
        this.out = out;
    }
}
