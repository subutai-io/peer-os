package io.subutai.common.peer;


import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import io.subutai.common.host.HostId;


/**
 * Container host identifier
 */
public class ContainerId extends HostId
{
    @JsonProperty("peerId")
    private PeerId peerId;
    @JsonProperty("environmentId")
    private EnvironmentId environmentId;


    @JsonCreator()
    public ContainerId( @JsonProperty("id")final String id, @JsonProperty("peerId")final PeerId peerId, @JsonProperty("environmentId")final EnvironmentId environmentId )
    {
        super( id );
        this.peerId = peerId;
        this.environmentId = environmentId;
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
