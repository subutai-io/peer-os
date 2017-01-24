package io.subutai.common.task;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.host.HostArchitecture;
import io.subutai.hub.share.quota.ContainerSize;


public class CloneResponse implements TaskResponse
{
    protected static final Logger LOG = LoggerFactory.getLogger( CloneResponse.class );

    private String resourceHostId;
    private String hostname;
    private String templateId;
    private HostArchitecture templateArch;
    private String containerName;
    private String ip;
    private String containerId;
    private long elapsedTime;
    private ContainerSize containerSize;


    public CloneResponse( final String resourceHostId, final String hostname, final String containerName,
                          final String templateId, final HostArchitecture templateArch, final String ip,
                          final String containerId, final long elapsedTime, final ContainerSize containerSize )
    {
        this.resourceHostId = resourceHostId;
        this.hostname = hostname;
        this.templateId = templateId;
        this.templateArch = templateArch;
        this.containerName = containerName;
        this.ip = ip;
        this.containerId = containerId;
        this.elapsedTime = elapsedTime;
        this.containerSize = containerSize;
    }


    @Override
    public String getResourceHostId()
    {
        return resourceHostId;
    }


    public String getHostname()
    {
        return hostname;
    }


    public String getContainerName()
    {
        return containerName;
    }


    public String getIp()
    {
        return ip.split( "/" )[0];
    }


    public String getContainerId()
    {
        return containerId;
    }


    public String getTemplateId()
    {
        return templateId;
    }


    public HostArchitecture getTemplateArch()
    {
        return templateArch;
    }


    @Override
    public long getElapsedTime()
    {
        return elapsedTime;
    }


    public ContainerSize getContainerSize()
    {
        return containerSize;
    }


    @Override
    public String toString()
    {
        return "CloneResponse{" + "resourceHostId='" + resourceHostId + '\'' + ", hostname='" + hostname + '\''
                + ", templateId='" + templateId + '\'' + ", templateArch=" + templateArch + ", containerName='"
                + containerName + '\'' + ", ip='" + ip + '\'' + ", containerId='" + containerId + '\'' + '}';
    }
}
