package io.subutai.common.environment;


import com.fasterxml.jackson.annotation.JsonProperty;

import io.subutai.common.peer.ContainerSize;


/**
 * Node group schema
 */
public class NodeSchema
{
    @JsonProperty( "name" )
    private String name;

    @JsonProperty( "size" )
    private ContainerSize size;

    @JsonProperty( "templateName" )
    private String templateName;

    @JsonProperty( "templateId" )
    private String templateId;


    public NodeSchema( @JsonProperty( "name" ) final String name, @JsonProperty( "size" ) final ContainerSize size,
                       @JsonProperty( "templateName" ) final String templateName,
                       @JsonProperty( "templateId" ) final String templateId )
    {
        this.name = name;
        this.size = size;
        this.templateName = templateName;
        this.templateId = templateId;
    }


    public String getName()
    {
        return name;
    }


    public ContainerSize getSize()
    {
        return size;
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
