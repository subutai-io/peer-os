package io.subutai.hub.share.dto.environment;


import java.util.HashSet;
import java.util.Set;


public class EnvironmentNodeDto
{
    private String hostId;

    private String hostName;

    private String containerName;

    private String environmentId;

    private String ownerId;

    private String templateName;

    private String templateArch;

    private String containerSize;

    private String ip;

    private String containerId;

    private ContainerStateDto state;

    private long elapsedTime;

    private int ipAddressOffset;

    private Set<String> sshKeys = new HashSet<>();


    public EnvironmentNodeDto()
    {
    }


    public String getHostId()
    {
        return hostId;
    }


    public void setHostId( final String hostId )
    {
        this.hostId = hostId;
    }


    public String getHostName()
    {
        return hostName;
    }


    public void setHostName( final String hostName )
    {
        this.hostName = hostName;
    }


    public String getContainerName()
    {
        return containerName;
    }


    public void setContainerName( final String containerName )
    {
        this.containerName = containerName;
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


    public String getTemplateName()
    {
        return templateName;
    }


    public void setTemplateName( final String templateName )
    {
        this.templateName = templateName;
    }


    public String getTemplateArch()
    {
        return templateArch;
    }


    public void setTemplateArch( final String templateArch )
    {
        this.templateArch = templateArch;
    }


    public String getContainerSize()
    {
        return containerSize;
    }


    public void setContainerSize( final String containerSize )
    {
        this.containerSize = containerSize;
    }


    public int getIpAddressOffset()
    {
        return ipAddressOffset;
    }


    public void setIpAddressOffset( final int ipAddressOffset )
    {
        this.ipAddressOffset = ipAddressOffset;
    }


    public String getIp()
    {
        return ip;
    }


    public void setIp( final String ip )
    {
        this.ip = ip;
    }


    public String getContainerId()
    {
        return containerId;
    }


    public void setContainerId( final String containerId )
    {
        this.containerId = containerId;
    }


    public long getElapsedTime()
    {
        return elapsedTime;
    }


    public void setElapsedTime( final long elapsedTime )
    {
        this.elapsedTime = elapsedTime;
    }


    public ContainerStateDto getState()
    {
        return state;
    }


    public void setState( final ContainerStateDto state )
    {
        this.state = state;
    }


    public Set<String> getSshKeys()
    {
        return sshKeys;
    }


    public void setSshKeys( final Set<String> sshKeys )
    {
        this.sshKeys = sshKeys;
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
}
