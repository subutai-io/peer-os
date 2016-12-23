package io.subutai.common.metric;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.Expose;


@JsonIgnoreProperties( ignoreUnknown = true )
public class Ram
{
    @Expose
    @JsonProperty( "total" )
    Double total;
    @Expose
    @JsonProperty( "free" )
    Double free;

    @Expose
    @JsonProperty( "cached" )
    Double cached;


    public Ram( @JsonProperty( "total" ) final Double total, @JsonProperty( "free" ) final Double free,
                @JsonProperty( "cached" ) final Double cached )
    {
        this.total = total;
        this.free = free;
        this.cached = cached;
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


    public Double getCached()
    {
        return cached;
    }


    public void setCached( final Double cached )
    {
        this.cached = cached;
    }


    @Override
    public String toString()
    {
        final StringBuffer sb = new StringBuffer( "Ram{" );
        sb.append( "total=" ).append( total );
        sb.append( ", free=" ).append( free );
        sb.append( ", cached=" ).append( cached );
        sb.append( '}' );
        return sb.toString();
    }
}
