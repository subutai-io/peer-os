package io.subutai.core.environment.impl.adapter;


import io.subutai.common.host.ContainerHostInfoModel;
import io.subutai.common.host.ContainerHostState;
import io.subutai.common.host.HostArchitecture;
import io.subutai.common.peer.ContainerSize;
import io.subutai.core.environment.impl.entity.EnvironmentContainerImpl;


public class ProxyEnvironmentContainer extends EnvironmentContainerImpl
{
    public ProxyEnvironmentContainer( String creatorPeerId, String peerId, String nodeGroupName, ContainerHostInfoModel hostInfo, String templateName,
                                      HostArchitecture templateArch, int sshGroupId, int hostsGroupId, String domainName, ContainerSize containerSize,
                                      String resourceHostId, String containerName )
    {
        super( creatorPeerId, peerId, nodeGroupName, hostInfo, templateName, templateArch, sshGroupId, hostsGroupId, domainName, containerSize,
             resourceHostId, containerName );
    }


    @Override
    public ContainerHostState getState()
    {
        return ContainerHostState.RUNNING;
    }
}
