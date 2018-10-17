package io.subutai.bazaar.share.dto.metrics;


import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;


@JsonIgnoreProperties( ignoreUnknown = true )
public class PeerMetricsDto
{
    @JsonProperty( "peerId" )
    private String peerId;

    @JsonProperty( "startTime" )
    private Long startTime;

    @JsonProperty( "endTime" )
    private Long endTime;

    @JsonProperty( "createdTime" )
    private Long createdTime = System.currentTimeMillis();

    @JsonProperty( "metrics" )
    private Set<HostMetricsDto> metrics = new HashSet<>();

    @JsonProperty( "registeredRhIds" )
    private Set<String> registeredRhIds = new HashSet<>();


    @JsonCreator
    public PeerMetricsDto( @JsonProperty( value = "peerId", required = true ) final String peerId,
                           @JsonProperty( value = "startTime", required = true ) final Long startTime,
                           @JsonProperty( value = "endTime", required = true ) final Long endTime,
                           @JsonProperty( value = "metrics", required = true ) final Set<HostMetricsDto> metrics,
                           @JsonProperty( value = "registeredRhIds", required = true )
                           final Set<String> registeredRhIds,
                           @JsonProperty( value = "createdTime", required = true ) Long createdTime )
    {
        this.peerId = peerId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.metrics = metrics;
        this.registeredRhIds = registeredRhIds;
        this.createdTime = createdTime;
    }


    public PeerMetricsDto( final String peerId, final Long startTime, final Long endTime )
    {
        this.peerId = peerId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.createdTime = System.currentTimeMillis();
    }


    public String getPeerId()
    {
        return peerId;
    }


    public void setPeerId( final String peerId )
    {
        this.peerId = peerId;
    }


    public Long getCreatedTime()
    {
        return createdTime;
    }


    public void setCreatedTime( final Long createdTime )
    {
        this.createdTime = createdTime;
    }


    public Long getStartTime()
    {
        return startTime;
    }


    public void setStartTime( final Long startTime )
    {
        this.startTime = startTime;
    }


    public Long getEndTime()
    {
        return endTime;
    }


    public void setEndTime( final Long endTime )
    {
        this.endTime = endTime;
    }


    public Set<HostMetricsDto> getMetrics()
    {
        return metrics;
    }


    public void setMetrics( final Set<HostMetricsDto> metrics )
    {
        this.metrics = metrics;
    }


    public void addHostMetrics( final HostMetricsDto metrics )
    {
        if ( metrics == null )
        {
            throw new IllegalArgumentException( "Host metrics could not be null." );
        }

        this.metrics.add( metrics );
    }


    public Set<String> getRegisteredRhIds()
    {
        return registeredRhIds;
    }


    public void setRegisteredRhIds( final Set<String> registeredRhIds )
    {
        Preconditions.checkNotNull( registeredRhIds );

        this.registeredRhIds = registeredRhIds;
    }


    @JsonIgnore
    private HostMetricsDto getHostMetricsById( final String hostId )
    {
        for ( HostMetricsDto hostMetrics : this.metrics )
        {
            if ( hostMetrics.getHostId().equals( hostId ) )
            {
                return hostMetrics;
            }
        }
        return null;
    }


    @Override
    public String toString()
    {
        final StringBuffer sb = new StringBuffer( "PeerMetricsDto{" );
        sb.append( "peerId='" ).append( peerId ).append( '\'' );
        sb.append( ", startTime=" ).append( startTime );
        sb.append( ", endTime=" ).append( endTime );
        sb.append( ", createdTime=" ).append( createdTime );
        sb.append( ", metrics=" ).append( metrics );
        sb.append( '}' );
        return sb.toString();
    }
}
