package io.subutai.common.peer;


import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import io.subutai.common.host.HostId;


/**
 * Container host identifier
 */
public class ContainerId extends HostId
{
    @JsonProperty( "hostName" )
    private String hostName;
    @JsonProperty( "peerId" )
    private PeerId peerId;
    @JsonProperty( "environmentId" )
    private EnvironmentId environmentId;


    public ContainerId( final String id )
    {
        super( id );
    }


    @JsonCreator()
    public ContainerId( @JsonProperty( "id" ) final String id, @JsonProperty( "hostName" ) final String hostName,
                        @JsonProperty( "peerId" ) final PeerId peerId,
                        @JsonProperty( "environmentId" ) final EnvironmentId environmentId )
    {
        super( id );
        this.hostName = hostName;
        this.peerId = peerId;
        this.environmentId = environmentId;
    }


    public String getHostName()
    {
        return hostName;
    }


    public PeerId getPeerId()
    {
        return peerId;
    }


    public EnvironmentId getEnvironmentId()
    {
        return environmentId;
    }
}
