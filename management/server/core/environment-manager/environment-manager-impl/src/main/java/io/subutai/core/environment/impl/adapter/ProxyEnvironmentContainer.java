package io.subutai.core.environment.impl.adapter;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.host.ContainerHostInfoModel;
import io.subutai.common.host.ContainerHostState;
import io.subutai.common.host.HostArchitecture;
import io.subutai.common.peer.ContainerSize;
import io.subutai.core.environment.impl.entity.EnvironmentContainerImpl;


public class ProxyEnvironmentContainer extends EnvironmentContainerImpl
{
    private final Logger log = LoggerFactory.getLogger( getClass() );


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


    @Override
    public CommandResult execute( RequestBuilder requestBuilder ) throws CommandException
    {
        log.debug( "requestBuilder: {}", requestBuilder );

        return getPeer().execute( requestBuilder, this );
    }
}
