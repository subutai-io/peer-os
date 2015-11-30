package io.subutai.common.metric;


import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import com.google.common.base.Preconditions;

import io.subutai.common.host.HostId;


/**
 * Resource alert value
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResourceAlertValue implements AlertValue
{
    @JsonProperty( "value" )
    private ResourceAlert value;


    public ResourceAlertValue( @JsonProperty( "value" ) final ResourceAlert value )
    {
        Preconditions.checkNotNull( value );
        Preconditions.checkNotNull( value.getHostId() );
        Preconditions.checkNotNull( value.getResourceType() );
        this.value = value;
    }


    @Override
    public String getId()
    {
        return value.getHostId() + ":" + value.getResourceType();
    }


    @Override
    public ResourceAlert getValue()
    {
        return value;
    }


    @Override
    public HostId getHostId()
    {
        return value.getHostId();
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
        sb.append( "value=" ).append( value );
        sb.append( '}' );
        return sb.toString();
    }
}
