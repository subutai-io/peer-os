package io.subutai.bazaar.share.dto.environment;


import java.util.HashSet;
import java.util.Set;

import io.subutai.bazaar.share.quota.ContainerQuota;


public class EnvironmentNodeDto
{
    private String hostId;

    private String hostName;

    private String containerName;

    private String environmentId;

    private String ownerId;

    private String templateName;

    private String templateId;

    private String templateArch;

    private ContainerQuota containerQuota;

    private String ip;

    private String containerId;

    private ContainerStateDto state;

    private long elapsedTime;

    private int ipAddressOffset;

    private String port;

    private boolean hasDomain;

    private Set<String> sshKeys = new HashSet<>();


    public EnvironmentNodeDto()
    {
    }


    public void setHostId( final String hostId )
    {
        this.hostId = hostId;
    }


    public String getHostId()
    {
        return hostId;
    }


    public void setHostName( final String hostName )
    {
        this.hostName = hostName;
    }


    public String getHostName()
    {
        return hostName;
    }


    public void setContainerName( final String containerName )
    {
        this.containerName = containerName;
    }


    public String getContainerName()
    {
        return containerName;
    }


    public void setTemplateName( final String templateName )
    {
        this.templateName = templateName;
    }


    public String getTemplateName()
    {
        return templateName;
    }


    public String getTemplateId()
    {
        return templateId;
    }


    public void setTemplateId( final String templateId )
    {
        this.templateId = templateId;
    }


    public ContainerQuota getContainerQuota()
    {
        return containerQuota;
    }


    public void setContainerQuota( final ContainerQuota containerQuota )
    {
        this.containerQuota = containerQuota;
    }


    public void setIp( final String ip )
    {
        this.ip = ip;
    }


    public String getIp()
    {
        return ip;
    }


    public String getContainerId()
    {
        return containerId;
    }


    public void setContainerId( final String containerId )
    {
        this.containerId = containerId;
    }


    public ContainerStateDto getState()
    {
        return state;
    }


    public void setState( final ContainerStateDto state )
    {
        this.state = state;
    }


    public void setSshKeys( final Set<String> sshKeys )
    {
        this.sshKeys = sshKeys;
    }


    public Set<String> getSshKeys()
    {
        return sshKeys;
    }


    public void addSshKey( final String sshKey )
    {
        if ( !( this.sshKeys == null ) )
        {
            this.sshKeys.add( sshKey );
        }
        else
        {
            this.sshKeys = new HashSet<>();
            this.sshKeys.add( sshKey );
        }
    }


    public String getEnvironmentId()
    {
        return environmentId;
    }


    public void setEnvironmentId( final String environmentId )
    {
        this.environmentId = environmentId;
    }


    public String getOwnerId()
    {
        return ownerId;
    }


    public void setOwnerId( final String ownerId )
    {
        this.ownerId = ownerId;
    }


    public String getTemplateArch()
    {
        return templateArch;
    }


    public void setTemplateArch( final String templateArch )
    {
        this.templateArch = templateArch;
    }


    public long getElapsedTime()
    {
        return elapsedTime;
    }


    public void setElapsedTime( final long elapsedTime )
    {
        this.elapsedTime = elapsedTime;
    }


    public int getIpAddressOffset()
    {
        return ipAddressOffset;
    }


    public void setIpAddressOffset( final int ipAddressOffset )
    {
        this.ipAddressOffset = ipAddressOffset;
    }


    public String getPort()
    {
        return port;
    }


    public void setPort( final String port )
    {
        this.port = port;
    }


    public boolean isHasDomain()
    {
        return hasDomain;
    }


    public void setHasDomain( final boolean hasDomain )
    {
        this.hasDomain = hasDomain;
    }
}
