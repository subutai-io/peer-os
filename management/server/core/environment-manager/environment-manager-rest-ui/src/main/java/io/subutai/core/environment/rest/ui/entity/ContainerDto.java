package io.subutai.core.environment.rest.ui.entity;


import java.util.Set;

import io.subutai.common.host.ContainerHostState;
import io.subutai.common.peer.ContainerSize;


/**
 * Trimmed container for REST
 */
public class ContainerDto
{
    private String id;
    private String environmentId;
    private String hostname;
    private String ip;
    private String templateName;
    private ContainerSize type;
    private String arch;
    private Set<String> tags;

    private String peerId;
    private String hostId;


    public ContainerDto( final String id, final String environmentId, final String hostname,
                         final String ip, final String templateName,
                         final ContainerSize type, final String arch, final Set<String> tags, final String peerId,
                         final String hostId )
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


    public ContainerSize getType()
    {
        return type;
    }


    public void setType( final ContainerSize type )
    {
        this.type = type;
    }


    public Set<String> getTags()
    {
        return tags;
    }


    public void setTags( final Set<String> tags )
    {
        this.tags = tags;
    }


    public String getArch()
    {
        return arch;
    }


    public void setArch( final String arch )
    {
        this.arch = arch;
    }


    public String getPeerId()
    {
        return peerId;
    }


    public void setPeerId( String peerId )
    {
        this.peerId = peerId;
    }


    public String getHostId()
    {
        return hostId;
    }


    public void setHostId( String hostId )
    {
        this.hostId = hostId;
    }
}
