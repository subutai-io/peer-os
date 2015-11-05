package io.subutai.common.metric;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonProperty;

import com.google.gson.annotations.Expose;


@XmlRootElement
@XmlAccessorType( XmlAccessType.FIELD )
public class Cpu
{
    @Expose
    @JsonProperty
    String model = "UNKNOWN";
    @Expose
    @JsonProperty
    Double idle = 0.0;
    @Expose
    @JsonProperty
    int coreCount = 0;


    public String getModel()
    {
        return model;
    }


    public void setModel( final String model )
    {
        this.model = model;
    }


    public Double getIdle()
    {
        return idle;
    }


    public void setIdle( final Double idle )
    {
        this.idle = idle;
    }


    public int getCoreCount()
    {
        return coreCount;
    }


    public void setCoreCount( final int coreCount )
    {
        this.coreCount = coreCount;
    }


    @Override
    public String toString()
    {
        final StringBuffer sb = new StringBuffer( "CPU{" );
        sb.append( "model='" ).append( model ).append( '\'' );
        sb.append( ", idle=" ).append( idle );
        sb.append( ", coreCount=" ).append( coreCount );
        sb.append( '}' );
        return sb.toString();
    }
}
