package io.subutai.common.environment;


import java.util.HashSet;
import java.util.Set;

import org.codehaus.jackson.annotate.JsonProperty;

import io.subutai.common.host.ContainerHostInfo;


/**
 * Containers wrapper
 */
public class Containers
{
    @JsonProperty( "containers" )
    private Set<ContainerHostInfo> containers = new HashSet<>();


    public Containers( @JsonProperty( "containers" ) final Set<ContainerHostInfo> containers )
    {
        this.containers = containers;
    }


    public Containers()
    {
    }


    public void addContainer( final ContainerHostInfo containerHostInfo )
    {
        if ( containerHostInfo == null )
        {
            throw new IllegalArgumentException( "Container host info could not be null." );
        }

        containers.add( containerHostInfo );
    }


    public Set<ContainerHostInfo> getContainers()
    {
        return containers;
    }
}
