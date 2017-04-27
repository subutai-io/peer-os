package io.subutai.common.host;


import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;


public class Quota implements Serializable
{
    @SerializedName( "cpu" )
    @JsonProperty( "cpu" )
    private Double cpu;
    @SerializedName( "ram" )
    @JsonProperty( "ram" )
    private Double ram;
    @SerializedName( "disk" )
    @JsonProperty( "disk" )
    private Double disk;


    public Double getCpu()
    {
        return cpu;
    }


    public Double getRam()
    {
        return ram;
    }


    public Double getDisk()
    {
        return disk;
    }


    @Override
    public String toString()
    {
        final StringBuffer sb = new StringBuffer( "Quota{" );
        sb.append( "cpu=" ).append( cpu );
        sb.append( ", ram=" ).append( ram );
        sb.append( ", disk=" ).append( disk );
        sb.append( '}' );
        return sb.toString();
    }
}
