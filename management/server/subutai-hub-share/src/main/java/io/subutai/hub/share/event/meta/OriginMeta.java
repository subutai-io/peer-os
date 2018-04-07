package io.subutai.hub.share.event.meta;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Preconditions;


public class OriginMeta implements Meta
{
    private String id;

    @JsonIgnore
    private String peerId;

    @JsonIgnore
    private String containerId;

    @JsonIgnore
    private String environmentId;


    @JsonCreator
    public OriginMeta( final String subutaiOrigin )
    {
        Preconditions.checkNotNull( subutaiOrigin );
        this.id = subutaiOrigin;
        prase();
    }


    private OriginMeta()
    {
    }


    private void prase()
    {
        final String[] splittedOrigins = this.id.split( ":" );
        this.peerId = splittedOrigins[0];
        if ( splittedOrigins.length > 1 )
        {
            this.containerId = splittedOrigins[1];
            if ( splittedOrigins.length > 2 )
            {
                this.environmentId = splittedOrigins[2];
            }
        }
    }


    public String getId()
    {
        return id;
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
        return String.format( "%s:%s:%s", peerId, environmentId, containerId );
    }
}
