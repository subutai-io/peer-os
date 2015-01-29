package org.safehaus.subutai.core.env.rest;


import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.environment.EnvironmentStatus;


/**
 * Trimmed environment for REST
 */
public class EnvironmentJson
{
    private UUID id;
    private String name;
    private EnvironmentStatus status;
    private String publicKey;
    private Set<ContainerJson> containers;


    public EnvironmentJson( final UUID id, final String name, final EnvironmentStatus status, final String publicKey,
                            final Set<ContainerJson> containers )
    {
        this.id = id;
        this.name = name;
        this.status = status;
        this.publicKey = publicKey;
        this.containers = containers;
    }


    public UUID getId()
    {
        return id;
    }


    public void setId( final UUID id )
    {
        this.id = id;
    }


    public String getName()
    {
        return name;
    }


    public void setName( final String name )
    {
        this.name = name;
    }


    public EnvironmentStatus getStatus()
    {
        return status;
    }


    public void setStatus( final EnvironmentStatus status )
    {
        this.status = status;
    }


    public String getPublicKey()
    {
        return publicKey;
    }


    public void setPublicKey( final String publicKey )
    {
        this.publicKey = publicKey;
    }


    public Set<ContainerJson> getContainers()
    {
        return containers;
    }


    public void setContainers( final Set<ContainerJson> containers )
    {
        this.containers = containers;
    }
}
