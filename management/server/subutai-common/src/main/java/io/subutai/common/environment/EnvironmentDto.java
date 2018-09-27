package io.subutai.common.environment;


import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;


public class EnvironmentDto
{
    @JsonProperty( "id" )
    private String id;
    @JsonProperty( "name" )
    private String name;
    @JsonProperty( "status" )
    private EnvironmentStatus status;
    @JsonIgnore
    private Boolean revoke;
    @JsonProperty( "containers" )
    private Set<ContainerDto> containers;

    // Where environment created: subutai, bazaar
    @JsonProperty( "dataSource" )
    private String dataSource;
    @JsonProperty( "username" )
    private String username;


    public EnvironmentDto( @JsonProperty( "id" ) final String id, @JsonProperty( "name" ) final String name,
                           @JsonProperty( "status" ) final EnvironmentStatus status,
                           @JsonProperty( "containers" ) final Set<ContainerDto> containers,
                           @JsonProperty( "dataSource" ) String dataSource,
                           @JsonProperty( "username" ) final String username )
    {
        this.id = id;
        this.name = name;
        this.status = status;
        this.containers = containers;
        this.dataSource = dataSource;
        this.username = username;
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


    public String getDataSource()
    {
        return dataSource;
    }


    public String getUsername()
    {
        return username;
    }
}