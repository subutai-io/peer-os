package io.subutai.common.metric;


import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;


/**
 * Resource host metric
 */
@XmlRootElement
public class ResourceHostMetric extends BaseMetric
{
    private static final double GB_DIVIDER = 1024 / 1024 / 1024;
    @JsonIgnore
    private String peerId;
    @JsonProperty
    private Integer containersCount;


    public ResourceHostMetric()
    {
    }


    protected ResourceHostMetric( final Ram ram )
    {
        super( ram );
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


    public String getPeerId()
    {
        return peerId;
    }


    public void setPeerId( final String peerId )
    {
        this.peerId = peerId;
    }


    @Override
    public String toString()
    {
        return String.format( "%s on %s", hostName, peerId );
    }


    public String getDescription()
    {
        return String
                .format( "Hostname: %s<br/> RAM: %.3f/%.3fGb<br/> Disk: %.3f/%.3fGb<br/> CPU: %s<br/> Containers #: %d",
                        hostName, ram.getTotal() / GB_DIVIDER, ram.getFree() / GB_DIVIDER, disk.getTotal() / GB_DIVIDER,
                        disk.getUsed() / GB_DIVIDER, cpu.getModel(), containersCount );
    }
}
