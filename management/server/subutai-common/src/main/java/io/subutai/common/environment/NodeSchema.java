package io.subutai.common.environment;


import com.fasterxml.jackson.annotation.JsonProperty;

import io.subutai.bazaar.share.quota.ContainerQuota;


/**
 * Node group schema
 */
public class NodeSchema
{
    @JsonProperty( "name" )
    private String name;

    @JsonProperty( "quota" )
    private ContainerQuota quota;

    @JsonProperty( "templateName" )
    private String templateName;

    @JsonProperty( "templateId" )
    private String templateId;


    public NodeSchema( @JsonProperty( "name" ) final String name, @JsonProperty( "quota" ) final ContainerQuota quota,
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


    public ContainerQuota getQuota()
    {
        return quota;
    }


    @Deprecated
    //This method is deprecated. Use getTemplateId instead
    public String getTemplateName()
    {
        return templateName;
    }


    public String getTemplateId()
    {
        return templateId;
    }
}
