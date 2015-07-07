package io.subutai.core.env.rest;


import java.util.UUID;

import org.safehaus.subutai.common.host.ContainerHostState;


/**
 * Trimmed container for REST
 */
public class ContainerJson
{
    private UUID id;
    private UUID environmentId;
    private String hostname;
    private ContainerHostState state;
    private String ip;
    private String templateName;


    public ContainerJson( final UUID id, final UUID environmentId, final String hostname,
                          final ContainerHostState state, final String ip, final String templateName )
    {
        this.id = id;
        this.environmentId = environmentId;
        this.hostname = hostname;
        this.state = state;
        this.ip = ip;
        this.templateName = templateName;
    }


    public UUID getId()
    {
        return id;
    }


    public void setId( final UUID id )
    {
        this.id = id;
    }


    public UUID getEnvironmentId()
    {
        return environmentId;
    }


    public void setEnvironmentId( final UUID environmentId )
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
