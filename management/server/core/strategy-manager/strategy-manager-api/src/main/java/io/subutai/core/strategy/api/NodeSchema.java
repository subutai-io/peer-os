package io.subutai.core.strategy.api;


import io.subutai.common.peer.ContainerSize;


/**
 * Node group schema
 */
public class NodeSchema
{
    private String name;
    private ContainerSize size;
    private String templateName;


    public NodeSchema( final String name, final ContainerSize size, final String templateName )
    {
        this.name = name;
        this.size = size;
        this.templateName = templateName;
    }


    public String getName()
    {
        return name;
    }


    public ContainerSize getSize()
    {
        return size;
    }


    public String getTemplateName()
    {
        return templateName;
    }
}
