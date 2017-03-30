package io.subutai.common.host;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

//TODO 30/03/17 we need to obtain consolidated disk quota in heartbeat instead of separated var opt root and home


public class Quota
{
    @SerializedName( "cpu" )
    @JsonProperty( "cpu" )
    private Double cpu;
    @SerializedName( "ram" )
    @JsonProperty( "ram" )
    private Double ram;
    @SerializedName( "root" )
    @JsonProperty( "root" )
    private Double root;
    @SerializedName( "home" )
    @JsonProperty( "home" )
    private Double home;
    @SerializedName( "opt" )
    @JsonProperty( "opt" )
    private Double opt;
    @SerializedName( "var" )
    @JsonProperty( "var" )
    private Double var;


    public Double getCpu()
    {
        return cpu;
    }


    public Double getRam()
    {
        return ram;
    }


    public Double getRoot()
    {
        return root;
    }


    public Double getHome()
    {
        return home;
    }


    public Double getOpt()
    {
        return opt;
    }


    public Double getVar()
    {
        return var;
    }


    @Override
    public String toString()
    {
        final StringBuffer sb = new StringBuffer( "Quota{" );
        sb.append( "cpu=" ).append( cpu );
        sb.append( ", ram=" ).append( ram );
        sb.append( ", root=" ).append( root );
        sb.append( ", home=" ).append( home );
        sb.append( ", opt=" ).append( opt );
        sb.append( ", var=" ).append( var );
        sb.append( '}' );
        return sb.toString();
    }
}
