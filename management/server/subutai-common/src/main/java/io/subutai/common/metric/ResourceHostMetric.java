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
}
