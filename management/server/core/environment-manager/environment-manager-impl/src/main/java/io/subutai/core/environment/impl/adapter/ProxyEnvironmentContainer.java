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
import io.subutai.common.host.Quota;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.Host;
import io.subutai.common.peer.PeerException;
import io.subutai.common.security.SshKeys;
import io.subutai.common.settings.Common;
import io.subutai.core.environment.impl.EnvironmentManagerImpl;
import io.subutai.core.environment.impl.entity.EnvironmentContainerImpl;
import io.subutai.bazaar.share.quota.ContainerQuota;
import io.subutai.bazaar.share.quota.ContainerSize;


class ProxyEnvironmentContainer extends EnvironmentContainerImpl
{
    private static final Logger LOG = LoggerFactory.getLogger( ProxyEnvironmentContainer.class );

    private static final RequestBuilder WHOAMI = new RequestBuilder( "whoami" );

    private ContainerHost proxyContainer;

    private final boolean local;


    ProxyEnvironmentContainer( JsonNode json, EnvironmentManagerImpl environmentManager, Set<String> localContainerIds )
    {
        //TODO pass env id and vlan frombazaar
        super( Common.BAZAAR_ID, json.get( "peerId" ).asText(),
                new ContainerHostInfoModel( json.get( "id" ).asText(), json.get( "hostName" ).asText(),
                        json.get( "name" ).asText(), initHostInterfaces( json ), HostArchitecture.AMD64,
                        ContainerHostState.RUNNING, null, null ), json.get( "templateId" ).asText(),
                json.get( "domainName" ).asText(), new ContainerQuota( parseSize( json ) ),
                json.get( "hostId" ).asText() );

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

            return result.hasSucceeded() && StringUtils.isNotBlank( result.getStdOut() ) && result.getStdOut()
                                                                                                  .contains( "root" );
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

        HostInterfaceModel him = new HostInterfaceModel( Common.DEFAULT_CONTAINER_INTERFACE, ip );

        Set<HostInterfaceModel> set = Sets.newHashSet();
        set.add( him );

        return new HostInterfaces( id, set );
    }


    void setProxyContainer( ContainerHost proxyContainer )
    {
        this.proxyContainer = proxyContainer;

        LOG.debug( "Set proxy: container={}, proxy={}", getId(),
                proxyContainer == null ? "no proxy" : proxyContainer.getId() );
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
        String proxyIp = proxyContainer.getIp();

        String targetHostIp = getIp();

        if ( targetHostIp.contains( "/" ) )
        {
            targetHostIp = StringUtils.substringBefore( targetHostIp, "/" );
        }

        Request req = requestBuilder.build( "id" );

        String command = String.format( "ssh root@%s %s", targetHostIp, req.getCommand().replace( "\\", "\\\\" ) );

        LOG.debug( "Command wrapped '{}' to send via {}", command, proxyIp );

        return new RequestBuilder( command ).withTimeout( requestBuilder.getTimeout() );
    }


    @Override
    public ContainerHostState getState()
    {
        if ( isLocal() )
        {
            return super.getState();
        }
        else
        {
            //TODO for remote containers obtain frombazaar metadata
            return ContainerHostState.UNKNOWN;
        }
    }


    @Override
    public Quota getRawQuota()
    {
        if ( isLocal() )
        {
            return super.getRawQuota();
        }
        else
        {
            //TODO for remote containers obtain frombazaar metadata
            return null;
        }
    }


    @Override
    public SshKeys getAuthorizedKeys() throws PeerException
    {
        if ( isLocal() )
        {
            return super.getAuthorizedKeys();
        }
        else
        {
            //TODO for remote containers obtain frombazaar metadata
            return new SshKeys();
        }
    }


    @Override
    public ContainerQuota getQuota() throws PeerException
    {
        if ( isLocal() )
        {
            return super.getQuota();
        }
        else
        {
            //TODO for remote containers obtain frombazaar metadata
            return null;
        }
    }
}
