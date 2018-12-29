package io.subutai.bazaar.share.event.meta;


import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;

import static java.lang.String.format;


@JsonAutoDetect( fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE )
@JsonIgnoreProperties( ignoreUnknown = true )
public class OriginMeta implements Meta
{
    @JsonProperty
    private String peerId;

    @JsonProperty
    private String containerId;

    @JsonProperty
    private String environmentId;

    @JsonCreator
    public OriginMeta( final String subutaiOrigin )
    {
        Preconditions.checkNotNull( subutaiOrigin );
        final String[] parts = subutaiOrigin.split( ":" );
        if ( parts.length != 3 )
        {
            throw new IllegalArgumentException( "Invalid origin argument." );
        }
        this.environmentId = parts[0];
        this.peerId = parts[1];
        this.containerId = parts[2];
    }


    private OriginMeta()
    {
    }


    public String getId()
    {
        return format( "%s:%s:%s", this.environmentId, this.peerId, this.containerId );
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


    @Override
    public String toString()
    {
        return this.getId();
    }
}
