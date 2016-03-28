package io.subutai.core.environment.impl.adapter;


import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Sets;

import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.Request;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.host.ContainerHostInfoModel;
import io.subutai.common.host.ContainerHostState;
import io.subutai.common.host.HostArchitecture;
import io.subutai.common.host.HostInterfaceModel;
import io.subutai.common.host.HostInterfaces;
import io.subutai.common.peer.ContainerSize;
import io.subutai.common.peer.Host;
import io.subutai.core.environment.impl.entity.EnvironmentContainerImpl;


class ProxyEnvironmentContainer extends EnvironmentContainerImpl
{
    private static final Logger LOG = LoggerFactory.getLogger( ProxyEnvironmentContainer.class );

    private Host proxyContainer;


    ProxyEnvironmentContainer( JsonNode json, String containerHostId )
    {
        super(
                "hub",
                json.get( "peerId" ).asText(),
                json.get( "hostName" ).asText(),

                new ContainerHostInfoModel(
                        containerHostId,
                        json.get( "id" ).asText(),
                        initHostInterfaces( json ),
                        HostArchitecture.AMD64,
                        ContainerHostState.RUNNING
                ),

                json.get( "templateName" ).asText(),
                HostArchitecture.AMD64, 0, 0,
                json.get( "domainName" ).asText(),
                parseSize( json ),
                json.get( "hostId" ).asText(),
                json.get( "name" ).asText()
        );
    }


    private static ContainerSize parseSize( JsonNode json )
    {
        String size = json.get( "type" ).asText();

        return ContainerSize.valueOf( size );
    }


    private static HostInterfaces initHostInterfaces( JsonNode json )
    {
        String ip = json.get( "ipAddress" ).asText();
        String id = json.get( "id" ).asText();

        HostInterfaceModel him = new HostInterfaceModel( "eth0", ip );

        Set<HostInterfaceModel> set = Sets.newHashSet();
        set.add( him );

        return new HostInterfaces( id, set );
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

        LOG.debug( "Command wrapped '{}' to send via {}", command, proxyIp );

        return new RequestBuilder( command );
    }

}
