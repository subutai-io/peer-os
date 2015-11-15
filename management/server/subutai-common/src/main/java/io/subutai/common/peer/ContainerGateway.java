package io.subutai.common.peer;


import org.codehaus.jackson.annotate.JsonProperty;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import io.subutai.common.settings.Common;


/**
 * Container gateway
 */
public class ContainerGateway
{
    @JsonProperty( "containerId" )
    private ContainerId containerId;
    @JsonProperty( "gateway" )
    private String gateway;


    public ContainerGateway( @JsonProperty( "containerId" ) final ContainerId containerId,
                             @JsonProperty( "gateway" ) final String gateway )
    {
        Preconditions.checkNotNull( containerId, "Container ID is null" );
        Preconditions.checkNotNull( gateway, "Gateway IP is null" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( gateway ) && gateway.matches( Common.IP_REGEX ),
                "Invalid gateway IP" );

        this.containerId = containerId;
        this.gateway = gateway;
    }


    public ContainerId getContainerId()
    {
        return containerId;
    }


    public String getGateway()
    {
        return gateway;
    }
}
