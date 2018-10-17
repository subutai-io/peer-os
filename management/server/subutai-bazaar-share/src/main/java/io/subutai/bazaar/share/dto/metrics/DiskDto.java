package io.subutai.bazaar.share.dto.metrics;


import com.fasterxml.jackson.annotation.JsonProperty;


public class DiskDto
{
    @JsonProperty( "total" )
    private double total = 0.0D;

    @JsonProperty( "available" )
    private double available = 0.0D;

    @JsonProperty( "used" )
    private double used = 0.0D;


    public double getTotal()
    {
        return total;
    }


    public void setTotal( final double total )
    {
        this.total = total;
    }


    public double getAvailable()
    {
        return available;
    }


    public void setAvailable( final double available )
    {
        this.available = available;
    }


    public double getUsed()
    {
        return used;
    }


    public void setUsed( final double used )
    {
        this.used = used;
    }


    @Override
    public String toString()
    {
        final StringBuffer sb = new StringBuffer( "DiskDto{" );
        sb.append( "total=" ).append( total );
        sb.append( ", available=" ).append( available );
        sb.append( ", used=" ).append( used );
        sb.append( '}' );
        return sb.toString();
    }
}
