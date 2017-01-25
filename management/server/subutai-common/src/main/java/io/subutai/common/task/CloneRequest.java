package io.subutai.common.task;


import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import io.subutai.common.host.HostArchitecture;
import io.subutai.common.settings.Common;
import io.subutai.hub.share.quota.ContainerQuota;


public class CloneRequest
{
    private final String resourceHostId;
    private String hostname;
    private String containerName;
    private final String ip;
    private final String templateId;
    private final HostArchitecture templateArch;
    private final ContainerQuota containerQuota;


    public CloneRequest( final String resourceHostId, final String hostname, final String containerName,
                         final String ip, final String templateId, HostArchitecture templateArch,
                         final ContainerQuota containerQuota )
    {
        Preconditions.checkNotNull( resourceHostId );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hostname ) );
        Preconditions.checkNotNull( templateId );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( ip ) && ip.matches( Common.CIDR_REGEX ) );

        this.resourceHostId = resourceHostId;
        this.hostname = hostname;
        this.containerName = containerName;
        this.ip = ip;
        this.templateId = templateId;
        this.templateArch = templateArch;
        this.containerQuota = containerQuota;
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


    public void setContainerName( final String containerName )
    {
        this.containerName = containerName;
    }


    public String getIp()
    {
        return ip;
    }


    public String getTemplateId()
    {
        return templateId;
    }


    public HostArchitecture getTemplateArch()
    {
        return templateArch;
    }


    public ContainerQuota getContainerQuota()
    {
        return containerQuota;
    }


    @Override
    public String toString()
    {
        return "CloneRequest{" + "resourceHostId='" + resourceHostId + '\'' + ", hostname='" + hostname + '\''
                + ", containerName='" + containerName + '\'' + ", ip='" + ip + '\'' + ", templateId='" + templateId
                + '\'' + ", templateArch=" + templateArch + ", containerQuota=" + containerQuota + '}';
    }
}
