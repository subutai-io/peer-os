package io.subutai.core.environment.rest;


import io.subutai.common.host.ContainerHostState;
import io.subutai.common.peer.ContainerType;


/**
 * Trimmed container for REST
 */
public class ContainerDto
{
    private String id;
    private String environmentId;
    private String hostname;
    private ContainerHostState state;
    private ContainerType containerType;
    private String ip;
    private String templateName;


    public ContainerDto( final String id, final String environmentId, final String hostname,
                         final ContainerType containerType, final ContainerHostState state, final String ip,
                         final String templateName )
    {
        this.id = id;
        this.environmentId = environmentId;
        this.hostname = hostname;
        this.containerType = containerType;
        this.state = state;
        this.ip = ip;
        this.templateName = templateName;
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


    public ContainerHostState getState()
    {
        return state;
    }


    public void setState( final ContainerHostState state )
    {
        this.state = state;
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
}
