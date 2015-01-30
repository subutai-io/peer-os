package org.safehaus.subutai.core.environment.rest;


import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.core.environment.api.helper.EnvironmentStatusEnum;


/**
 * Trimmed environment for REST
 */
public class EnvironmentJson
{
    private UUID id;
    private String name;
    private EnvironmentStatusEnum status;
    private String publicKey;
    private Set<ContainerJson> containers;


    public EnvironmentJson( final UUID id, final String name, final EnvironmentStatusEnum status,
                            final String publicKey, final Set<ContainerJson> containers )
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


    public EnvironmentStatusEnum getStatus()
    {
        return status;
    }


    public void setStatus( final EnvironmentStatusEnum status )
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
