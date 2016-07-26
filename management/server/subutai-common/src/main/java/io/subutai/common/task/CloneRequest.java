package io.subutai.common.task;


import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import io.subutai.common.host.HostArchitecture;
import io.subutai.common.peer.ContainerSize;
import io.subutai.common.settings.Common;


public class CloneRequest implements TaskRequest
{
    private final String resourceHostId;
    private String hostname;
    private final String containerName;
    private final String ip;
    private final String templateName;
    private final HostArchitecture templateArch;
    private final ContainerSize containerSize;


    public CloneRequest( final String resourceHostId, final String hostname, final String containerName,
                         final String ip, final String templateName, HostArchitecture templateArch,
                         final ContainerSize containerSize )
    {
        Preconditions.checkNotNull( resourceHostId );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hostname ) );
        Preconditions.checkNotNull( templateName );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( ip ) && ip.matches( Common.CIDR_REGEX ) );

        this.resourceHostId = resourceHostId;
        this.hostname = hostname;
        this.containerName = containerName;
        this.ip = ip;
        this.templateName = templateName;
        this.templateArch = templateArch;
        this.containerSize = containerSize;
    }


    public String getResourceHostId()
    {
        return resourceHostId;
    }


    public String getHostname()
    {
        return hostname;
    }


    public void setHostname( final String hostname )
    {
        this.hostname = hostname;
    }


    public String getContainerName()
    {
        return containerName;
    }


    public String getIp()
    {
        return ip;
    }


    public String getTemplateName()
    {
        return templateName;
    }


    public HostArchitecture getTemplateArch()
    {
        return templateArch;
    }


    public ContainerSize getContainerSize()
    {
        return containerSize;
    }


    @Override
    public String toString()
    {
        return "CloneRequest{" + "resourceHostId='" + resourceHostId + '\'' + ", hostname='" + hostname + '\''
                + ", containerName='" + containerName + '\'' + ", ip='" + ip + '\'' + ", templateName='" + templateName
                + '\'' + ", templateArch=" + templateArch + ", containerSize=" + containerSize + '}';
    }
}
