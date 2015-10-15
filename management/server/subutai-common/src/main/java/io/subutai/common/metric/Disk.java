package io.subutai.common.metric;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import com.google.gson.annotations.Expose;


@XmlRootElement
@XmlAccessorType( XmlAccessType.FIELD )
public class Disk
{
    @Expose
    @JsonProperty
    Double total = 0.0;
    @Expose
    @JsonProperty
    Double used = 0.0;


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
        return total - used;
    }


    @Override
    public String toString()
    {
        final StringBuffer sb = new StringBuffer( "Disk{" );
        sb.append( "total=" ).append( total );
        sb.append( ", used=" ).append( used );
        sb.append( '}' );
        return sb.toString();
    }
}