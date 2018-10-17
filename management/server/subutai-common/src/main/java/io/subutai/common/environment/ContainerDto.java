package io.subutai.common.environment;


import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.subutai.common.host.ContainerHostState;
import io.subutai.bazaar.share.quota.ContainerSize;


/**
 * Container DTO
 */
@JsonIgnoreProperties( ignoreUnknown = true )
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
    @JsonProperty( "templateId" )
    private String templateId;
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
    @JsonProperty( "rhId" )
    private String rhId;
    @JsonProperty( "quota" )
    private ContainerQuotaDto quota;


    // Where environment of container created: subutai, bazaar
    @JsonProperty( "dataSource" )
    private String dataSource;

    @JsonProperty( "containerName" )
    private String containerName;


    public ContainerDto( @JsonProperty( "id" ) final String id,
                         @JsonProperty( "environmentId" ) final String environmentId,
                         @JsonProperty( "hostname" ) final String hostname, @JsonProperty( "ip" ) final String ip,
                         @JsonProperty( "templateName" ) final String templateName,
                         @JsonProperty( "type" ) final ContainerSize type, @JsonProperty( "arch" ) final String arch,
                         @JsonProperty( "tags" ) final Set<String> tags, @JsonProperty( "peerId" ) final String peerId,
                         @JsonProperty( "hostId" ) final String hostId, @JsonProperty( "local" ) boolean local,
                         @JsonProperty( "dataSource" ) String dataSource,
                         @JsonProperty( "state" ) ContainerHostState state,
                         @JsonProperty( "templateId" ) String templateId,
                         @JsonProperty( "containerName" ) String containerName, @JsonProperty( "rhId" ) String rhId )
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
        this.templateId = templateId;
        this.dataSource = dataSource;
        this.state = state;
        this.containerName = containerName;
        this.rhId = rhId;
    }


    public String getId()
    {
        return id;
    }


    public String getEnvironmentId()
    {
        return environmentId;
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


    public String getTemplateName()
    {
        return templateName;
    }


    public void setTemplateName( final String templateName )
    {
        this.templateName = templateName;
    }


    public String getTemplateId()
    {
        return templateId;
    }


    public ContainerSize getType()
    {
        return type;
    }


    public String getArch()
    {
        return arch;
    }


    public Set<String> getTags()
    {
        return tags;
    }


    public String getPeerId()
    {
        return peerId;
    }


    public String getHostId()
    {
        return hostId;
    }


    public boolean isLocal()
    {
        return local;
    }


    public ContainerHostState getState()
    {
        return state;
    }


    public String getDataSource()
    {
        return dataSource;
    }


    public String getContainerName()
    {
        return containerName;
    }


    public void setContainerName( final String containerName )
    {
        this.containerName = containerName;
    }


    public String getRhId()
    {
        return rhId;
    }


    public ContainerQuotaDto getQuota()
    {
        return quota;
    }


    public void setQuota( final ContainerQuotaDto quota )
    {
        this.quota = quota;
    }
}
