package io.subutai.core.environment.impl.adapter;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.Request;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.host.ContainerHostInfoModel;
import io.subutai.common.host.ContainerHostState;
import io.subutai.common.host.HostArchitecture;
import io.subutai.common.peer.ContainerSize;
import io.subutai.common.peer.Host;
import io.subutai.core.environment.impl.entity.EnvironmentContainerImpl;


public class ProxyEnvironmentContainer extends EnvironmentContainerImpl
{
    private final Logger log = LoggerFactory.getLogger( getClass() );

    private Host proxyContainer;


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


    void setProxyContainer( Host proxyContainer )
    {
        this.proxyContainer = proxyContainer;
    }


    @Override
    public CommandResult execute( RequestBuilder requestBuilder ) throws CommandException
    {
        Host host = this;

        // If this is a remote host a command is sent via a proxyContainer b/c the remote host is not directly accessible from current peer.
        if ( proxyContainer != null )
        {
            requestBuilder = wrapForProxy( requestBuilder );

            host = proxyContainer;
        }

        return getPeer().execute( requestBuilder, host );
    }


    private RequestBuilder wrapForProxy( RequestBuilder requestBuilder )
    {
        String proxyIp = proxyContainer.getHostInterfaces().getAll().iterator().next().getIp();

        String targetHostIp = getHostInterfaces().getAll().iterator().next().getIp();

        Request req = requestBuilder.build( "id" );

        String command = String.format( "ssh root@%s %s", targetHostIp, req.getCommand() );

        log.debug( "Command wrapped '{}' to send via {}", command, proxyIp );

        return new RequestBuilder( command );
    }

}
