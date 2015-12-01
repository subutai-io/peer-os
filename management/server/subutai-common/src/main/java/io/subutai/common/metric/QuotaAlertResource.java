package io.subutai.common.metric;


import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.google.common.base.Preconditions;

import io.subutai.common.host.HostId;


/**
 * Resource alert value
 */
@JsonIgnoreProperties( ignoreUnknown = true )
public class QuotaAlertResource implements AlertResource
{
    @JsonProperty( "resource" )
    private ResourceAlert resource;
    @JsonProperty( "createdTime" )
    private Long createdTime;


    public QuotaAlertResource( @JsonProperty( "resource" ) final ResourceAlert resource,
                               @JsonProperty( "createdTime" ) Long createdTime )
    {
        Preconditions.checkNotNull( createdTime );
        Preconditions.checkNotNull( resource );
        Preconditions.checkNotNull( resource.getHostId() );
        Preconditions.checkNotNull( resource.getResourceType() );
        this.resource = resource;
        this.createdTime = createdTime;
    }


    @JsonIgnore
    @Override
    public String getId()
    {
        return resource.getHostId() + ":" + resource.getResourceType();
    }


    @JsonIgnore
    @Override
    public ResourceAlert getValue()
    {
        return resource;
    }


    @JsonIgnore
    @Override
    public HostId getHostId()
    {
        return resource.getHostId();
    }


    @JsonIgnore
    @Override
    public AlertType getType()
    {
        return AlertType.ENVIRONMENT_ALERT;
    }


    @JsonIgnore
    @Override
    public long getLiveTime()
    {
        return System.currentTimeMillis() - createdTime;
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
