package io.subutai.common.host;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.base.Preconditions;

import static java.lang.String.format;


public class SubutaiOrigin
{
    private static final String UNDNEFINED = "undefined";
    private String environmentId;
    private String peerId;
    private String containerId;


    @JsonCreator
    public SubutaiOrigin( final String origin )
    {
        Preconditions.checkNotNull( origin );

        final String[] parts = origin.split( ":" );

        if ( parts.length != 3 )
        {
            throw new IllegalArgumentException( "Invalid subutai origin" );
        }

        this.environmentId = parts[0];
        this.peerId = parts[1];
        this.containerId = parts[2];
    }


    public SubutaiOrigin( final String environmentId, final String peerId, final String containerId )
    {
        this.environmentId = environmentId == null ? UNDNEFINED : environmentId;
        this.peerId = peerId == null ? UNDNEFINED : peerId;
        this.containerId = containerId == null ? UNDNEFINED : containerId;
    }


    public String getEnvironmentId()
    {
        return environmentId;
    }


    public String getPeerId()
    {
        return peerId;
    }


    public String getContainerId()
    {
        return containerId;
    }


    @Override
    public String toString()
    {
        return format( "%s:%s:%s", environmentId, peerId, containerId );
    }
}
