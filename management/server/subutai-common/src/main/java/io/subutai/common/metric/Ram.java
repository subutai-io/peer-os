package io.subutai.common.metric;


import org.codehaus.jackson.annotate.JsonProperty;

import com.google.gson.annotations.Expose;


public class Ram
{
    @Expose
    @JsonProperty( "total" )
    Double total;
    @Expose
    @JsonProperty( "free" )
    Double free;


    public Ram( @JsonProperty( "total" ) final Double total, @JsonProperty( "free" ) final Double free )
    {
        this.total = total;
        this.free = free;
    }


    public Double getTotal()
    {
        return total;
    }


    public void setTotal( final Double total )
    {
        this.total = total;
    }


    public Double getFree()
    {
        return free;
    }


    public void setFree( final Double free )
    {
        this.free = free;
    }


    @Override
    public String toString()
    {
        final StringBuffer sb = new StringBuffer( "RAM{" );
        sb.append( "total=" ).append( total );
        sb.append( ", free=" ).append( free );
        sb.append( '}' );
        return sb.toString();
    }
}
