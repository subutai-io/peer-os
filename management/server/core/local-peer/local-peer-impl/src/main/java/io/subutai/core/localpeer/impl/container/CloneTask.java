package io.subutai.core.localpeer.impl.container;


import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import io.subutai.common.command.CommandResult;
import io.subutai.common.command.CommandResultParser;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.host.HostInfo;
import io.subutai.common.peer.ContainerSize;
import io.subutai.common.peer.Host;
import io.subutai.common.peer.PeerException;
import io.subutai.common.peer.ResourceHost;
import io.subutai.common.protocol.TemplateKurjun;
import io.subutai.common.quota.ContainerQuota;
import io.subutai.common.quota.ContainerResource;
import io.subutai.common.settings.Common;
import io.subutai.common.util.NumUtil;
import io.subutai.common.util.ServiceLocator;
import io.subutai.core.hostregistry.api.HostDisconnectedException;
import io.subutai.core.hostregistry.api.HostRegistry;
import io.subutai.common.task.Command;
import io.subutai.common.task.CommandBatch;
import io.subutai.core.localpeer.impl.LocalPeerImpl;
import io.subutai.core.localpeer.impl.entity.ContainerHostEntity;
import io.subutai.core.registration.api.RegistrationManager;


public class CloneTask extends DaemonTask<HostInfo>
{
    protected static final Logger LOG = LoggerFactory.getLogger( CloneTask.class );

    private static final int CLONE_TIMEOUT = 60 * 10; // 10 min
    private final ResourceHost resourceHost;
    private final String hostname;
    private final TemplateKurjun template;
    private final String ip;
    private final int vlan;
    private final ContainerQuota quota;
    private final String environmentId;
    private final LocalPeerImpl localPeer;
    private HostRegistry hostRegistry;
    private String ownerId;
    private String initiatorPeerId;


    public CloneTask( LocalPeerImpl localPeer, HostRegistry hostRegistry, final ResourceHost resourceHost,
                      final TemplateKurjun template, final String hostname, final String environmentId,
                      final String ownerId, final String initiatorPeerId, final ContainerQuota quota, final String ip,
                      final int vlan )
    {
        Preconditions.checkNotNull( resourceHost );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hostname ) );
        Preconditions.checkNotNull( template );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( ip ) && ip.matches( Common.CIDR_REGEX ) );
        Preconditions.checkArgument( NumUtil.isIntBetween( vlan, Common.MIN_VLAN_ID, Common.MAX_VLAN_ID ) );

        this.localPeer = localPeer;
        this.hostRegistry = hostRegistry;
        this.resourceHost = resourceHost;
        this.template = template;
        this.hostname = hostname;
        this.environmentId = environmentId;
        this.ownerId = ownerId;
        this.initiatorPeerId = initiatorPeerId;
        this.ip = ip;
        this.vlan = vlan;
        this.quota = quota;
    }


    public static RegistrationManager getRegistrationManager() throws NamingException
    {

        return ServiceLocator.getServiceNoCache( RegistrationManager.class );
    }


    public CommandBatch getCommandBatch() throws Exception
    {
        CommandBatch result = new CommandBatch();

        Command cloneAction = new Command( "clone",
                Lists.newArrayList( template.getName(), hostname, "-i", String.format( "\"%s %s\"", ip, vlan ), "-e",
                        environmentId, "-t",
                        getRegistrationManager().generateContainerTTLToken( ( CLONE_TIMEOUT + 10 ) * 1000L )
                                                .getToken() ) );

        result.addCommand( cloneAction );

        for ( ContainerResource r : quota.getAllResources() )
        {

            Command quotaCommand = new Command( "quota" );
            quotaCommand.addArgument( hostname );
            quotaCommand.addArgument( r.getContainerResourceType().getKey() );
            quotaCommand.addArgument( "-s" );
            quotaCommand.addArgument( r.getWriteValue() );
            result.addCommand( quotaCommand );
        }

        return result;
    }


    @Override
    public Host getHost()
    {
        return resourceHost;
    }

    @Override
    public HostInfo lookupResult()
    {
        HostInfo result = null;
        try
        {
            result = hostRegistry.getContainerHostInfoByHostname( hostname );
        }
        catch ( HostDisconnectedException e )
        {
            //ignore
        }

        return result;
    }

    @Override
    public int getTimeout()
    {
        return CLONE_TIMEOUT;
    }


    @Override
    public boolean isSequential()
    {
        return true;
    }


    public void onSuccess()
    {
        ContainerHostEntity containerHostEntity =
                new ContainerHostEntity( result.getId(), result, template.getName(), template.getArchitecture() );
        containerHostEntity.setEnvironmentId( environmentId );
        containerHostEntity.setOwnerId( ownerId );
        containerHostEntity.setInitiatorPeerId( initiatorPeerId );
        //todo: containerSize
        containerHostEntity.setContainerSize( ContainerSize.MEDIUM );

        try
        {
            localPeer.registerContainer( resourceHost, containerHostEntity );
        }
        catch ( PeerException e )
        {
            LOG.error( "Error on registering container.", e );
        }
    }
}
