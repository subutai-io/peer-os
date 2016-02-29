package io.subutai.common.task;


import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import io.subutai.common.host.HostArchitecture;
import io.subutai.common.peer.ContainerSize;
import io.subutai.common.settings.Common;
import io.subutai.common.task.TaskRequest;


public class CloneRequest implements TaskRequest
{
    private final String resourceHostId;
    private final String hostname;
    private final String containerName;
    private final String ip;
    private final String environmentId;
    private final String initiatorPeerId;
    private final String ownerId;
    private final String templateName;
    private final HostArchitecture templateArch;
    private final ContainerSize containerSize;


    // todo: add vlan at request creation time
    public CloneRequest( final String resourceHostId, final String hostname, final String containerName,
                         final String ip, final String environmentId, final String initiatorPeerId,
                         final String ownerId, final String templateName, HostArchitecture templateArch,
                         final ContainerSize containerSize )
    {
        Preconditions.checkNotNull( resourceHostId );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hostname ) );
        Preconditions.checkNotNull( templateName );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( ip ) && ip.matches( Common.CIDR_REGEX ) );
        //        Preconditions.checkArgument( NumUtil.isIntBetween( vlan, Common.MIN_VLAN_ID, Common.MAX_VLAN_ID ) );

        this.resourceHostId = resourceHostId;
        this.hostname = hostname;
        this.containerName = containerName;
        this.ip = ip;
        //        this.vlan = vlan;
        this.environmentId = environmentId;
        this.initiatorPeerId = initiatorPeerId;
        this.ownerId = ownerId;
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


    public String getContainerName()
    {
        return containerName;
    }


    public String getIp()
    {
        return ip;
    }

    //
    //    public Integer getVlan()
    //    {
    //        return vlan;
    //    }


    public String getEnvironmentId()
    {
        return environmentId;
    }


    public String getInitiatorPeerId()
    {
        return initiatorPeerId;
    }


    public String getOwnerId()
    {
        return ownerId;
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
        final StringBuffer sb = new StringBuffer( "CloneRequest{" );
        sb.append( "resourceHostId='" ).append( resourceHostId ).append( '\'' );
        sb.append( ", hostname='" ).append( hostname ).append( '\'' );
        sb.append( ", containerName='" ).append( containerName ).append( '\'' );
        sb.append( ", ip='" ).append( ip ).append( '\'' );
        sb.append( ", environmentId='" ).append( environmentId ).append( '\'' );
        sb.append( ", initiatorPeerId='" ).append( initiatorPeerId ).append( '\'' );
        sb.append( ", ownerId='" ).append( ownerId ).append( '\'' );
        sb.append( ", templateName='" ).append( templateName ).append( '\'' );
        sb.append( ", templateArch=" ).append( templateArch );
        sb.append( ", containerSize=" ).append( containerSize );
        sb.append( '}' );
        return sb.toString();
    }
}
