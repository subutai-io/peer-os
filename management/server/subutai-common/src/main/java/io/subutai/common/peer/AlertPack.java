package io.subutai.common.peer;


import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import org.apache.commons.lang.time.DateUtils;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.subutai.common.metric.AlertResource;


/**
 * Alert packet for transferring between peers
 */
public class AlertPack
{
    @JsonProperty( "peerId" )
    String peerId;
    @JsonProperty( "environmentId" )
    String environmentId;
    @JsonProperty( "containerId" )
    String containerId;
    @JsonProperty( "templateName" )
    String templateName;
    @JsonProperty( "resource" )
    AlertResource resource;
    @JsonIgnore
    boolean delivered = false;
    @JsonProperty( "expiredTime" )
    Long expiredTime;


    public AlertPack( @JsonProperty( "peerId" ) final String peerId,
                      @JsonProperty( "environmentId" ) final String environmentId,
                      @JsonProperty( "containerId" ) final String containerId,
                      @JsonProperty( "templateName" ) final String templateName,
                      @JsonProperty( "resource" ) final AlertResource resource,
                      @JsonProperty( "expiredTime" ) final Long expiredTime )
    {
        this.peerId = peerId;
        this.environmentId = environmentId;
        this.containerId = containerId;
        this.resource = resource;
        this.templateName = templateName;
        this.expiredTime = expiredTime;
    }


    public String getPeerId()
    {
        return peerId;
    }


    public String getEnvironmentId()
    {
        return environmentId;
    }


    public String getContainerId()
    {
        return containerId;
    }


    public AlertResource getResource()
    {
        return resource;
    }


    public String getTemplateName()
    {
        return templateName;
    }


    public boolean isDelivered()
    {
        return delivered;
    }


    public void setDelivered( final boolean delivered )
    {
        this.delivered = delivered;
    }


    public Long getExpiredTime()
    {
        return expiredTime;
    }


    @JsonIgnore
    public boolean isExpired()
    {
        return System.currentTimeMillis() > expiredTime;
    }


    @JsonIgnore
    public long getLiveTime()
    {
        final long liveTime = expiredTime - System.currentTimeMillis();
        return liveTime < 0 ? liveTime : 0;
    }


    @Override
    public String toString()
    {
        return String.format( "%s %s Route:(%s,%s,%s) ttl:%d %s", resource.getId(), isExpired() ? "EXPIRED" : "NOT EXPIRED", peerId,
                environmentId, containerId, TimeUnit.SECONDS.convert( getLiveTime(), TimeUnit.MILLISECONDS ),
                delivered ? "DELIVERED" : "NOT DELIVERED" );
    }
}
