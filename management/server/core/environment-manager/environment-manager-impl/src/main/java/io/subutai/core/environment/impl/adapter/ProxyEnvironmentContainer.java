package io.subutai.core.environment.impl.adapter;


import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.StringUtils;

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
import io.subutai.core.environment.impl.EnvironmentManagerImpl;
import io.subutai.core.environment.impl.entity.EnvironmentContainerImpl;


class ProxyEnvironmentContainer extends EnvironmentContainerImpl
{
    private static final Logger LOG = LoggerFactory.getLogger( ProxyEnvironmentContainer.class );

    private static final RequestBuilder WHOAMI = new RequestBuilder( "whoami" );

    private Host proxyContainer;

    private final boolean local;


    ProxyEnvironmentContainer( JsonNode json, EnvironmentManagerImpl environmentManager, Set<String> localContainerIds )
    {
        super( "hub", json.get( "peerId" ).asText(),
                new ContainerHostInfoModel( json.get( "id" ).asText(), json.get( "hostName" ).asText(),
                        json.get( "name" ).asText(), initHostInterfaces( json ), HostArchitecture.AMD64,
                        ContainerHostState.RUNNING ), json.get( "templateName" ).asText(), HostArchitecture.AMD64, 0, 0,
                json.get( "domainName" ).asText(), parseSize( json ), json.get( "hostId" ).asText(),
                json.get( "name" ).asText() );

        local = localContainerIds.contains( getId() );

        setEnvironmentManager( environmentManager );
    }


    @Override
    public boolean isLocal()
    {
        return local;
    }


    @Override
    public boolean isConnected()
    {
        return isLocal() ? super.isConnected() : isRemoteContainerConnected();
    }


    private boolean isRemoteContainerConnected()
    {
        try
        {
            CommandResult result = execute( WHOAMI );

            return result.getExitCode() == 0 && StringUtils.isNotBlank( result.getStdOut() ) && result.getStdOut()
                                                                                                      .contains(
                                                                                                              "root" );
        }
        catch ( CommandException e )
        {
            LOG.error( "Error to check if remote container is connected: ", e );

            return false;
        }
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

        LOG.debug( "Set proxy: container={}, proxy={}", getId(), proxyContainer.getId() );
    }


    @Override
    public CommandResult execute( RequestBuilder requestBuilder ) throws CommandException
    {
        Host host = this;

        // If this is a remote host then the command is sent via a proxyContainer
        // b/c the remote host is not directly accessible from the current peer.
        if ( !isLocal() )
        {
            if ( proxyContainer != null )
            {
                requestBuilder = wrapForProxy( requestBuilder );

                host = proxyContainer;
            }
            else
            {
                throw new CommandException(
                        "Please start at least one local container from this environment to be able to execute "
                                + "commands on remote ones" );
            }
        }

        return host.getPeer().execute( requestBuilder, host );
    }


    private RequestBuilder wrapForProxy( RequestBuilder requestBuilder )
    {
        String proxyIp = proxyContainer.getHostInterfaces().getAll().iterator().next().getIp();

        String targetHostIp = getHostInterfaces().getAll().iterator().next().getIp();

        if ( targetHostIp.contains( "/" ) )
        {
            targetHostIp = StringUtils.substringBefore( targetHostIp, "/" );
        }

        Request req = requestBuilder.build( "id" );

        String command = String.format( "ssh root@%s %s", targetHostIp, req.getCommand() );

        LOG.debug( "Command wrapped '{}' to send via {}", command, proxyIp );

        return new RequestBuilder( command ).withTimeout( requestBuilder.getTimeout() );
    }
}
