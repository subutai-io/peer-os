package io.subutai.common.environment;


import java.util.HashSet;
import java.util.Set;

import org.codehaus.jackson.annotate.JsonProperty;

import com.google.common.base.Preconditions;

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
        Preconditions.checkNotNull( containers );

        this.containers = containers;
    }


    public Containers()
    {
    }


    public void addContainer( final ContainerHostInfo containerHostInfo )
    {

        Preconditions.checkNotNull( containerHostInfo, "Container host info could not be null." );


        containers.add( containerHostInfo );
    }


    public Set<ContainerHostInfo> getContainers()
    {
        return containers;
    }
}
