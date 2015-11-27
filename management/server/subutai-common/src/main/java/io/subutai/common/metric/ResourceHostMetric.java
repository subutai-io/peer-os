package io.subutai.common.metric;


import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import io.subutai.common.host.HostInfo;


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


    public ResourceHostMetric( final String peerId, final HostInfo hostInfo, final Integer containersCount )
    {
        super( peerId, hostInfo );
        this.containersCount = containersCount;
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
                .format( "%s:%s", getPeerId() != null ? getPeerId() : "UNKNOWN", hostName != null ? hostName : getHostInfo().getHostname() );
    }
}
