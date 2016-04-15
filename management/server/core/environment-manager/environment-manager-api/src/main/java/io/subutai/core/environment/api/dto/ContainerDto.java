package io.subutai.core.environment.api.dto;


import java.util.Set;

import org.codehaus.jackson.annotate.JsonProperty;

import io.subutai.common.host.ContainerHostState;
import io.subutai.common.peer.ContainerSize;


/**
 * Trimmed container for REST
 */
public class ContainerDto
{
    @JsonProperty( "id" )
    private String id;
    @JsonProperty( "environmentId" )
    private String environmentId;
    @JsonProperty( "hostname" )
    private String hostname;
    @JsonProperty( "ip" )
    private String ip;
    @JsonProperty( "templateName" )
    private String templateName;
    @JsonProperty( "type" )
    private ContainerSize type;
    @JsonProperty( "arch" )
    private String arch;
    @JsonProperty( "tags" )
    private Set<String> tags;
    @JsonProperty( "peerId" )
    private String peerId;
    @JsonProperty( "hostId" )
    private String hostId;
    @JsonProperty( "local" )
    private boolean local;
    @JsonProperty( "state" )
    private ContainerHostState state;

    // Where environment of container created: subutai, hub
    @JsonProperty( "dataSource" )
    private String dataSource;


    public ContainerDto( @JsonProperty( "id" ) final String id,
                         @JsonProperty( "environmentId" ) final String environmentId,
                         @JsonProperty( "hostname" ) final String hostname, @JsonProperty( "ip" ) final String ip,
                         @JsonProperty( "templateName" ) final String templateName,
                         @JsonProperty( "type" ) final ContainerSize type, @JsonProperty( "arch" ) final String arch,
                         @JsonProperty( "tags" ) final Set<String> tags, @JsonProperty( "peerId" ) final String peerId,
                         @JsonProperty( "hostId" ) final String hostId, @JsonProperty( "local" ) boolean local,
                         @JsonProperty( "dataSource" ) String dataSource,
                         @JsonProperty( "state" ) ContainerHostState state )
    {
        this.id = id;
        this.environmentId = environmentId;
        this.hostname = hostname;
        this.ip = ip;
        this.templateName = templateName;
        this.type = type;
        this.arch = arch;
        this.tags = tags;
        this.peerId = peerId;
        this.hostId = hostId;
        this.local = local;

        this.dataSource = dataSource;
        this.state = state;
    }


    public String getId()
    {
        return id;
    }


    public void setId( final String id )
    {
        this.id = id;
    }


    public String getEnvironmentId()
    {
        return environmentId;
    }


    public void setEnvironmentId( final String environmentId )
    {
        this.environmentId = environmentId;
    }


    public String getHostname()
    {
        return hostname;
    }


    public void setHostname( final String hostname )
    {
        this.hostname = hostname;
    }


    public String getIp()
    {
        return ip;
    }


    public void setIp( final String ip )
    {
        this.ip = ip;
    }


    public String getTemplateName()
    {
        return templateName;
    }


    public void setTemplateName( final String templateName )
    {
        this.templateName = templateName;
    }


    public ContainerSize getType()
    {
        return type;
    }


    public void setType( final ContainerSize type )
    {
        this.type = type;
    }


    public Set<String> getTags()
    {
        return tags;
    }


    public void setTags( final Set<String> tags )
    {
        this.tags = tags;
    }


    public String getArch()
    {
        return arch;
    }


    public void setArch( final String arch )
    {
        this.arch = arch;
    }


    public String getPeerId()
    {
        return peerId;
    }


    public void setPeerId( String peerId )
    {
        this.peerId = peerId;
    }


    public String getHostId()
    {
        return hostId;
    }


    public void setHostId( String hostId )
    {
        this.hostId = hostId;
    }


    public boolean isLocal()
    {
        return local;
    }


    public void setLocal( boolean local )
    {
        this.local = local;
    }


    public String getDataSource()
    {
        return dataSource;
    }


    public ContainerHostState getState()
    {
        return state;
    }
}
