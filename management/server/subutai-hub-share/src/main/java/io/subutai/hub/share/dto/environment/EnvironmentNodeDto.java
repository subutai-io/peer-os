package io.subutai.hub.share.dto.environment;


import java.util.HashSet;
import java.util.Set;

//TODO:TEMPLATE add templateId
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


    public String getHostName()
    {
        return hostName;
    }


    public String getContainerName()
    {
        return containerName;
    }


    public String getTemplateName()
    {
        return templateName;
    }


    public String getContainerSize()
    {
        return containerSize;
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
}
