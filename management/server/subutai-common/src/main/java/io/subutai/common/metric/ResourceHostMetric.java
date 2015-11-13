package io.subutai.common.metric;


import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;


/**
 * Resource host metric
 */
@XmlRootElement
public class ResourceHostMetric extends HostMetric
{
    @JsonProperty
    private Integer containersCount;


    public ResourceHostMetric()
    {
    }


    @JsonIgnore
    public Integer getContainersCount()
    {
        return containersCount;
    }


    public void setContainersCount( final Integer containersCount )
    {
        this.containersCount = containersCount;
    }


    @Override
    public String toString()
    {
        return String
                .format( "%s:%s", getPeerId() != null ? getPeerId() : "UNKNOWN", hostName != null ? hostName : hostId );
    }
}
