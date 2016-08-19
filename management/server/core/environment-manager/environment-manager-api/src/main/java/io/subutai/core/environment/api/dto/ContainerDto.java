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
                         @JsonProperty( "state" ) ContainerHostState state,
                         @JsonProperty( "templateId" ) String templateId )
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
    }
}
