package io.subutai.core.environment.rest.ui;


import java.util.Set;

import io.subutai.common.environment.EnvironmentStatus;


/**
 * Trimmed environment for REST
 */
public class EnvironmentDto
{
    private String id;
    private String name;
    private String relationDeclaration;
    private EnvironmentStatus status;
    private Set<ContainerDto> containers;


    public EnvironmentDto( final String id, final String name, final EnvironmentStatus status,
                           final Set<ContainerDto> containers, String relationDeclaration )
    {
        this.id = id;
        this.name = name;
        this.status = status;
        this.containers = containers;
        this.relationDeclaration = relationDeclaration;
    }


    public String getId()
    {
        return id;
    }


    public void setId( final String id )
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


    public Set<ContainerDto> getContainers()
    {
        return containers;
    }


    public void setContainers( final Set<ContainerDto> containers )
    {
        this.containers = containers;
    }


    public String getRelationDeclaration()
    {
        return relationDeclaration;
    }


    public void setRelationDeclaration( final String relationDeclaration )
    {
        this.relationDeclaration = relationDeclaration;
    }
}
