package io.subutai.core.environment.rest;


import org.codehaus.jackson.annotate.JsonProperty;

import io.subutai.common.host.ContainerHostState;


public class ContainerStateDto
{
    @JsonProperty( "STATE" )
    private final ContainerHostState state;


    public ContainerStateDto( final ContainerHostState status )
    {
        this.state = status;
    }
}
