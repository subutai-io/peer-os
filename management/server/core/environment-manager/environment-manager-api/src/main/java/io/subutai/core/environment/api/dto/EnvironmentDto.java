package io.subutai.core.environment.api.dto;


import java.util.Set;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import io.subutai.common.environment.EnvironmentStatus;


public class EnvironmentDto
{
    @JsonProperty( "id" )
    private String id;
    @JsonProperty( "name" )
    private String name;
    @JsonProperty( "relationDeclaration" )
    private String relationDeclaration;
    @JsonProperty( "status" )
    private EnvironmentStatus status;
    @JsonIgnore
    private Boolean revoke;
    @JsonProperty( "containers" )
    private Set<ContainerDto> containers;

    // Where environment created: subutai, hub
    @JsonProperty( "dataSource" )
    private String dataSource;


    public EnvironmentDto( @JsonProperty( "id" ) final String id, @JsonProperty( "name" ) final String name,
                           @JsonProperty( "status" ) final EnvironmentStatus status,
                           @JsonProperty( "containers" ) final Set<ContainerDto> containers,
                           @JsonProperty( "relationDeclaration" ) String relationDeclaration,
                           @JsonProperty( "dataSource" ) String dataSource )
    {
        this.id = id;
        this.name = name;
        this.status = status;
        this.containers = containers;
        this.relationDeclaration = relationDeclaration;
        this.dataSource = dataSource;
    }


    public boolean isRevoke()
    {
        return revoke;
    }


    public void setRevoke( final boolean revoke )
    {
        this.revoke = revoke;
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


    public String getDataSource()
    {
        return dataSource;
    }
}