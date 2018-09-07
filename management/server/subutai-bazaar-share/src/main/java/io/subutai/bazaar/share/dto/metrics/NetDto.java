package io.subutai.bazaar.share.dto.metrics;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;


public class NetDto
{
    @JsonProperty( value = "iface" )
    private String iface;

    @JsonProperty( value = "in" )
    private double in = 0.0D;

    @JsonProperty( value = "out" )
    private double out = 0.0D;


    @JsonCreator
    public NetDto( @JsonProperty( value = "iface", required = true ) final String iface,
                   @JsonProperty( value = "in" ) final double in, @JsonProperty( value = "out" ) final double out )
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


    @Override
    public String toString()
    {
        final StringBuffer sb = new StringBuffer( "NetDto{" );
        sb.append( "iface='" ).append( iface ).append( '\'' );
        sb.append( ", in=" ).append( in );
        sb.append( ", out=" ).append( out );
        sb.append( '}' );
        return sb.toString();
    }
}
