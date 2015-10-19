package io.subutai.common.metric;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonProperty;

import com.google.gson.annotations.Expose;


@XmlRootElement
@XmlAccessorType( XmlAccessType.FIELD )
public class Ram
{
    @Expose
    @JsonProperty
    Double total;
    @Expose
    @JsonProperty
    Double free;


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
