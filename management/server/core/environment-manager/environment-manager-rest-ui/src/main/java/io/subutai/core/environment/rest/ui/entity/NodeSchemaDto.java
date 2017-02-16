package io.subutai.core.environment.rest.ui.entity;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.subutai.hub.share.quota.ContainerCpuResource;
import io.subutai.hub.share.quota.ContainerHomeResource;
import io.subutai.hub.share.quota.ContainerOptResource;
import io.subutai.hub.share.quota.ContainerQuota;
import io.subutai.hub.share.quota.ContainerRamResource;
import io.subutai.hub.share.quota.ContainerRootfsResource;
import io.subutai.hub.share.quota.ContainerSize;
import io.subutai.hub.share.quota.ContainerVarResource;
import io.subutai.hub.share.quota.Quota;


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


    public NodeSchemaDto( @JsonProperty( "name" ) final String name,
                          @JsonProperty( "quota" ) final ContainerQuotaDto quota,
                          @JsonProperty( "templateName" ) final String templateName,
                          @JsonProperty( "templateId" ) final String templateId )
    {
        this.name = name;
        this.quota = quota;
        this.templateName = templateName;
        this.templateId = templateId;
    }


    public String getName()
    {
        return name;
    }


    public ContainerQuotaDto getQuota()
    {
        return quota;
    }


    public ContainerQuota getContainerQuota()
    {
        ContainerQuota quota = new ContainerQuota( this.quota.getContainerSize() );
        if ( this.quota.getContainerSize() == ContainerSize.CUSTOM )
        {
            quota.add( new Quota( new ContainerCpuResource( this.quota.getCpu() ), 0 ) );
            quota.add( new Quota( new ContainerRamResource( this.quota.getRam() ), 0 ) );
            quota.add( new Quota( new ContainerRootfsResource( this.quota.getRoot() ), 0 ) );
            quota.add( new Quota( new ContainerHomeResource( this.quota.getHome() ), 0 ) );
            quota.add( new Quota( new ContainerOptResource( this.quota.getOpt() ), 0 ) );
            quota.add( new Quota( new ContainerVarResource( this.quota.getVar() ), 0 ) );
        }
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
}
