package io.subutai.core.environment.rest.ui.entity;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.subutai.common.environment.ContainerQuotaDto;


@JsonIgnoreProperties( ignoreUnknown = true )
public class NodeSchemaDto
{
    @JsonProperty( "name" )
    private String name;

    @JsonProperty( "quota" )
    private ContainerQuotaDto quota;

    @JsonProperty( "templateName" )
    private String templateName;

    @JsonProperty( "templateId" )
    private String templateId;

    @JsonProperty( "peerId" )
    private String peerId;

    @JsonProperty( "hostId" )
    private String hostId;


    public NodeSchemaDto( @JsonProperty( "name" ) final String name,
                          @JsonProperty( "quota" ) final ContainerQuotaDto quota,
                          @JsonProperty( "templateName" ) final String templateName,
                          @JsonProperty( "templateId" ) final String templateId,
                          @JsonProperty( "peerId" ) final String peerId, @JsonProperty( "hostId" ) final String hostId )
    {
        this.name = name;
        this.quota = quota;
        this.templateName = templateName;
        this.templateId = templateId;
        this.peerId = peerId;
        this.hostId = hostId;
    }


    public String getName()
    {
        return name;
    }


    public ContainerQuotaDto getQuota()
    {
        return quota;
    }


    public String getTemplateName()
    {
        return templateName;
    }


    public String getTemplateId()
    {
        return templateId;
    }


    public String getPeerId()
    {
        return peerId;
    }


    public String getHostId()
    {
        return hostId;
    }
}
