package io.subutai.core.environment.rest;


import io.subutai.bazaar.share.quota.ContainerSize;


public class NodeDto
{
    private String hostname;

    private String templateName;

    private String templateId;

    private ContainerSize size;

    private String peerId;

    private String resourceHostId;


    public String getTemplateName()
    {
        return templateName;
    }


    public String getTemplateId()
    {
        return templateId;
    }


    public String getHostname()
    {
        return hostname;
    }


    public ContainerSize getSize()
    {
        return size;
    }


    public String getPeerId()
    {
        return peerId;
    }


    public String getResourceHostId()
    {
        return resourceHostId;
    }
}