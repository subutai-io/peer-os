package io.subutai.common.metric;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.Expose;


public class Disk
{
    @Expose
    @JsonProperty( "total" )
    Double total = 0.0;
    @Expose
    @JsonProperty( "used" )
    Double used = 0.0;


    public Disk( @JsonProperty( "total" ) final Double total, @JsonProperty( "used" ) final Double used )
    {
        this.total = total;
        this.used = used;
    }


    public Double getTotal()
    {
        return total;
    }


    public void setTotal( final Double total )
    {
        this.total = total;
    }


    public Double getUsed()
    {
        return used;
    }


    public void setUsed( final Double used )
    {
        this.used = used;
    }


    @JsonIgnore
    public Double getAvailableSpace()
    {
        return total != null && used != null ? total - used : 0;
    }


    @Override
    public String toString()
    {
        return "Disk{" + "total=" + total + ", used=" + used + '}';
    }
}