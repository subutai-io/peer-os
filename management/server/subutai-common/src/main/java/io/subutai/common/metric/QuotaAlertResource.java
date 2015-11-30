package io.subutai.common.metric;


import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import com.google.common.base.Preconditions;

import io.subutai.common.host.HostId;


/**
 * Resource alert value
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class QuotaAlertResource implements AlertResource
{
    @JsonProperty( "value" )
    private ResourceAlert resource;


    public QuotaAlertResource( @JsonProperty( "resource" ) final ResourceAlert resource )
    {
        Preconditions.checkNotNull( resource );
        Preconditions.checkNotNull( resource.getHostId() );
        Preconditions.checkNotNull( resource.getResourceType() );
        this.resource = resource;
    }


    @Override
    public String getId()
    {
        return resource.getHostId() + ":" + resource.getResourceType();
    }


    @Override
    public ResourceAlert getValue()
    {
        return resource;
    }


    @Override
    public HostId getHostId()
    {
        return resource.getHostId();
    }


    @Override
    public AlertType getType()
    {
        return AlertType.ENVIRONMENT_ALERT;
    }


    @Override
    public String toString()
    {
        final StringBuffer sb = new StringBuffer( "ResourceAlertValue{" );
        sb.append( "resource=" ).append( resource );
        sb.append( '}' );
        return sb.toString();
    }
}
