package io.subutai.core.localpeer.impl;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;

import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.karaf.bundle.core.BundleState;
import org.apache.karaf.bundle.core.BundleStateService;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import io.subutai.bazaar.share.dto.metrics.HostMetricsDto;
import io.subutai.bazaar.share.parser.CommonResourceValueParser;
import io.subutai.bazaar.share.quota.ContainerCpuResource;
import io.subutai.bazaar.share.quota.ContainerDiskResource;
import io.subutai.bazaar.share.quota.ContainerQuota;
import io.subutai.bazaar.share.quota.ContainerRamResource;
import io.subutai.bazaar.share.quota.ContainerResource;
import io.subutai.bazaar.share.quota.ContainerResourceFactory;
import io.subutai.bazaar.share.quota.ContainerSize;
import io.subutai.bazaar.share.quota.Quota;
import io.subutai.bazaar.share.quota.QuotaException;
import io.subutai.bazaar.share.resource.ByteUnit;
import io.subutai.bazaar.share.resource.ContainerResourceType;
import io.subutai.bazaar.share.resource.CpuResource;
import io.subutai.bazaar.share.resource.DiskResource;
import io.subutai.bazaar.share.resource.HostResources;
import io.subutai.bazaar.share.resource.PeerResources;
import io.subutai.bazaar.share.resource.RamResource;
import io.subutai.bazaar.share.resource.ResourceValue;
import io.subutai.common.command.CommandCallback;
import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.CommandUtil;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.dao.DaoManager;
import io.subutai.common.environment.CloneContainerTask;
import io.subutai.common.environment.Containers;
import io.subutai.common.environment.CreateEnvironmentContainersRequest;
import io.subutai.common.environment.CreateEnvironmentContainersResponse;
import io.subutai.common.environment.HostAddresses;
import io.subutai.common.environment.Node;
import io.subutai.common.environment.Nodes;
import io.subutai.common.environment.PeerTemplatesDownloadProgress;
import io.subutai.common.environment.PeerTemplatesUploadProgress;
import io.subutai.common.environment.PrepareTemplatesRequest;
import io.subutai.common.environment.PrepareTemplatesResponse;
import io.subutai.common.environment.RhP2pIp;
import io.subutai.common.environment.RhTemplatesDownloadProgress;
import io.subutai.common.environment.RhTemplatesUploadProgress;
import io.subutai.common.exception.DaoException;
import io.subutai.common.host.ContainerHostInfo;
import io.subutai.common.host.ContainerHostInfoModel;
import io.subutai.common.host.ContainerHostState;
import io.subutai.common.host.HostId;
import io.subutai.common.host.HostInfo;
import io.subutai.common.host.HostInterfaceModel;
import io.subutai.common.host.HostInterfaces;
import io.subutai.common.host.ResourceHostInfo;
import io.subutai.common.host.ResourceHostInfoModel;
import io.subutai.common.metric.HistoricalMetrics;
import io.subutai.common.metric.QuotaAlertValue;
import io.subutai.common.metric.ResourceHostMetric;
import io.subutai.common.metric.ResourceHostMetrics;
import io.subutai.common.network.NetworkResource;
import io.subutai.common.network.NetworkResourceImpl;
import io.subutai.common.network.ProxyLoadBalanceStrategy;
import io.subutai.common.network.ReservedNetworkResources;
import io.subutai.common.network.SshTunnel;
import io.subutai.common.network.UsedNetworkResources;
import io.subutai.common.peer.AlertEvent;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.ContainerId;
import io.subutai.common.peer.ContainerInfo;
import io.subutai.common.peer.EnvironmentId;
import io.subutai.common.peer.FitCheckResult;
import io.subutai.common.peer.Host;
import io.subutai.common.peer.HostNotFoundException;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.LocalPeerEventListener;
import io.subutai.common.peer.Payload;
import io.subutai.common.peer.PeerException;
import io.subutai.common.peer.PeerId;
import io.subutai.common.peer.PeerInfo;
import io.subutai.common.peer.PeerPolicy;
import io.subutai.common.peer.RegistrationStatus;
import io.subutai.common.peer.RequestListener;
import io.subutai.common.peer.ResourceHost;
import io.subutai.common.peer.ResourceHostCapacity;
import io.subutai.common.peer.ResourceHostException;
import io.subutai.common.protocol.CustomProxyConfig;
import io.subutai.common.protocol.Disposable;
import io.subutai.common.protocol.P2PConfig;
import io.subutai.common.protocol.P2PCredentials;
import io.subutai.common.protocol.P2pIps;
import io.subutai.common.protocol.Template;
import io.subutai.common.security.PublicKeyContainer;
import io.subutai.common.security.SshEncryptionType;
import io.subutai.common.security.SshKey;
import io.subutai.common.security.SshKeys;
import io.subutai.common.security.crypto.pgp.KeyPair;
import io.subutai.common.security.crypto.pgp.PGPKeyUtil;
import io.subutai.common.security.objects.KeyTrustLevel;
import io.subutai.common.security.objects.Ownership;
import io.subutai.common.security.objects.PermissionObject;
import io.subutai.common.security.objects.SecurityKeyType;
import io.subutai.common.security.objects.UserType;
import io.subutai.common.security.relation.RelationLink;
import io.subutai.common.security.relation.RelationLinkDto;
import io.subutai.common.security.relation.RelationManager;
import io.subutai.common.security.relation.model.Relation;
import io.subutai.common.security.relation.model.RelationInfoMeta;
import io.subutai.common.security.relation.model.RelationMeta;
import io.subutai.common.security.relation.model.RelationStatus;
import io.subutai.common.settings.Common;
import io.subutai.common.task.CloneRequest;
import io.subutai.common.util.CollectionUtil;
import io.subutai.common.util.ExceptionUtil;
import io.subutai.common.util.HostUtil;
import io.subutai.common.util.P2PUtil;
import io.subutai.common.util.ServiceLocator;
import io.subutai.common.util.StringUtil;
import io.subutai.common.util.TaskUtil;
import io.subutai.common.util.UnitUtil;
import io.subutai.core.executor.api.CommandExecutor;
import io.subutai.core.hostregistry.api.HostDisconnectedException;
import io.subutai.core.hostregistry.api.HostListener;
import io.subutai.core.hostregistry.api.HostRegistry;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.model.User;
import io.subutai.core.localpeer.impl.binding.BatchOutput;
import io.subutai.core.localpeer.impl.binding.Commands;
import io.subutai.core.localpeer.impl.binding.QuotaOutput;
import io.subutai.core.localpeer.impl.command.CommandRequestListener;
import io.subutai.core.localpeer.impl.container.CreateEnvironmentContainersRequestListener;
import io.subutai.core.localpeer.impl.container.ImportTemplateTask;
import io.subutai.core.localpeer.impl.container.PrepareTemplateRequestListener;
import io.subutai.core.localpeer.impl.dao.NetworkResourceDaoImpl;
import io.subutai.core.localpeer.impl.dao.ResourceHostDataService;
import io.subutai.core.localpeer.impl.entity.AbstractSubutaiHost;
import io.subutai.core.localpeer.impl.entity.ContainerHostEntity;
import io.subutai.core.localpeer.impl.entity.NetworkResourceEntity;
import io.subutai.core.localpeer.impl.entity.ResourceHostEntity;
import io.subutai.core.localpeer.impl.tasks.CleanupEnvironmentTask;
import io.subutai.core.localpeer.impl.tasks.DeleteTunnelsTask;
import io.subutai.core.localpeer.impl.tasks.JoinP2PSwarmTask;
import io.subutai.core.localpeer.impl.tasks.ResetP2PSwarmSecretTask;
import io.subutai.core.localpeer.impl.tasks.SetupTunnelsTask;
import io.subutai.core.localpeer.impl.tasks.UsedHostNetResourcesTask;
import io.subutai.core.metric.api.Monitor;
import io.subutai.core.network.api.NetworkManager;
import io.subutai.core.network.api.NetworkManagerException;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.registration.api.HostRegistrationManager;
import io.subutai.core.security.api.SecurityManager;
import io.subutai.core.security.api.crypto.EncryptionTool;
import io.subutai.core.security.api.crypto.KeyManager;
import io.subutai.core.template.api.TemplateManager;


/**
 * Local peer implementation
 *
 * TODO add proper security annotations
 */
@PermitAll
public class LocalPeerImpl extends HostListener implements LocalPeer, Disposable
{
    private static final int BUNDLE_COUNT = 275;

    private static final Logger LOG = LoggerFactory.getLogger( LocalPeerImpl.class );
    private static final BigDecimal ONE_HUNDRED = new BigDecimal( "100.00" );

    private transient DaoManager daoManager;
    private transient TemplateManager templateManager;
    transient Set<ResourceHost> resourceHosts = Sets.newConcurrentHashSet();
    private transient CommandExecutor commandExecutor;
    private transient Monitor monitor;
    transient ResourceHostDataService resourceHostDataService;
    private transient HostRegistry hostRegistry;
    transient CommandUtil commandUtil = new CommandUtil();
    transient ExceptionUtil exceptionUtil = new ExceptionUtil();
    transient Set<RequestListener> requestListeners = Sets.newHashSet();
    private transient SecurityManager securityManager;
    transient ServiceLocator serviceLocator = new ServiceLocator();
    private transient IdentityManager identityManager;
    private transient RelationManager relationManager;
    private transient NetworkResourceDaoImpl networkResourceDao;
    private transient final LocalPeerCommands localPeerCommands = new LocalPeerCommands();
    private transient final HostUtil hostUtil = new HostUtil();
    private ObjectMapper mapper = new ObjectMapper();
    volatile boolean initialized = false;
    PeerInfo peerInfo;
    private transient ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor();
    private transient ExecutorService threadPool = Executors.newCachedThreadPool();
    private transient Set<LocalPeerEventListener> peerEventListeners = Sets.newHashSet();
    private AtomicInteger containerCreationCounter = new AtomicInteger();


    public LocalPeerImpl( DaoManager daoManager, TemplateManager templateManager, CommandExecutor commandExecutor,
                          HostRegistry hostRegistry, Monitor monitor, SecurityManager securityManager )
    {
        this.daoManager = daoManager;
        this.templateManager = templateManager;
        this.monitor = monitor;
        this.commandExecutor = commandExecutor;
        this.hostRegistry = hostRegistry;
        this.securityManager = securityManager;
    }


    public void setIdentityManager( final IdentityManager identityManager )
    {
        this.identityManager = identityManager;
    }


    public void setRelationManager( final RelationManager relationManager )
    {
        this.relationManager = relationManager;
    }


    public void init()
    {
        LOG.debug( "********************************************** Initializing peer "
                + "******************************************" );
        try
        {
            initPeerInfo();

            //add command request listener
            addRequestListener( new CommandRequestListener() );
            //add command response listener

            //add create container requests listener
            addRequestListener( new CreateEnvironmentContainersRequestListener( this ) );

            //add prepare templates listener
            addRequestListener( new PrepareTemplateRequestListener( this ) );

            resourceHostDataService = createResourceHostDataService();

            resourceHosts.clear();

            resourceHosts.addAll( resourceHostDataService.getAll() );

            setResourceHostTransientFields( getResourceHosts() );

            this.networkResourceDao = new NetworkResourceDaoImpl( daoManager.getEntityManagerFactory() );


            cleaner.scheduleWithFixedDelay( new Runnable()
            {
                @Override
                public void run()
                {
                    removeStaleContainers();
                }
            }, 30, 5, TimeUnit.MINUTES );
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            throw new LocalPeerInitializationError( "Failed to init Local Peer", e );
        }

        initialized = true;
    }


    @Override
    public boolean isInitialized()
    {
        return initialized;
    }


    @Override
    public boolean isMHPresent()
    {
        try
        {
            ResourceHost mh = getManagementHost();

            return mh.isConnected();
        }
        catch ( HostNotFoundException ignore )
        {
            return false;
        }
    }


    private void initPeerInfo()
    {
        peerInfo = new PeerInfo();
        peerInfo.setId( securityManager.getKeyManager().getPeerId() );
        peerInfo.setOwnerId( securityManager.getKeyManager().getPeerOwnerId() );
        peerInfo.setPublicUrl( Common.DEFAULT_PUBLIC_URL );
        peerInfo.setPublicSecurePort( Common.DEFAULT_PUBLIC_SECURE_PORT );
        peerInfo.setName( "Local Peer" );
    }


    @Override
    public PeerInfo getPeerInfo()
    {
        return peerInfo;
    }


    @Override
    public void setPeerInfo( final PeerInfo peerInfo )
    {
        this.peerInfo = peerInfo;
    }


    ResourceHostDataService createResourceHostDataService()
    {
        return new ResourceHostDataService( daoManager.getEntityManagerFactory() );
    }


    @Override
    public void dispose()
    {
        for ( ResourceHost resourceHost : getResourceHosts() )
        {
            ( ( Disposable ) resourceHost ).dispose();
        }

        hostUtil.dispose();

        commandUtil.dispose();
    }


    private void setResourceHostTransientFields( Set<ResourceHost> resourceHosts )
    {
        for ( ResourceHost resourceHost : resourceHosts )
        {
            ( ( AbstractSubutaiHost ) resourceHost ).setPeer( this );
        }
    }


    @Override
    public String getId()
    {
        return peerInfo.getId();
    }


    @Override
    public String getName()
    {
        return peerInfo.getName();
    }


    @Override
    public String getOwnerId()
    {
        return peerInfo.getOwnerId();
    }


    @Override
    public ContainerHostState getContainerState( final ContainerId containerId ) throws PeerException
    {
        Preconditions.checkNotNull( containerId );

        try
        {
            ContainerHostInfo containerHostInfo =
                    ( ContainerHostInfo ) hostRegistry.getHostInfoById( containerId.getId() );
            return containerHostInfo.getState();
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage() );
            throw new PeerException( String.format( "Error getting container state: %s", e.getMessage() ), e );
        }
    }


    @Override
    public Containers getEnvironmentContainers( final EnvironmentId environmentId ) throws PeerException
    {
        Preconditions.checkNotNull( environmentId );

        Containers result = new Containers();
        try
        {
            Set<ContainerHost> containers = findContainersByEnvironmentId( environmentId.getId() );

            for ( ContainerHost c : containers )
            {
                ContainerHostInfo info;
                try
                {
                    info = hostRegistry.getContainerHostInfoById( c.getId() );
                }
                catch ( HostDisconnectedException e )
                {
                    info = new ContainerHostInfoModel( c );
                }
                result.addContainer( info );
            }
            return result;
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage() );
            throw new PeerException( String.format( "Error getting environment containers: %s", e.getMessage() ), e );
        }
    }


    @Override
    public void configureHostsInEnvironment( final EnvironmentId environmentId, final HostAddresses hostAddresses )
            throws PeerException
    {
        Preconditions.checkNotNull( environmentId, "Environment id is null" );
        Preconditions.checkNotNull( hostAddresses, "Invalid HostAdresses" );
        Preconditions.checkArgument( !hostAddresses.isEmpty(), "No host addresses" );

        Set<Host> hosts = Sets.newHashSet();

        hosts.addAll( findContainersByEnvironmentId( environmentId.getId() ) );

        if ( hosts.isEmpty() )
        {
            return;
        }

        CommandUtil.HostCommandResults results = commandUtil
                .executeFailFast( localPeerCommands.getAddIpHostToEtcHostsCommand( hostAddresses.getHostAddresses() ),
                        hosts, environmentId.getId() );

        for ( CommandUtil.HostCommandResult result : results.getCommandResults() )
        {
            if ( !result.hasSucceeded() )
            {
                LOG.error( "Host registration failed on host {}: {}", result.getHost().getHostname(),
                        result.getFailureReason() );
            }
        }

        if ( results.hasFailures() )
        {
            throw new PeerException( "Failed to register hosts on each host" );
        }
    }


    @Override
    public SshKeys readOrCreateSshKeysForEnvironment( final EnvironmentId environmentId,
                                                      final SshEncryptionType sshKeyType ) throws PeerException
    {
        Preconditions.checkNotNull( environmentId, "Environment id is null" );
        Preconditions.checkNotNull( sshKeyType, "Ssh key type is null" );

        SshKeys sshPublicKeys = new SshKeys();

        Set<Host> hosts = Sets.newHashSet();

        hosts.addAll( findContainersByEnvironmentId( environmentId.getId() ) );

        if ( hosts.isEmpty() )
        {
            return sshPublicKeys;
        }

        CommandUtil.HostCommandResults readResults = commandUtil
                .executeFailFast( localPeerCommands.getReadOrCreateSSHCommand( sshKeyType ), hosts,
                        environmentId.getId() );

        Set<Host> succeededHosts = Sets.newHashSet();
        Set<Host> failedHosts = Sets.newHashSet( hosts );

        for ( CommandUtil.HostCommandResult result : readResults.getCommandResults() )
        {
            if ( result.hasSucceeded() && !Strings.isNullOrEmpty( result.getCommandResult().getStdOut() ) )
            {
                sshPublicKeys.addKey( new SshKey( result.getHost().getId(), sshKeyType,
                        result.getCommandResult().getStdOut().trim() ) );

                succeededHosts.add( result.getHost() );
            }
            else
            {
                LOG.error( "Failed to generate ssh key on host {}: {}", result.getHost().getHostname(),
                        result.getFailureReason() );
            }
        }

        failedHosts.removeAll( succeededHosts );

        if ( !failedHosts.isEmpty() )
        {
            throw new PeerException( "Failed to generate ssh keys on each host" );
        }

        return sshPublicKeys;
    }


    @Override
    public void configureSshInEnvironment( final EnvironmentId environmentId, final SshKeys sshKeys )
            throws PeerException
    {
        Preconditions.checkNotNull( environmentId, "Environment id is null" );
        Preconditions.checkNotNull( sshKeys, "SshPublicKey is null" );
        Preconditions.checkArgument( !sshKeys.isEmpty(), "No ssh keys" );

        Set<Host> hosts = Sets.newHashSet();

        hosts.addAll( findContainersByEnvironmentId( environmentId.getId() ) );

        if ( hosts.isEmpty() )
        {
            return;
        }

        //add keys in portions, since all can not fit into one command, it fails
        int portionSize = Common.MAX_KEYS_IN_ECHO_CMD;
        int i = 0;
        StringBuilder keysString = new StringBuilder();
        Set<SshKey> keys = sshKeys.getKeys();

        for ( SshKey key : keys )
        {
            keysString.append( key.getPublicKey() ).append( System.lineSeparator() );

            i++;

            //send next portion of keys
            if ( i % portionSize == 0 || i == keys.size() )
            {
                CommandUtil.HostCommandResults appendResults = commandUtil
                        .executeFailFast( localPeerCommands.getAppendSshKeysCommand( keysString.toString() ), hosts,
                                environmentId.getId() );

                keysString.setLength( 0 );

                for ( CommandUtil.HostCommandResult result : appendResults.getCommandResults() )
                {
                    if ( !result.hasSucceeded() )
                    {
                        LOG.error( "Failed to add ssh keys on host {}: {}", result.getHost().getHostname(),
                                result.getFailureReason() );
                    }
                }

                if ( appendResults.hasFailures() )
                {
                    throw new PeerException( "Failed to add ssh keys on each host" );
                }
            }
        }

        //config ssh
        CommandUtil.HostCommandResults configResults =
                commandUtil.executeFailFast( localPeerCommands.getConfigSSHCommand(), hosts, environmentId.getId() );

        for ( CommandUtil.HostCommandResult result : configResults.getCommandResults() )
        {
            if ( !result.hasSucceeded() )
            {
                LOG.error( "Failed to configure ssh on host {}: {}", result.getHost().getHostname(),
                        result.getFailureReason() );
            }
        }

        if ( configResults.hasFailures() )
        {
            throw new PeerException( "Failed to configure ssh on each host" );
        }
    }


    @Override
    public SshKeys getSshKeys( final EnvironmentId environmentId, final SshEncryptionType sshEncryptionType )
            throws PeerException
    {
        Preconditions.checkNotNull( environmentId, "Environment id is null" );
        Preconditions.checkNotNull( sshEncryptionType, "SSH encryption type is null" );

        Set<Host> hosts = Sets.newHashSet();

        hosts.addAll( findContainersByEnvironmentId( environmentId.getId() ) );

        SshKeys sshKeys = new SshKeys();

        if ( hosts.isEmpty() )
        {
            return sshKeys;
        }

        CommandUtil.HostCommandResults results = commandUtil
                .execute( localPeerCommands.getReadSSHKeyCommand( sshEncryptionType ), hosts, environmentId.getId() );

        for ( CommandUtil.HostCommandResult result : results.getCommandResults() )
        {
            if ( !result.hasSucceeded() )
            {
                LOG.warn( "SSH key read failed on host {}: {}", result.getHost().getHostname(),
                        result.getFailureReason() );
            }
            else
            {
                sshKeys.addKey( new SshKey( result.getHost().getId(), sshEncryptionType,
                        result.getCommandResult().getStdOut().trim() ) );
            }
        }

        return sshKeys;
    }


    @Override
    public SshKeys getContainerAuthorizedKeys( final ContainerId containerId ) throws PeerException
    {
        Preconditions.checkNotNull( containerId, "Container id is null" );

        ContainerHost containerHost = getContainerHostById( containerId.getId() );


        try
        {
            CommandResult commandResult = execute( localPeerCommands.getReadAuthorizedKeysFile(), containerHost );

            StringTokenizer tokenizer = new StringTokenizer( commandResult.getStdOut(), System.lineSeparator() );

            SshKeys sshKeys = new SshKeys();

            while ( tokenizer.hasMoreTokens() )
            {
                String sshKey = tokenizer.nextToken();

                if ( !sshKey.trim().isEmpty() )
                {
                    sshKeys.addKey(
                            new SshKey( containerId.getId(), SshEncryptionType.parseTypeFromKey( sshKey ), sshKey ) );
                }
            }

            return sshKeys;
        }
        catch ( CommandException e )
        {
            throw new PeerException( "Error obtaining authorized keys", e );
        }
    }


    @Override
    public SshKey createSshKey( final EnvironmentId environmentId, final ContainerId containerId,
                                final SshEncryptionType sshEncryptionType ) throws PeerException
    {
        Preconditions.checkNotNull( environmentId, "Environment id is null" );
        Preconditions.checkNotNull( containerId, "Container id is null" );
        Preconditions.checkNotNull( sshEncryptionType, "SSH encryption type is null" );

        try
        {
            ContainerHost containerHost = getContainerHostById( containerId.getId() );
            if ( !containerHost.getEnvironmentId().equals( environmentId ) )
            {
                throw new HostNotFoundException( "Environment does not contains requested container." );
            }

            CommandResult commandResult = commandUtil
                    .execute( localPeerCommands.getReadOrCreateSSHCommand( sshEncryptionType ), containerHost );

            if ( commandResult.hasSucceeded() )
            {
                return new SshKey( containerId.getId(), sshEncryptionType, commandResult.getStdOut().trim() );
            }
            else
            {
                throw new CommandException( "Command execution failed." );
            }
        }
        catch ( Exception e )
        {
            throw new PeerException( "Error on creating ssh key." );
        }
    }


    @Override
    public void addToAuthorizedKeys( final EnvironmentId environmentId, final String sshPublicKey ) throws PeerException
    {
        Preconditions.checkNotNull( environmentId, "Environment id is null" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( sshPublicKey ), "Invalid ssh key" );

        Set<Host> hosts = Sets.newHashSet();

        hosts.addAll( findContainersByEnvironmentId( environmentId.getId() ) );

        if ( hosts.isEmpty() )
        {
            return;
        }

        CommandUtil.HostCommandResults results = commandUtil
                .executeFailFast( localPeerCommands.getAppendSshKeysCommand( sshPublicKey.trim() ), hosts,
                        environmentId.getId() );

        for ( CommandUtil.HostCommandResult result : results.getCommandResults() )
        {
            if ( !result.hasSucceeded() )
            {
                LOG.error( "SSH key addition failed on host {}: {}", result.getHost().getHostname(),
                        result.getFailureReason() );
            }
        }

        if ( results.hasFailures() )
        {
            throw new PeerException( "Failed to add SSH key on each host" );
        }
    }


    @Override
    public void removeFromAuthorizedKeys( final EnvironmentId environmentId, final String sshPublicKey )
            throws PeerException
    {
        Preconditions.checkNotNull( environmentId, "Environment id is null" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( sshPublicKey ), "Invalid ssh key" );

        Set<Host> hosts = Sets.newHashSet();

        hosts.addAll( findContainersByEnvironmentId( environmentId.getId() ) );

        if ( hosts.isEmpty() )
        {
            return;
        }

        CommandUtil.HostCommandResults results = commandUtil
                .executeFailFast( localPeerCommands.getRemoveSshKeyCommand( sshPublicKey ), hosts,
                        environmentId.getId() );


        for ( CommandUtil.HostCommandResult result : results.getCommandResults() )
        {
            if ( !result.hasSucceeded() )
            {
                LOG.error( "SSH key removal failed on host {}: {}", result.getHost().getHostname(),
                        result.getFailureReason() );
            }
        }

        if ( results.hasFailures() )
        {
            throw new PeerException( "Failed to remove SSH key on each host" );
        }
    }


    @RolesAllowed( "Environment-Management|Write" )
    @Override
    public PrepareTemplatesResponse prepareTemplates( final PrepareTemplatesRequest request ) throws PeerException
    {
        PrepareTemplatesResponse response = new PrepareTemplatesResponse();

        HostUtil.Tasks tasks = new HostUtil.Tasks();

        for ( final String resourceHostId : request.getTemplates().keySet() )
        {
            final ResourceHost resourceHost = getResourceHostById( resourceHostId );

            for ( final String templateId : request.getTemplates().get( resourceHostId ) )
            {
                Template template = templateManager.getTemplate( templateId );

                if ( template == null )
                {
                    throw new PeerException( String.format( "Template `%s` not found.", templateId ) );
                }

                HostUtil.Task<Object> importTask =
                        new ImportTemplateTask( template, resourceHost, request.getEnvironmentId(),
                                request.getCdnToken() );

                tasks.addTask( resourceHost, importTask );
            }
        }

        HostUtil.Results results = hostUtil.executeFailFast( tasks, request.getEnvironmentId() );

        response.addResults( results );

        return response;
    }


    @Override
    public State getState()
    {
        boolean failed = false;

        boolean ready = true;

        BundleContext ctx = FrameworkUtil.getBundle( LocalPeerImpl.class ).getBundleContext();

        BundleStateService bundleStateService = ServiceLocator.lookup( BundleStateService.class );

        Bundle[] bundles = ctx.getBundles();

        if ( bundles.length < BUNDLE_COUNT )
        {
            LOG.warn( "Bundle count is {}", bundles.length );

            return State.LOADING;
        }

        for ( Bundle bundle : bundles )
        {
            if ( bundleStateService.getState( bundle ) == BundleState.Failure )
            {
                failed = true;

                break;
            }

            if ( !( ( bundle.getState() == Bundle.ACTIVE ) || ( bundle.getState() == Bundle.RESOLVED ) ) )
            {
                ready = false;

                break;
            }
        }

        return failed ? State.FAILED : ready ? State.READY : State.LOADING;
    }


    @Override
    public synchronized FitCheckResult checkResources( final Nodes nodes ) throws PeerException
    {
        Preconditions.checkArgument(
                nodes != null && ( !CollectionUtil.isMapEmpty( nodes.getQuotas() ) || !CollectionUtil
                        .isCollectionEmpty( nodes.getNewNodes() ) ), "Invalid nodes" );

        Map<ResourceHost, ResourceHostCapacity> requestedResources = Maps.newHashMap();

        for ( ContainerHostInfo containerHostInfo : hostRegistry.getContainerHostsInfo() )
        {
            double requestedRam = 0, requestedCpu = 0, requestedDisk = 0;

            //exclude container from calculations if it is included into removed containers
            if ( nodes.getRemovedContainers() != null && nodes.getRemovedContainers()
                                                              .contains( containerHostInfo.getId() ) )
            {
                LOG.debug( "Skipping removed container {}", containerHostInfo.getContainerName() );
            }
            else
            {
                //use container quotas as amount of used resources in calculations

                //use new quota instead of current if present
                ContainerQuota newQuota = null;
                if ( nodes.getQuotas() != null )
                {
                    newQuota = nodes.getQuotas().get( containerHostInfo.getId() );
                }

                //use current quota as requested amount unless the container has a change of quota
                //note: we use 0 for containers that have unset quota since we don't know what the effective limit is
                requestedRam = newQuota != null ? newQuota.getContainerSize().getRamQuota() :
                               containerHostInfo.getRawQuota() == null
                                       || containerHostInfo.getRawQuota().getRam() == null ? 0 :
                               UnitUtil.convert( containerHostInfo.getRawQuota().getRam(), UnitUtil.Unit.MB,
                                       UnitUtil.Unit.B );

                requestedCpu = newQuota != null ? newQuota.getContainerSize().getCpuQuota() :
                               containerHostInfo.getRawQuota() == null
                                       || containerHostInfo.getRawQuota().getCpu() == null ? 0 :
                               containerHostInfo.getRawQuota().getCpu();

                requestedDisk = newQuota != null ? newQuota.getContainerSize().getDiskQuota() :
                                containerHostInfo.getRawQuota() == null
                                        || containerHostInfo.getRawQuota().getDisk() == null ? 0 :
                                UnitUtil.convert( containerHostInfo.getRawQuota().getDisk(), UnitUtil.Unit.GB,
                                        UnitUtil.Unit.B );
            }

            //figure out current container resource consumption based on historical metrics
            Calendar cal = Calendar.getInstance();
            Date endTime = cal.getTime();
            //1 hour interval is enough
            cal.add( Calendar.MINUTE, -60 );
            Date startTime = cal.getTime();

            try
            {
                ContainerHost containerHost = getContainerHostById( containerHostInfo.getId() );

                HistoricalMetrics historicalMetrics = monitor.getMetricsSeries( containerHost, startTime, endTime );
                HostMetricsDto hostMetricsDto = historicalMetrics.getHostMetrics();

                //skip partial metric, b/c this happens for new containers
                if ( !HistoricalMetrics.isZeroMetric( hostMetricsDto ) )
                {

                    double ramUsed = hostMetricsDto.getMemory().getCached() + hostMetricsDto.getMemory().getRss();
                    double cpuUsed = hostMetricsDto.getCpu().getSystem() + hostMetricsDto.getCpu().getUser();
                    double diskUsed = historicalMetrics.getContainerDiskUsed();

                    //subtract current consumption resource amount from the requested amount
                    if ( requestedRam > 0 )
                    {
                        requestedRam -= ramUsed;
                    }
                    if ( requestedCpu > 0 )
                    {
                        requestedCpu -= cpuUsed;
                    }
                    if ( requestedDisk > 0 )
                    {
                        requestedDisk -= diskUsed;
                    }
                }
            }
            catch ( HostNotFoundException e )
            {
                //skip unregistered containers, b/c their quotas are not considered
            }

            ResourceHostInfo resourceHostInfo;
            try
            {
                resourceHostInfo = hostRegistry.getResourceHostByContainerHost( containerHostInfo );
            }
            catch ( HostDisconnectedException e )
            {
                throw new PeerException( e );
            }

            ResourceHost resourceHost = getResourceHostById( resourceHostInfo.getId() );

            ResourceHostCapacity resourceHostCapacity = requestedResources.get( resourceHost );

            if ( resourceHostCapacity != null )
            {
                resourceHostCapacity.setRam( resourceHostCapacity.getRam() + requestedRam );
                resourceHostCapacity.setDisk( resourceHostCapacity.getDisk() + requestedDisk );
                resourceHostCapacity.setCpu( resourceHostCapacity.getCpu() + requestedCpu );
            }
            else
            {
                resourceHostCapacity = new ResourceHostCapacity( requestedRam, requestedDisk, requestedCpu );
            }

            requestedResources.put( resourceHost, resourceHostCapacity );
        }

        //new nodes
        if ( nodes.getNewNodes() != null )
        {
            for ( Node node : nodes.getNewNodes() )
            {
                double requestedRam = node.getQuota().getContainerSize().getRamQuota();
                double requestedDisk = node.getQuota().getContainerSize().getDiskQuota();
                double requestedCpu = node.getQuota().getContainerSize().getCpuQuota();

                ResourceHost resourceHost = getResourceHostById( node.getHostId() );

                ResourceHostCapacity resourceHostCapacity = requestedResources.get( resourceHost );

                if ( resourceHostCapacity != null )
                {
                    resourceHostCapacity.setRam( resourceHostCapacity.getRam() + requestedRam );
                    resourceHostCapacity.setDisk( resourceHostCapacity.getDisk() + requestedDisk );
                    resourceHostCapacity.setCpu( resourceHostCapacity.getCpu() + requestedCpu );
                }
                else
                {
                    resourceHostCapacity = new ResourceHostCapacity( requestedRam, requestedDisk, requestedCpu );
                }

                requestedResources.put( resourceHost, resourceHostCapacity );
            }
        }

        Map<ResourceHost, ResourceHostCapacity> availableResources = Maps.newHashMap();

        for ( ResourceHost resourceHost : getResourceHosts() )
        {
            ResourceHostMetric resourceHostMetric = monitor.getResourceHostMetric( resourceHost );

            double availPeerRam = resourceHostMetric.getAvailableRam();
            double availPeerDisk = resourceHostMetric.getAvailableSpace();
            double availPeerCpu = resourceHostMetric.getCpuCore() * resourceHostMetric.getAvailableCpu();

            availableResources
                    .put( resourceHost, new ResourceHostCapacity( availPeerRam, availPeerDisk, availPeerCpu ) );
        }


        return new FitCheckResult( availableResources, requestedResources );
    }


    @RolesAllowed( "Environment-Management|Read" )
    @Override
    public boolean canAccommodate( final Nodes nodes ) throws PeerException
    {
        if ( !Common.CHECK_RH_LIMITS )
        {
            return true;
        }

        FitCheckResult fitCheckResult = checkResources( nodes );

        return fitCheckResult.canFit();
    }


    @RolesAllowed( "Environment-Management|Write" )
    @Override
    public CreateEnvironmentContainersResponse createEnvironmentContainers(
            final CreateEnvironmentContainersRequest requestGroup ) throws PeerException
    {
        Preconditions.checkNotNull( requestGroup );

        try
        {
            containerCreationCounter.incrementAndGet();

            NetworkResource reservedNetworkResource =
                    getReservedNetworkResources().findByEnvironmentId( requestGroup.getEnvironmentId() );

            if ( reservedNetworkResource == null )
            {
                throw new PeerException( String.format( "No reserved network resources found for environment %s",
                        requestGroup.getEnvironmentId() ) );
            }

            Set<String> namesToExclude = Sets.newHashSet();
            for ( ContainerHostInfo containerHostInfo : getNotRegisteredContainers() )
            {
                namesToExclude.add( containerHostInfo.getContainerName().toLowerCase() );
            }


            //clone containers
            HostUtil.Tasks cloneTasks = new HostUtil.Tasks();

            for ( final CloneRequest request : requestGroup.getRequests() )
            {
                ResourceHost resourceHost = getResourceHostById( request.getResourceHostId() );

                CloneContainerTask task =
                        new CloneContainerTask( request, templateManager.getTemplate( request.getTemplateId() ),
                                resourceHost, reservedNetworkResource, this, namesToExclude );

                cloneTasks.addTask( resourceHost, task );
            }

            HostUtil.Results cloneResults = hostUtil.execute( cloneTasks, reservedNetworkResource.getEnvironmentId() );

            //register succeeded containers

            for ( HostUtil.Task cloneTask : cloneResults.getTasks().getTasks() )
            {
                CloneRequest request = ( ( CloneContainerTask ) cloneTask ).getRequest();

                if ( cloneTask.getTaskState() == HostUtil.Task.TaskState.SUCCEEDED )
                {

                    final HostInterfaces interfaces = new HostInterfaces();

                    interfaces.addHostInterface( new HostInterfaceModel( Common.DEFAULT_CONTAINER_INTERFACE,
                            request.getIp().split( "/" )[0] ) );

                    ContainerHostEntity containerHostEntity =
                            new ContainerHostEntity( getId(), ( ( CloneContainerTask ) cloneTask ).getResult(),
                                    request.getHostname(), request.getTemplateArch(), interfaces,
                                    request.getContainerName(), request.getTemplateId(),
                                    requestGroup.getEnvironmentId(), requestGroup.getOwnerId(),
                                    requestGroup.getInitiatorPeerId(), request.getContainerQuota(),
                                    reservedNetworkResource.getVlan() );

                    registerContainer( request.getResourceHostId(), containerHostEntity );
                }
            }

            return new CreateEnvironmentContainersResponse( cloneResults, reservedNetworkResource );
        }
        finally
        {
            containerCreationCounter.decrementAndGet();
        }
    }


    private boolean isContainerCreationInProgress()
    {
        return containerCreationCounter.get() > 0;
    }


    private void registerContainer( String resourceHostId, ContainerHostEntity containerHost ) throws PeerException
    {

        ResourceHost resourceHost = getResourceHostById( resourceHostId );

        try
        {
            signContainerKeyWithPEK( containerHost.getId(), containerHost.getEnvironmentId() );

            resourceHost.addContainerHost( containerHost );

            resourceHostDataService.update( ( ResourceHostEntity ) resourceHost );

            buildEnvContainerRelation( containerHost );

            LOG.debug( "New container host registered: " + containerHost.getHostname() );
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );

            throw new PeerException( String.format( "Error registering container: %s", e.getMessage() ), e );
        }
    }


    private void signContainerKeyWithPEK( String containerId, EnvironmentId envId ) throws PeerException
    {
        String pairId = String.format( "%s_%s", getId(), envId.getId() );
        final PGPSecretKeyRing pekSecKeyRing = securityManager.getKeyManager().getSecretKeyRing( pairId );

        PGPPublicKeyRing containerPub = securityManager.getKeyManager().getPublicKeyRing( containerId );

        PGPPublicKeyRing signedKey =
                securityManager.getKeyManager().setKeyTrust( pekSecKeyRing, containerPub, KeyTrustLevel.FULL.getId() );

        securityManager.getKeyManager().updatePublicKeyRing( signedKey );
    }


    @PermitAll
    @Override
    public Set<ContainerHost> findContainersByEnvironmentId( final String environmentId )
    {
        Preconditions.checkNotNull( environmentId, "Invalid environment id" );

        Set<ContainerHost> result = new HashSet<>();

        for ( ResourceHost resourceHost : getResourceHosts() )
        {
            result.addAll( resourceHost.getContainerHostsByEnvironmentId( environmentId ) );
        }
        return result;
    }


    @PermitAll
    @Override
    public ContainerHost getContainerHostByHostName( String hostname ) throws HostNotFoundException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hostname ), "Container hostname shouldn't be null" );

        for ( ResourceHost resourceHost : getResourceHosts() )
        {
            try
            {
                return resourceHost.getContainerHostByHostName( hostname );
            }
            catch ( HostNotFoundException ignore )
            {
                //ignore
            }
        }

        throw new HostNotFoundException( String.format( "No container host found for hostname %s", hostname ) );
    }


    @PermitAll
    @Override
    public ContainerHost getContainerHostByContainerName( String containerName ) throws HostNotFoundException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( containerName ), "Container name shouldn't be null" );

        for ( ResourceHost resourceHost : getResourceHosts() )
        {
            try
            {
                return resourceHost.getContainerHostByContainerName( containerName );
            }
            catch ( HostNotFoundException ignore )
            {
                //ignore
            }
        }

        throw new HostNotFoundException( String.format( "No container host found for name %s", containerName ) );
    }


    @PermitAll
    @Override
    public ContainerHost getContainerHostById( final String hostId ) throws HostNotFoundException
    {
        Preconditions.checkNotNull( hostId, "Invalid container host id" );

        for ( ResourceHost resourceHost : getResourceHosts() )
        {
            try
            {
                return resourceHost.getContainerHostById( hostId );
            }
            catch ( HostNotFoundException e )
            {
                //ignore
            }
        }

        throw new HostNotFoundException( String.format( "Container host not found by id %s", hostId ) );
    }


    @PermitAll
    @Override
    public ContainerHost getContainerHostByIp( final String hostIp ) throws HostNotFoundException
    {
        Preconditions.checkNotNull( hostIp, "Invalid container host ip" );

        for ( ResourceHost resourceHost : getResourceHosts() )
        {
            try
            {
                return resourceHost.getContainerHostByIp( hostIp );
            }
            catch ( HostNotFoundException e )
            {
                //ignore
            }
        }

        throw new HostNotFoundException( String.format( "Container host not found by ip %s", hostIp ) );
    }


    @PermitAll
    @Override
    public ResourceHost getResourceHostByHostName( String hostname ) throws HostNotFoundException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hostname ), "Invalid resource host hostname" );


        for ( ResourceHost resourceHost : getResourceHosts() )
        {
            if ( resourceHost.getHostname().equalsIgnoreCase( hostname ) )
            {
                return resourceHost;
            }
        }
        throw new HostNotFoundException( String.format( "Resource host not found by hostname %s", hostname ) );
    }


    @PermitAll
    @Override
    public ResourceHost getResourceHostById( final String hostId ) throws HostNotFoundException
    {
        Preconditions.checkNotNull( hostId, "Resource host id is null" );

        for ( ResourceHost resourceHost : getResourceHosts() )
        {
            if ( resourceHost.getId().equals( hostId ) )
            {
                return resourceHost;
            }
        }
        throw new HostNotFoundException( String.format( "Resource host not found by id %s", hostId ) );
    }


    @PermitAll
    @Override
    public ResourceHost getResourceHostByContainerHostName( final String hostname ) throws HostNotFoundException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hostname ), "Invalid container hostname" );

        ContainerHost c = getContainerHostByHostName( hostname );
        ContainerHostEntity containerHostEntity = ( ContainerHostEntity ) c;
        return containerHostEntity.getParent();
    }


    @Override
    public ResourceHost getResourceHostByContainerName( final String containerName ) throws HostNotFoundException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( containerName ), "Invalid container name" );

        ContainerHost c = getContainerHostByContainerName( containerName );
        ContainerHostEntity containerHostEntity = ( ContainerHostEntity ) c;
        return containerHostEntity.getParent();
    }


    @PermitAll
    @Override
    public ResourceHost getResourceHostByContainerId( final String hostId ) throws HostNotFoundException
    {
        Preconditions.checkNotNull( hostId, "Container host id is invalid" );

        ContainerHost c = getContainerHostById( hostId );
        ContainerHostEntity containerHostEntity = ( ContainerHostEntity ) c;
        return containerHostEntity.getParent();
    }


    @Override
    public Host findHost( String id ) throws HostNotFoundException
    {
        Preconditions.checkNotNull( id );

        for ( ResourceHost resourceHost : getResourceHosts() )
        {
            if ( resourceHost.getId().equals( id ) )
            {
                return resourceHost;
            }
            else
            {
                try
                {
                    return resourceHost.getContainerHostById( id );
                }
                catch ( HostNotFoundException ignore )
                {
                    //ignore
                }
            }
        }

        throw new HostNotFoundException( String.format( "Host by id %s is not registered", id ) );
    }


    @Override
    public Host findHostByName( final String hostname ) throws HostNotFoundException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hostname ) );

        for ( ResourceHost resourceHost : getResourceHosts() )
        {
            if ( resourceHost.getHostname().equals( hostname ) )
            {
                return resourceHost;
            }
            for ( ContainerHost containerHost : resourceHost.getContainerHosts() )
            {
                if ( containerHost.getHostname().equals( hostname ) )
                {
                    return containerHost;
                }
            }
        }

        throw new HostNotFoundException( "Host by name '" + hostname + "' is not registered." );
    }


    @RolesAllowed( "Environment-Management|Update" )
    @Override
    public void startContainer( final ContainerId containerId ) throws PeerException
    {
        Preconditions.checkNotNull( containerId, "Cannot operate on null container id" );

        ContainerHostEntity containerHost = ( ContainerHostEntity ) getContainerHostById( containerId.getId() );
        ResourceHost resourceHost = containerHost.getParent();
        try
        {
            resourceHost.startContainerHost( containerHost );
        }
        catch ( Exception e )
        {
            String errMsg = String.format( "Could not start container %s: %s", containerHost.getContainerName(),
                    e.getMessage() );
            LOG.error( errMsg, e );
            throw new PeerException( errMsg, e );
        }
    }


    @RolesAllowed( "Environment-Management|Update" )
    @Override
    public void stopContainer( final ContainerId containerId ) throws PeerException
    {
        Preconditions.checkNotNull( containerId, "Cannot operate on null container id" );

        ContainerHostEntity containerHost = ( ContainerHostEntity ) getContainerHostById( containerId.getId() );
        ResourceHost resourceHost = containerHost.getParent();
        try
        {
            resourceHost.stopContainerHost( containerHost );
        }
        catch ( Exception e )
        {
            String errMsg = String.format( "Could not stop container %s: %s", containerHost.getContainerName(),
                    e.getMessage() );
            LOG.error( errMsg, e );
            throw new PeerException( errMsg, e );
        }
    }


    @RolesAllowed( "Environment-Management|Delete" )
    @Override
    public void destroyContainer( final ContainerId containerId ) throws PeerException
    {
        Preconditions.checkNotNull( containerId, "Cannot operate on null container id" );

        ContainerHostEntity host;
        try
        {
            host = ( ContainerHostEntity ) getContainerHostById( containerId.getId() );
        }
        catch ( HostNotFoundException e )
        {
            return;
        }

        ResourceHost resourceHost = host.getParent();

        try
        {
            resourceHost.destroyContainerHost( host );
        }
        catch ( ResourceHostException e )
        {
            String errMsg = String.format( "Could not destroy container %s: %s", host.getHostname(), e.getMessage() );
            LOG.error( errMsg, e );
            throw new PeerException( errMsg, e );
        }

        resourceHostDataService.update( ( ResourceHostEntity ) resourceHost );

        //if this container is the last one in its host environment
        //then cleanup the host environment on local peer to release its reserved resources
        if ( findContainersByEnvironmentId( host.getEnvironmentId().getId() ).isEmpty() )
        {
            //don't remove local peer PEK, it is used for communication with remote peers
            cleanupEnvironment( host.getEnvironmentId(), !getId().equals( host.getInitiatorPeerId() ) );
        }
    }


    @Override
    public boolean isConnected( final HostId hostId )
    {
        Preconditions.checkNotNull( hostId, "Host id null" );

        try
        {
            HostInfo hostInfo = hostRegistry.getHostInfoById( hostId.getId() );

            if ( hostInfo.getId().equals( hostId.getId() ) )
            {
                if ( hostInfo instanceof ResourceHostInfo )
                {
                    return hostRegistry.pingHost( ( ( ResourceHostInfo ) hostInfo ).getAddress() );
                }
                else
                {
                    ResourceHostInfo resourceHostInfo =
                            hostRegistry.getResourceHostByContainerHost( ( ContainerHostInfo ) hostInfo );

                    return hostRegistry.pingHost( resourceHostInfo.getAddress() ) && ContainerHostState.RUNNING
                            .equals( ( ( ContainerHostInfo ) hostInfo ).getState() );
                }
            }
        }
        catch ( HostDisconnectedException ignore )
        {
        }

        return false;
    }


    @Override
    public ResourceHost getManagementHost() throws HostNotFoundException
    {
        return getResourceHostByContainerName( Common.MANAGEMENT_HOSTNAME );
    }


    @Override
    public Set<ResourceHost> getResourceHosts()
    {
        return Collections.unmodifiableSet( this.resourceHosts );
    }


    void addResourceHost( final ResourceHost host )
    {
        Preconditions.checkNotNull( host, "Resource host could not be null." );

        resourceHosts.add( host );
    }


    @Override
    public void removeResourceHost( String rhId ) throws HostNotFoundException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( rhId ) );

        //remove rh ssl cert
        securityManager.getKeyStoreManager().removeCertFromTrusted( Common.DEFAULT_PUBLIC_SECURE_PORT, rhId );

        securityManager.getHttpContextManager().reloadKeyStore();

        //remove rh key
        KeyManager keyManager = securityManager.getKeyManager();

        keyManager.removeKeyData( rhId );

        //remove rh from db
        resourceHostDataService.remove( rhId );

        //remove from host registry cache
        hostRegistry.removeResourceHost( rhId );

        ResourceHost resourceHost = getResourceHostById( rhId );

        //remove rh containers' keys
        for ( final ContainerHost containerHost : resourceHost.getContainerHosts() )
        {
            keyManager.removeKeyData( containerHost.getKeyId() );
        }

        //remove rh from local cache
        resourceHosts.remove( resourceHost );
    }


    @Override
    public CommandResult execute( final RequestBuilder requestBuilder, final Host host ) throws CommandException
    {
        return execute( requestBuilder, host, null );
    }


    @Override
    public CommandResult execute( final RequestBuilder requestBuilder, final Host aHost,
                                  final CommandCallback callback ) throws CommandException
    {
        Preconditions.checkNotNull( requestBuilder, "Invalid request" );
        Preconditions.checkNotNull( aHost, "Invalid host" );

        CommandResult result;

        if ( callback == null )
        {
            result = commandExecutor.execute( aHost.getId(), requestBuilder );
        }
        else
        {
            result = commandExecutor.execute( aHost.getId(), requestBuilder, callback );
        }

        return result;
    }


    @Override
    public void executeAsync( final RequestBuilder requestBuilder, final Host aHost, final CommandCallback callback )
            throws CommandException
    {
        Preconditions.checkNotNull( requestBuilder, "Invalid request" );
        Preconditions.checkNotNull( aHost, "Invalid host" );

        if ( callback == null )
        {
            commandExecutor.executeAsync( aHost.getId(), requestBuilder );
        }
        else
        {
            commandExecutor.executeAsync( aHost.getId(), requestBuilder, callback );
        }
    }


    @Override
    public void executeAsync( final RequestBuilder requestBuilder, final Host host ) throws CommandException
    {
        executeAsync( requestBuilder, host, null );
    }


    @Override
    public boolean isLocal()
    {
        return true;
    }


    @PermitAll
    @Override
    public boolean isOnline()
    {
        return true;
    }


    @Override
    public <T, V> V sendRequest( final T request, final String recipient, final int requestTimeout,
                                 final Class<V> responseType, final int responseTimeout, Map<String, String> headers )
            throws PeerException
    {
        Preconditions.checkNotNull( responseType, "Invalid response type" );

        return sendRequestInternal( request, recipient, responseType );
    }


    @Override
    public <T> void sendRequest( final T request, final String recipient, final int requestTimeout,
                                 Map<String, String> headers ) throws PeerException
    {
        sendRequestInternal( request, recipient, null );
    }


    <T, V> V sendRequestInternal( final T request, final String recipient, final Class<V> responseType )
            throws PeerException
    {
        Preconditions.checkNotNull( request, "Invalid request" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( recipient ), "Invalid recipient" );

        for ( RequestListener requestListener : requestListeners )
        {
            if ( recipient.equalsIgnoreCase( requestListener.getRecipient() ) )
            {
                try
                {
                    Object response = requestListener.onRequest( new Payload( request, getId() ) );

                    if ( response != null && responseType != null )
                    {
                        return responseType.cast( response );
                    }
                }
                catch ( Exception e )
                {
                    LOG.error( e.getMessage(), e );
                    throw new PeerException( e );
                }
            }
        }

        return null;
    }


    public void registerResourceHost( ResourceHostInfo resourceHostInfo )
    {

        if ( StringUtils.isBlank( resourceHostInfo.getId() ) )
        {
            //handle case when agent sends empty ID after RH update
            return;
        }

        if ( isInitialized() && !StringUtils.isBlank( resourceHostInfo.getAddress() ) )
        {
            boolean firstMhRegistration = false;

            ResourceHostEntity host;

            try
            {
                host = ( ResourceHostEntity ) getResourceHostById( resourceHostInfo.getId() );
            }
            catch ( HostNotFoundException e )
            {
                //register new RH
                host = new ResourceHostEntity( getId(), resourceHostInfo );

                resourceHostDataService.persist( host );

                addResourceHost( host );

                setResourceHostTransientFields( Sets.<ResourceHost>newHashSet( host ) );

                buildAdminHostRelation( host );

                LOG.debug( String.format( "Resource host %s registered.", resourceHostInfo.getHostname() ) );

                try
                {
                    getManagementHost();
                }
                catch ( HostNotFoundException ignore )
                {
                    for ( ContainerHostInfo containerHostInfo : resourceHostInfo.getContainers() )
                    {
                        if ( Common.MANAGEMENT_HOSTNAME.equalsIgnoreCase( containerHostInfo.getContainerName() ) )
                        {
                            firstMhRegistration = true;
                            break;
                        }
                    }
                }
            }

            //update host info from heartbeat
            host.updateHostInfo( resourceHostInfo );

            resourceHostDataService.update( host );

            LOG.debug( String.format( "Resource host %s updated.", resourceHostInfo.getHostname() ) );

            if ( firstMhRegistration )
            {
                //exchange keys with MH container
                try
                {
                    registerManagementContainer( host );
                }
                catch ( Exception e )
                {
                    LOG.error( "Error exchanging keys with MH", e );
                }

                //setup security
                try
                {
                    buildAdminHostRelation( getContainerHostByContainerName( Common.MANAGEMENT_HOSTNAME ) );
                }
                catch ( Exception e )
                {
                    LOG.error( "Error setting up security relations with MH", e );
                }
            }
        }
    }


    @Override
    public void onHeartbeat( final ResourceHostInfo resourceHostInfo, Set<QuotaAlertValue> alerts )
    {
        LOG.debug( "On heartbeat: " + resourceHostInfo.getHostname() );

        registerResourceHost( resourceHostInfo );

        releaseUpdateLock( resourceHostInfo );
    }


    @Override
    public void registerManagementContainer( ResourceHost resourceHost ) throws PeerException
    {
        HostRegistrationManager registrationManager = ServiceLocator.lookup( HostRegistrationManager.class );

        try
        {
            String token = registrationManager.generateContainerToken( 30 * 1000L );

            commandUtil.execute( localPeerCommands.getRegisterManagementContainerCommand( token ), resourceHost );
        }
        catch ( Exception e )
        {
            throw new PeerException( e );
        }
    }


    //networking


    @Override
    public String getVniDomain( final Long vni ) throws PeerException
    {
        NetworkResource reservedNetworkResource = getReservedNetworkResources().findByVni( vni );

        if ( reservedNetworkResource != null )
        {
            try
            {
                return getNetworkManager().getVlanDomain( reservedNetworkResource.getVlan() );
            }
            catch ( NetworkManagerException e )
            {
                String errMsg =
                        String.format( "Error obtaining domain by vlan %d: %s", reservedNetworkResource.getVlan(),
                                e.getMessage() );
                LOG.error( errMsg, e );
                throw new PeerException( errMsg, e );
            }
        }
        else
        {
            throw new PeerException( String.format( "Vlan for vni %d not found", vni ) );
        }
    }


    //    @RolesAllowed( "Environment-Management|Delete" )
    @Override
    public void removeVniDomain( final Long vni ) throws PeerException
    {
        NetworkResource reservedNetworkResource = getReservedNetworkResources().findByVni( vni );

        if ( reservedNetworkResource != null )
        {
            try
            {
                getNetworkManager().removeVlanDomain( reservedNetworkResource.getVlan() );
            }
            catch ( NetworkManagerException e )
            {
                String errMsg =
                        String.format( "Error removing domain by vlan %d: %s", reservedNetworkResource.getVlan(),
                                e.getMessage() );
                LOG.error( errMsg, e );
                throw new PeerException( errMsg, e );
            }
        }
        else
        {
            throw new PeerException( String.format( "Vlan for vni %d not found", vni ) );
        }
    }


    //    @RolesAllowed( "Environment-Management|Update" )
    @Override
    public void setVniDomain( final Long vni, final String domain,
                              final ProxyLoadBalanceStrategy proxyLoadBalanceStrategy, final String sslCertPath )
            throws PeerException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( domain ) );
        Preconditions.checkNotNull( proxyLoadBalanceStrategy );

        NetworkResource reservedNetworkResource = getReservedNetworkResources().findByVni( vni );

        if ( reservedNetworkResource != null )
        {
            try
            {
                getNetworkManager().setVlanDomain( reservedNetworkResource.getVlan(), domain, proxyLoadBalanceStrategy,
                        sslCertPath );
            }
            catch ( NetworkManagerException e )
            {
                String errMsg = String.format( "Error setting domain by vlan %d: %s", reservedNetworkResource.getVlan(),
                        e.getMessage() );
                LOG.error( errMsg, e );
                throw new PeerException( errMsg, e );
            }
        }
        else
        {
            throw new PeerException( String.format( "Vlan for vni %d not found", vni ) );
        }
    }


    @Override
    public boolean isIpInVniDomain( final String hostIp, final Long vni ) throws PeerException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hostIp ) );

        NetworkResource reservedNetworkResource = getReservedNetworkResources().findByVni( vni );

        if ( reservedNetworkResource != null )
        {
            try
            {
                return getNetworkManager().isIpInVlanDomain( hostIp, reservedNetworkResource.getVlan() );
            }
            catch ( NetworkManagerException e )
            {
                String errMsg = String.format( "Error checking domain by ip %s and vlan %d: %s", hostIp,
                        reservedNetworkResource.getVlan(), e.getMessage() );
                LOG.error( errMsg, e );
                throw new PeerException( errMsg, e );
            }
        }
        else
        {
            throw new PeerException( String.format( "Vlan for vni %d not found", vni ) );
        }
    }


    //    @RolesAllowed( "Environment-Management|Update" )
    @Override
    public void addIpToVniDomain( final String hostIp, final Long vni ) throws PeerException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hostIp ) );

        NetworkResource reservedNetworkResource = getReservedNetworkResources().findByVni( vni );

        if ( reservedNetworkResource != null )
        {
            try
            {
                getNetworkManager().addIpToVlanDomain( hostIp, reservedNetworkResource.getVlan() );
            }
            catch ( NetworkManagerException e )
            {
                String errMsg = String.format( "Error adding ip %s to domain by vlan %d: %s", hostIp,
                        reservedNetworkResource.getVlan(), e.getMessage() );
                LOG.error( errMsg, e );
                throw new PeerException( errMsg, e );
            }
        }
        else
        {
            throw new PeerException( String.format( "Vlan for vni %d not found", vni ) );
        }
    }


    //    @RolesAllowed( "Environment-Management|Update" )
    @Override
    public void removeIpFromVniDomain( final String hostIp, final Long vni ) throws PeerException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hostIp ) );

        NetworkResource reservedNetworkResource = getReservedNetworkResources().findByVni( vni );

        if ( reservedNetworkResource != null )
        {
            try
            {
                getNetworkManager().removeIpFromVlanDomain( hostIp, reservedNetworkResource.getVlan() );
            }
            catch ( NetworkManagerException e )
            {
                String errMsg = String.format( "Error removing ip %s from domain by vlan %d: %s", hostIp,
                        reservedNetworkResource.getVlan(), e.getMessage() );
                LOG.error( errMsg, e );
                throw new PeerException( errMsg, e );
            }
        }
        else
        {

            throw new PeerException( String.format( "Vlan for vni %d not found", vni ) );
        }
    }


    @RolesAllowed( "Environment-Management|Update" )
    @Override
    public SshTunnel setupSshTunnelForContainer( final String containerIp, final int sshIdleTimeout )
            throws PeerException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( containerIp ) );
        Preconditions.checkArgument( containerIp.matches( Common.IP_REGEX ) );
        Preconditions.checkArgument( sshIdleTimeout > 0 );


        try
        {
            return getNetworkManager().setupContainerSshTunnel( containerIp, sshIdleTimeout );
        }
        catch ( NetworkManagerException e )
        {
            String errMsg =
                    String.format( "Error setting up ssh tunnel for container ip %s: %s", containerIp, e.getMessage() );
            LOG.error( errMsg, e );
            throw new PeerException( errMsg, e );
        }
    }


    @Override
    public List<ContainerHost> getPeerContainers( final String peerId )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( peerId ) );

        List<ContainerHost> result = new ArrayList<>();
        for ( ResourceHost resourceHost : getResourceHosts() )
        {
            result.addAll( resourceHost.getContainerHostsByPeerId( peerId ) );
        }
        return result;
    }


    @Override
    public void addRequestListener( final RequestListener listener )
    {
        if ( listener != null )
        {
            requestListeners.add( listener );
        }
    }


    public void removeRequestListener( final RequestListener listener )
    {
        if ( listener != null )
        {
            requestListeners.remove( listener );
        }
    }


    @Override
    public Set<RequestListener> getRequestListeners()
    {
        return Collections.unmodifiableSet( requestListeners );
    }


    /* ***********************************************
     *  Create PEK
     */
    @RolesAllowed( "Environment-Management|Write" )
    @Override
    public PublicKeyContainer createPeerEnvironmentKeyPair( RelationLinkDto envLink ) throws PeerException
    {
        Preconditions.checkNotNull( envLink );

        KeyManager keyManager = securityManager.getKeyManager();
        EncryptionTool encTool = securityManager.getEncryptionTool();
        String pairId = String.format( "%s_%s", getId(), envLink.getUniqueIdentifier() );

        PGPPublicKeyRing envPubkey = keyManager.getPublicKeyRing( pairId );
        try
        {
            if ( envPubkey == null )
            {
                buildPeerEnvRelation( envLink );

                final PGPSecretKeyRing peerSecKeyRing = keyManager.getSecretKeyRing( null );
                KeyPair keyPair = keyManager.generateKeyPair( pairId, false );

                //******Create PEK *****************************************************************
                PGPSecretKeyRing secRing = PGPKeyUtil.readSecretKeyRing( keyPair.getSecKeyring() );
                PGPPublicKeyRing pubRing = PGPKeyUtil.readPublicKeyRing( keyPair.getPubKeyring() );

                //***************Save Keys *********************************************************
                keyManager.saveSecretKeyRing( pairId, SecurityKeyType.PEER_ENVIRONMENT_KEY.getId(), secRing );
                keyManager.savePublicKeyRing( pairId, SecurityKeyType.PEER_ENVIRONMENT_KEY.getId(), pubRing );

                pubRing = keyManager.setKeyTrust( peerSecKeyRing, pubRing, KeyTrustLevel.FULL.getId() );

                return new PublicKeyContainer( getId(), pubRing.getPublicKey().getFingerprint(),
                        encTool.armorByteArrayToString( pubRing.getEncoded() ) );
            }
            else
            {
                return new PublicKeyContainer( getId(), envPubkey.getPublicKey().getFingerprint(),
                        encTool.armorByteArrayToString( envPubkey.getEncoded() ) );
            }
        }
        catch ( Exception e )
        {
            String errMsg = String.format( "Error creating PEK: %s", e.getMessage() );
            LOG.error( errMsg, e );
            throw new PeerException( errMsg, e );
        }
    }


    private void buildEnvContainerRelation( final ContainerHostEntity containerHost )
    {

        RelationInfoMeta relationInfoMeta = new RelationInfoMeta( true, true, true, true, Ownership.USER.getLevel() );
        Map<String, String> relationTraits = relationInfoMeta.getRelationTraits();
        relationTraits.put( "containerLimit", "unlimited" );
        relationTraits.put( "bandwidthLimit", "unlimited" );
        relationTraits.put( "read", "true" );
        relationTraits.put( "write", "true" );
        relationTraits.put( "update", "true" );
        relationTraits.put( "delete", "true" );
        relationTraits.put( "ownership", Ownership.USER.getName() );

        RelationLink source;
        User activeUser = identityManager.getActiveUser();
        if ( activeUser == null || activeUser.getType() == UserType.SYSTEM.getId() )
        {
            // Most probably it is remote container, so owner will be localPeer
            source = this;
            LOG.debug( "Setting LocalPeer as source" );
        }
        else
        {
            source = identityManager.getUserDelegate( activeUser.getId() );
            LOG.debug( "Setting DelegatedUser as source" );
        }


        RelationLink envLink = new RelationLink()
        {
            @Override
            public String getLinkId()
            {
                return String.format( "%s|%s", getClassPath(), getUniqueIdentifier() );
            }


            @Override
            public String getUniqueIdentifier()
            {
                return containerHost.getEnvironmentId().getId();
            }


            @Override
            public String getClassPath()
            {
                return "LocalEnvironment";
            }


            @Override
            public String getContext()
            {
                return PermissionObject.ENVIRONMENT_MANAGEMENT.getName();
            }


            @Override
            public String getKeyId()
            {
                return containerHost.getEnvironmentId().getId();
            }
        };

        if ( source == null )
        {
            LOG.debug( "Source is null" );
        }


        RelationMeta relationMeta = new RelationMeta( source, envLink, containerHost, envLink.getKeyId() );
        Relation relation = relationManager.buildRelation( relationInfoMeta, relationMeta );
        relation.setRelationStatus( RelationStatus.VERIFIED );
        relationManager.saveRelation( relation );
    }


    private void buildAdminHostRelation( Host host )
    {
        // Build relation between Admin and management/resource host.

        User peerOwner = identityManager.getUserByKeyId( identityManager.getPeerOwnerId() );
        if ( peerOwner != null )
        {
            // Simply pass key value object as map
            RelationInfoMeta relationInfoMeta =
                    new RelationInfoMeta( true, true, true, true, Ownership.USER.getLevel() );
            Map<String, String> relationTraits = relationInfoMeta.getRelationTraits();
            relationTraits.put( "bandwidthControl", "true" );
            relationTraits.put( "ownership", Ownership.USER.getName() );
            relationTraits.put( "read", "true" );
            relationTraits.put( "write", "true" );
            relationTraits.put( "update", "true" );
            relationTraits.put( "delete", "true" );

            if ( host instanceof ContainerHost && Common.MANAGEMENT_HOSTNAME
                    .equalsIgnoreCase( ( ( ContainerHost ) host ).getContainerName() ) )
            {
                relationTraits.put( "managementSupervisor", "true" );
            }
            else
            {
                relationTraits.put( "resourceSupervisor", "true" );
                relationTraits.put( "containerManagement", "true" );
            }

            RelationMeta relationMeta = new RelationMeta( peerOwner, peerOwner, host, peerOwner.getSecurityKeyId() );
            Relation relation = relationManager.buildRelation( relationInfoMeta, relationMeta );
            relation.setRelationStatus( RelationStatus.VERIFIED );
            relationManager.saveRelation( relation );
        }
    }


    private void buildPeerEnvRelation( final RelationLink envLink )
    {

        // Build relation between LocalPeer and LocalEnvironment/CrossPeerEnvironment. Planned to use it in future
        // for peer policy feature between peers.

        User peerOwner = identityManager.getUserByKeyId( identityManager.getPeerOwnerId() );
        if ( peerOwner != null )
        {
            // Simply pass key value object as map
            RelationInfoMeta relationInfoMeta =
                    new RelationInfoMeta( true, true, true, true, Ownership.USER.getLevel() );
            Map<String, String> relationTraits = relationInfoMeta.getRelationTraits();
            relationTraits.put( "hostEnvironment", "true" );
            relationTraits.put( "containerLimit", "unlimited" );
            relationTraits.put( "bandwidthLimit", "unlimited" );
            relationTraits.put( "read", "true" );
            relationTraits.put( "write", "true" );
            relationTraits.put( "update", "true" );
            relationTraits.put( "delete", "true" );

            RelationMeta relationMeta = new RelationMeta( peerOwner, this, envLink, this.getKeyId() );
            Relation relation = relationManager.buildRelation( relationInfoMeta, relationMeta );
            relation.setRelationStatus( RelationStatus.VERIFIED );
            relationManager.saveRelation( relation );
        }
        buildRelation( envLink );
    }


    private void buildRelation( final RelationLink envLink )
    {
        try
        {
            RelationLink source;
            String keyId;
            User activeUser = identityManager.getActiveUser();
            if ( activeUser == null || activeUser.getType() == UserType.SYSTEM.getId() )
            {
                // Most probably it is cross peer environment
                source = this;
                keyId = source.getKeyId();
                LOG.debug( "Setting local peer as source" );
            }
            else
            {
                source = identityManager.getUserDelegate( activeUser.getId() );
                keyId = activeUser.getSecurityKeyId();
                LOG.debug( "Extracting delegated user" );
            }

            // User           - Delegated user - Environment
            // Delegated user - Delegated user - Environment
            // Delegated user - Environment    - Container
            RelationInfoMeta relationInfoMeta = new RelationInfoMeta();
            Map<String, String> traits = relationInfoMeta.getRelationTraits();
            traits.put( "read", "true" );
            traits.put( "write", "true" );
            traits.put( "update", "true" );
            traits.put( "delete", "true" );
            traits.put( "ownership", Ownership.USER.getName() );

            if ( source == null )
            {
                LOG.debug( "Source is null" );
            }
            if ( envLink == null )
            {
                LOG.debug( "envLink is null" );
            }

            RelationMeta relationMeta = new RelationMeta( source, source, envLink, keyId );
            Relation relation = relationManager.buildRelation( relationInfoMeta, relationMeta );
            relation.setRelationStatus( RelationStatus.VERIFIED );
            relationManager.saveRelation( relation );
        }
        catch ( Exception e )
        {
            LOG.warn( "Error message.", e );
        }
    }


    @Override
    public void addPeerEnvironmentPubKey( final String keyId, final PGPPublicKeyRing pubRing )
    {
        Preconditions.checkNotNull( keyId );
        Preconditions.checkNotNull( pubRing );

        securityManager.getKeyManager()
                       .savePublicKeyRing( keyId, SecurityKeyType.PEER_ENVIRONMENT_KEY.getId(), pubRing );

        // Build relation between LocalPeer => RemotePeer => Environment
        // for message encryption/decryption mechanism described in relation traits
        String[] ids = keyId.split( "_" );
        if ( ids.length == 2 )
        {
            String envId = ids[1];
            RelationLink envLink = relationManager.getRelationLink( envId );
            RelationLink peerLink = relationManager.getRelationLink( ids[0] );

            RelationInfoMeta relationInfoMeta =
                    new RelationInfoMeta( true, true, true, true, Ownership.USER.getLevel() );
            Map<String, String> relationTraits = relationInfoMeta.getRelationTraits();
            relationTraits.put( "encryptMessage", "true" );
            relationTraits.put( "decryptMessage", "true" );
            relationTraits.put( "ownership", Ownership.USER.getName() );
            relationTraits.put( "read", "true" );
            relationTraits.put( "write", "true" );
            relationTraits.put( "update", "true" );
            relationTraits.put( "delete", "true" );

            RelationMeta relationMeta = new RelationMeta( this, peerLink, envLink, this.getKeyId() );
            Relation relation = relationManager.buildRelation( relationInfoMeta, relationMeta );
            relation.setRelationStatus( RelationStatus.VERIFIED );
            relationManager.saveRelation( relation );
        }
    }


    @Override
    public void updatePeerEnvironmentPubKey( final EnvironmentId environmentId, final PGPPublicKeyRing pubKeyRing )
            throws PeerException
    {
        Preconditions.checkNotNull( environmentId );
        Preconditions.checkNotNull( pubKeyRing );

        securityManager.getKeyManager().updatePublicKeyRing( pubKeyRing );
    }


    @Override
    public synchronized Integer reserveNetworkResource( final NetworkResourceImpl networkResource ) throws PeerException
    {

        Preconditions.checkNotNull( networkResource );

        try
        {
            NetworkResource nr = networkResourceDao.find( networkResource );

            if ( nr != null )
            {
                throw new PeerException( String.format( "Network resource %s is already reserved", nr ) );
            }
            else
            {
                UsedNetworkResources usedNetworkResources = getUsedNetworkResources();

                if ( usedNetworkResources.containerSubnetExists( networkResource.getContainerSubnet() ) )
                {
                    throw new PeerException( String.format( "Container subnet %s is already reserved",
                            networkResource.getContainerSubnet() ) );
                }
                if ( usedNetworkResources.p2pSubnetExists( networkResource.getP2pSubnet() ) )
                {
                    throw new PeerException(
                            String.format( "P2P subnet %s is already reserved", networkResource.getP2pSubnet() ) );
                }
                if ( usedNetworkResources.vniExists( networkResource.getVni() ) )
                {
                    throw new PeerException( String.format( "VNI %d is already reserved", networkResource.getVni() ) );
                }

                //calculate free vlan for this environment
                int freeVlan = usedNetworkResources.calculateFreeVlan();
                if ( freeVlan == -1 )
                {
                    throw new PeerException( "No free VLAN slots are left" );
                }

                NetworkResourceEntity networkResourceEntity = new NetworkResourceEntity( networkResource, freeVlan );

                networkResourceDao.create( networkResourceEntity );

                return freeVlan;
            }
        }
        catch ( Exception e )
        {
            String errMsg = String.format( "Error reserving network resources: %s", e.getMessage() );
            LOG.error( errMsg, e );
            throw new PeerException( errMsg, e );
        }
    }


    @Override
    public ReservedNetworkResources getReservedNetworkResources() throws PeerException
    {
        ReservedNetworkResources reservedNetworkResources = new ReservedNetworkResources();

        try
        {
            for ( NetworkResource networkResource : networkResourceDao.readAll() )
            {
                reservedNetworkResources.addNetworkResource( networkResource );
            }
        }
        catch ( Exception e )
        {
            String errMsg = String.format( "Error getting reserved network resources: %s", e.getMessage() );
            LOG.error( errMsg, e );
            throw new PeerException( errMsg, e );
        }

        return reservedNetworkResources;
    }


    @Override
    public UsedNetworkResources getUsedNetworkResources() throws PeerException
    {
        final UsedNetworkResources usedNetworkResources = new UsedNetworkResources();

        Set<ResourceHost> resourceHostSet = getResourceHosts();

        HostUtil.Tasks hostTasks = new HostUtil.Tasks();

        for ( final ResourceHost resourceHost : resourceHostSet )
        {
            hostTasks.addTask( resourceHost, new UsedHostNetResourcesTask( resourceHost, usedNetworkResources ) );
        }

        HostUtil.Results results = hostUtil.executeFailFast( hostTasks, null );

        if ( results.hasFailures() )
        {
            HostUtil.Task task = results.getFirstFailedTask();

            String errMsg =
                    String.format( "Error gathering reserved net resources on host %s: %s", task.getHost().getId(),
                            task.getFailureReason() );

            LOG.error( errMsg );

            throw new PeerException( errMsg, task.getException() );
        }


        //add reserved ones too
        for ( NetworkResource networkResource : getReservedNetworkResources().getNetworkResources() )
        {
            usedNetworkResources.addVni( networkResource.getVni() );
            usedNetworkResources.addVlan( networkResource.getVlan() );
            usedNetworkResources.addContainerSubnet( networkResource.getContainerSubnet() );
            usedNetworkResources.addP2pSubnet( networkResource.getP2pSubnet() );
        }

        return usedNetworkResources;
    }

    //----------- P2P SECTION BEGIN --------------------


    @RolesAllowed( "Environment-Management|Write" )
    @Override
    public void setupTunnels( final P2pIps p2pIps, final EnvironmentId environmentId ) throws PeerException
    {
        Preconditions.checkNotNull( p2pIps, "Invalid peer ips set" );
        Preconditions.checkNotNull( environmentId, "Invalid environment id" );

        final NetworkResource reservedNetworkResource =
                getReservedNetworkResources().findByEnvironmentId( environmentId.getId() );

        if ( reservedNetworkResource == null )
        {
            throw new PeerException(
                    String.format( "No reserved network resources found for environment %s", environmentId ) );
        }

        Set<ResourceHost> resourceHostSet = getResourceHosts();

        HostUtil.Tasks tasks = new HostUtil.Tasks();

        for ( final ResourceHost resourceHost : resourceHostSet )
        {
            //setup tunnel only if this RH participates in the swarm
            if ( p2pIps.findByRhId( resourceHost.getId() ) != null )
            {
                tasks.addTask( resourceHost, new SetupTunnelsTask( resourceHost, p2pIps, reservedNetworkResource ) );
            }
        }

        HostUtil.Results results = hostUtil.executeFailFast( tasks, reservedNetworkResource.getEnvironmentId() );

        if ( results.hasFailures() )
        {
            HostUtil.Task task = results.getFirstFailedTask();

            String errMsg = String.format( "Error setting up tunnels on host %s: %s", task.getHost().getId(),
                    task.getFailureReason() );

            LOG.error( errMsg );

            throw new PeerException( errMsg, task.getException() );
        }
    }


    @Override
    public void deleteTunnels( P2pIps p2pIps, EnvironmentId environmentId ) throws PeerException
    {
        Preconditions.checkNotNull( p2pIps, "Invalid peer ips set" );
        Preconditions.checkNotNull( environmentId, "Invalid environment id" );

        final NetworkResource reservedNetworkResource =
                getReservedNetworkResources().findByEnvironmentId( environmentId.getId() );

        if ( reservedNetworkResource == null )
        {
            throw new PeerException(
                    String.format( "No reserved network resources found for environment %s", environmentId ) );
        }

        Set<ResourceHost> resourceHostSet = getResourceHosts();

        HostUtil.Tasks tasks = new HostUtil.Tasks();

        for ( final ResourceHost resourceHost : resourceHostSet )
        {
            tasks.addTask( resourceHost, new DeleteTunnelsTask( resourceHost, p2pIps, reservedNetworkResource ) );
        }

        HostUtil.Results results = hostUtil.execute( tasks, reservedNetworkResource.getEnvironmentId() );

        if ( results.hasFailures() )
        {
            String errMsg = "Error deleting tunnels across all RHs: " + results.getFirstFailedTask().getFailureReason();

            LOG.error( errMsg );

            throw new PeerException( errMsg );
        }
    }


    @Override
    public void resetSwarmSecretKey( final P2PCredentials p2PCredentials ) throws PeerException
    {

        Preconditions.checkNotNull( p2PCredentials, "Invalid p2p credentials" );

        Set<ResourceHost> resourceHostSet = getResourceHosts();

        HostUtil.Tasks tasks = new HostUtil.Tasks();

        for ( final ResourceHost resourceHost : resourceHostSet )
        {
            tasks.addTask( resourceHost, new ResetP2PSwarmSecretTask( resourceHost, p2PCredentials.getP2pHash(),
                    p2PCredentials.getP2pSecretKey(), p2PCredentials.getP2pTtlSeconds() ) );
        }

        HostUtil.Results results = hostUtil.executeFailFast( tasks, p2PCredentials.getEnvironmentId() );

        if ( results.hasFailures() )
        {
            HostUtil.Task task = results.getFirstFailedTask();

            String errMsg = String.format( "Error resetting P2P secret key on host %s: %s", task.getHost().getId(),
                    task.getFailureReason() );

            LOG.error( errMsg );

            throw new PeerException( errMsg, task.getException() );
        }
    }


    @RolesAllowed( "Environment-Management|Update" )
    @Override
    public void joinP2PSwarm( final P2PConfig config ) throws PeerException
    {
        Preconditions.checkNotNull( config, "Invalid p2p config" );

        NetworkResource reservedNetworkResource =
                getReservedNetworkResources().findByEnvironmentId( config.getEnvironmentId() );

        if ( reservedNetworkResource == null )
        {
            throw new PeerException(
                    String.format( "Reserved vni not found for environment %s", config.getEnvironmentId() ) );
        }

        final String p2pInterface = P2PUtil.generateInterfaceName( reservedNetworkResource.getVlan() );

        HostUtil.Tasks tasks = new HostUtil.Tasks();

        for ( final RhP2pIp rhP2pIp : config.getRhP2pIps() )
        {
            final ResourceHost resourceHost = getResourceHostById( rhP2pIp.getRhId() );

            tasks.addTask( resourceHost,
                    new JoinP2PSwarmTask( resourceHost, rhP2pIp.getP2pIp(), p2pInterface, config.getHash(),
                            config.getSecretKey(), config.getSecretKeyTtlSec() ) );
        }

        HostUtil.Results results = hostUtil.executeFailFast( tasks, reservedNetworkResource.getEnvironmentId() );

        if ( results.hasFailures() )
        {
            HostUtil.Task task = results.getFirstFailedTask();

            String errMsg = String.format( "Error joining P2P swarm on host %s: %s", task.getHost().getId(),
                    task.getFailureReason() );

            LOG.error( errMsg );

            throw new PeerException( errMsg, task.getException() );
        }
    }


    @Override
    public void joinOrUpdateP2PSwarm( final P2PConfig config ) throws PeerException
    {
        Preconditions.checkNotNull( config, "Invalid p2p config" );

        NetworkResource reservedNetworkResource =
                getReservedNetworkResources().findByEnvironmentId( config.getEnvironmentId() );

        if ( reservedNetworkResource == null )
        {
            throw new PeerException(
                    String.format( "Reserved vni not found for environment %s", config.getEnvironmentId() ) );
        }

        final String p2pInterface = P2PUtil.generateInterfaceName( reservedNetworkResource.getVlan() );

        Set<ResourceHost> resourceHostSet = getResourceHosts();

        HostUtil.Tasks tasks = new HostUtil.Tasks();

        for ( final ResourceHost resourceHost : resourceHostSet )
        {
            final RhP2pIp rhP2pIp = config.findByRhId( resourceHost.getId() );

            if ( rhP2pIp != null )
            {
                //try to join RH (updates if already participating)
                tasks.addTask( resourceHost,
                        new JoinP2PSwarmTask( resourceHost, rhP2pIp.getP2pIp(), p2pInterface, config.getHash(),
                                config.getSecretKey(), config.getSecretKeyTtlSec() ) );
            }
            else
            {
                //try to update missing RH in case it participates in the swarm
                tasks.addTask( resourceHost,
                        new ResetP2PSwarmSecretTask( resourceHost, config.getHash(), config.getSecretKey(),
                                config.getSecretKeyTtlSec() ) );
            }
        }

        HostUtil.Results results = hostUtil.executeFailFast( tasks, reservedNetworkResource.getEnvironmentId() );

        if ( results.hasFailures() )
        {
            HostUtil.Task task = results.getFirstFailedTask();

            String errMsg = String.format( "Error joining/updating P2P swarm on host %s: %s", task.getHost().getId(),
                    task.getFailureReason() );

            LOG.error( errMsg );

            throw new PeerException( errMsg, task.getException() );
        }
    }


    //----------- P2P SECTION END --------------------


    @RolesAllowed( "Environment-Management|Delete" )
    @Override
    public void cleanupEnvironment( final EnvironmentId environmentId ) throws PeerException
    {
        cleanupEnvironment( environmentId, true );
    }


    private void cleanupEnvironment( EnvironmentId environmentId, boolean removePEK ) throws PeerException
    {
        Preconditions.checkNotNull( environmentId );

        final NetworkResource reservedNetworkResource =
                getReservedNetworkResources().findByEnvironmentId( environmentId.getId() );

        if ( reservedNetworkResource == null )
        {
            LOG.warn( "Network reservation for environment {} not found", environmentId.getId() );
            return;
        }

        //interrupt active environment operations
        boolean hasActiveTasks = hostUtil.cancelEnvironmentTasks( environmentId.getId() );

        hasActiveTasks |= commandUtil.cancelEnvironmentCommands( environmentId.getId() );

        if ( hasActiveTasks )
        {
            //await clone commands on agent to complete, best effort
            TaskUtil.sleep( 10 * 1000L ); // 10 sec
        }


        //send cleanup command to RHs
        Set<ResourceHost> resourceHostSet = getResourceHosts();

        HostUtil.Tasks tasks = new HostUtil.Tasks();

        for ( final ResourceHost resourceHost : resourceHostSet )
        {
            tasks.addTask( resourceHost, new CleanupEnvironmentTask( resourceHost, reservedNetworkResource ) );
        }

        hostUtil.submit( tasks, reservedNetworkResource.getEnvironmentId() );


        try
        {
            //remove PEK
            KeyManager keyManager = securityManager.getKeyManager();

            if ( removePEK )
            {
                keyManager.removeKeyData( environmentId.getId() );

                keyManager.removeKeyData( getId() + "_" + environmentId.getId() );
            }

            //remove container keys
            Containers containers = getEnvironmentContainers( environmentId );

            for ( final ContainerHostInfo containerHostInfo : containers.getContainers() )
            {
                keyManager.removeKeyData( containerHostInfo.getId() );
            }
        }
        catch ( Exception e )
        {
            LOG.error( "Failed to delete PEK for environment {}", environmentId.getId(), e );
        }

        //remove reservation
        try
        {
            networkResourceDao.delete( ( NetworkResourceEntity ) reservedNetworkResource );
        }
        catch ( DaoException e )
        {
            LOG.error( "Failed to delete network reservation for environment {}", environmentId.getId(), e );
        }
    }


    @Override
    public ResourceHostMetrics getResourceHostMetrics()
    {
        return monitor.getResourceHostMetrics();
    }


    @Override
    public PeerResources getResourceLimits( PeerId peerId )
    {
        PeerPolicy policy = getPeerManager().getPolicy( peerId.getId() );
        int environmentLimit = policy.getEnvironmentLimit();
        int containerLimit = policy.getContainerLimit();
        int networkLimit = policy.getNetworkUsageLimit();

        Set<String> environments = new HashSet<>();
        final List<ContainerHost> peerContainers = getPeerContainers( peerId.getId() );
        for ( ContainerHost containerHost : peerContainers )
        {
            environments.add( containerHost.getEnvironmentId().getId() );
        }

        environmentLimit -= environments.size();
        containerLimit -= peerContainers.size();

        ResourceHostMetrics metrics = getResourceHostMetrics();

        List<HostResources> resources = new ArrayList<>();

        if ( metrics != null )
        {
            for ( ResourceHostMetric resourceHostMetric : metrics.getResources() )
            {
                try
                {
                    ResourceHost resourceHost = getResourceHostByHostName( resourceHostMetric.getHostName() );
                    BigDecimal[] usedResources = getUsedResources();

                    BigDecimal cpuLimit = getCpuLimit( policy );

                    BigDecimal ramLimit = getRamLimit( new BigDecimal( resourceHostMetric.getTotalRam() ), policy );

                    BigDecimal diskLimit = getDiskLimit( new BigDecimal( resourceHostMetric.getTotalSpace() ), policy );

                    CpuResource cpuResource = new CpuResource( cpuLimit.subtract( usedResources[0] ), 0.0, "UNKNOWN",
                            resourceHostMetric.getCpuCore(), 0, 0, 0, resourceHostMetric.getCpuFrequency(), 0 );

                    RamResource ramResource = new RamResource( ramLimit.subtract( usedResources[1] ), 0.0 );

                    DiskResource diskResource =
                            new DiskResource( diskLimit.subtract( usedResources[2] ), 0.0, "UNKNOWN", 0.0, 0.0, false );


                    HostResources hostResources =
                            new HostResources( resourceHost.getId(), cpuResource, ramResource, diskResource );
                    resources.add( hostResources );
                }
                catch ( Exception e )
                {
                    // ignore
                    LOG.warn( e.getMessage() );
                }
            }
        }

        return new PeerResources( getId(), environmentLimit, containerLimit, networkLimit, resources );
    }


    private BigDecimal getRamLimit( final BigDecimal total, final PeerPolicy peerPolicy )
    {
        return percentage( total, new BigDecimal( peerPolicy.getMemoryUsageLimit() ) );
    }


    private BigDecimal getDiskLimit( final BigDecimal total, final PeerPolicy peerPolicy )
    {
        return percentage( total, new BigDecimal( peerPolicy.getDiskUsageLimit() ) );
    }


    private BigDecimal getCpuLimit( final PeerPolicy peerPolicy )
    {
        return percentage( ONE_HUNDRED, new BigDecimal( peerPolicy.getCpuUsageLimit() ) );
    }


    private BigDecimal[] getUsedResources() throws QuotaException
    {
        BigDecimal cpuAccumulo = BigDecimal.ZERO;
        BigDecimal ramAccumulo = BigDecimal.ZERO;
        BigDecimal diskAccumulo = BigDecimal.ZERO;
        // todo: extract from DB

        return new BigDecimal[] { cpuAccumulo, ramAccumulo, diskAccumulo };
    }


    private static BigDecimal percentage( BigDecimal base, BigDecimal pct )
    {
        return base.multiply( pct ).divide( ONE_HUNDRED, BigDecimal.ROUND_UP );
    }


    @Override
    public PeerResources getResources()
    {
        int environmentLimit = 100;
        int containerLimit = 200;
        int networkLimit = 100;

        ResourceHostMetrics metrics = getResourceHostMetrics();

        List<HostResources> resources = new ArrayList<>();

        for ( ResourceHostMetric resourceHostMetric : metrics.getResources() )
        {
            try
            {
                ResourceHost resourceHost = getResourceHostByHostName( resourceHostMetric.getHostName() );

                BigDecimal cpuLimit = new BigDecimal( "100.00" );

                BigDecimal ramLimit = new BigDecimal( resourceHostMetric.getTotalRam() );

                BigDecimal diskLimit = new BigDecimal( resourceHostMetric.getTotalSpace() );

                CpuResource cpuResource =
                        new CpuResource( cpuLimit, 0.0, "UNKNOWN", resourceHostMetric.getCpuCore(), 0, 0, 0,
                                resourceHostMetric.getCpuFrequency(), 0 );

                RamResource ramResource = new RamResource( ramLimit, 0.0 );

                DiskResource diskResource = new DiskResource( diskLimit, 0.0, "UNKNOWN", 0.0, 0.0, false );


                HostResources hostResources =
                        new HostResources( resourceHost.getId(), cpuResource, ramResource, diskResource );
                resources.add( hostResources );
            }
            catch ( HostNotFoundException e )
            {
                // ignore
            }
        }


        return new PeerResources( getId(), environmentLimit, containerLimit, networkLimit, resources );
    }


    @Override
    public Set<Template> getTemplates()
    {
        return templateManager.getTemplates();
    }


    @Override
    public Template getTemplateByName( final String templateName )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( templateName ), "Invalid template name" );

        return templateManager.getTemplateByName( templateName );
    }


    @Override
    public Template getTemplateById( final String templateId ) throws PeerException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( templateId ), "Invalid template id" );

        return templateManager.getTemplate( templateId );
    }


    @Override
    public Quota getQuota( final ContainerId containerId, ContainerResourceType containerResourceType )
            throws PeerException
    {
        Preconditions.checkNotNull( containerId );
        try
        {
            ResourceHost resourceHost = getResourceHostByContainerId( containerId.getId() );
            CommandResult result = resourceHost
                    .execute( Commands.getReadQuotaCommand( containerId.getContainerName(), containerResourceType ) );
            QuotaOutput quotaOutput = mapper.readValue( result.getStdOut(), QuotaOutput.class );
            ResourceValue resourceValue =
                    CommonResourceValueParser.parse( quotaOutput.getQuota(), containerResourceType );

            ContainerResource containerResource =
                    ContainerResourceFactory.createContainerResource( containerResourceType, resourceValue );
            return new Quota( containerResource, quotaOutput.getThreshold() );
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            throw new PeerException( String.format( "Could not obtain %s quota value of %s.", containerResourceType,
                    containerId.getId() ) );
        }
    }


    @Override
    public io.subutai.common.host.Quota getRawQuota( final ContainerId containerId ) throws PeerException
    {
        Preconditions.checkNotNull( containerId );

        try
        {
            ContainerHostInfo containerHostInfo =
                    ( ContainerHostInfo ) hostRegistry.getHostInfoById( containerId.getId() );

            io.subutai.common.host.Quota quota = containerHostInfo.getRawQuota();

            if ( quota == null )
            {
                return new io.subutai.common.host.Quota( 0D, 0D, 0D );
            }

            //temp workaround for btrfs quota issue https://github.com/subutai-io/agent/wiki/Switch-to-Soft-Quota
            return new io.subutai.common.host.Quota( quota.getCpu(), quota.getRam(), quota.getDisk() / 2 );
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            throw new PeerException( String.format( "Error getting container quota: %s", e.getMessage() ), e );
        }
    }


    //TODO review this method
    @Override
    public ContainerQuota getQuota( final ContainerId containerId ) throws PeerException
    {
        final ContainerHost containerHost = getContainerHostById( containerId.getId() );
        ContainerQuota containerQuota = new ContainerQuota( containerHost.getContainerSize() );
        try
        {

            try
            {
                final ContainerHostInfo hostInfo = hostRegistry.getContainerHostInfoById( containerId.getId() );
                List<Quota> quota = buildQuota( hostInfo.getRawQuota() );
                containerQuota.addAll( quota );
            }
            catch ( Exception e )
            {
                ResourceHost resourceHost = getResourceHostByContainerId( containerId.getId() );
                CommandResult result =
                        resourceHost.execute( Commands.getReadQuotaCommand( containerHost.getContainerName() ) );

                JavaType type = mapper.getTypeFactory().constructCollectionType( List.class, BatchOutput.class );
                List<BatchOutput> outputs = mapper.readValue( result.getStdOut(), type );

                for ( int i = 0; i < outputs.size(); i++ )
                {
                    QuotaOutput quotaOutput = outputs.get( i ).getOutput();

                    ContainerResourceType containerResourceType = ContainerResourceType.values()[i];

                    ResourceValue resourceValue =
                            CommonResourceValueParser.parse( quotaOutput.getQuota(), containerResourceType );


                    ContainerResource containerResource =
                            ContainerResourceFactory.createContainerResource( containerResourceType, resourceValue );

                    containerQuota.add( new Quota( containerResource, quotaOutput.getThreshold() ) );
                }

                //todo here adjust disk quota by dividing by 2
            }
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            throw new PeerException( String.format( "Could not obtain quota values of %s.", containerId.getId() ) );
        }

        //temp workaround for btrfs quota issue https://github.com/subutai-io/agent/wiki/Switch-to-Soft-Quota
        if ( containerQuota.getContainerSize() == ContainerSize.CUSTOM )
        {

            containerQuota.add( new Quota( new ContainerDiskResource(
                    containerQuota.get( ContainerResourceType.DISK ).getAsDiskResource().longValue( ByteUnit.GB ) / 2,
                    ByteUnit.GB ), 0 ) );
        }

        return containerQuota;
    }


    private List<Quota> buildQuota( final io.subutai.common.host.Quota rawQuota )
    {
        // TODO: 2/17/17 add quota thresholds after implementing in system level
        List<Quota> result = new ArrayList<>();

        if ( rawQuota.getCpu() != null )
        {
            Quota cpuQuota = new Quota( new ContainerCpuResource( rawQuota.getCpu() ), 0 );
            result.add( cpuQuota );
        }

        if ( rawQuota.getRam() != null )
        {
            Quota ramQuota = new Quota( new ContainerRamResource( rawQuota.getRam(), ByteUnit.MB ), 0 );
            result.add( ramQuota );
        }

        if ( rawQuota.getDisk() != null )
        {
            Quota diskQuota = new Quota( new ContainerDiskResource( rawQuota.getDisk(), ByteUnit.GB ), 0 );
            result.add( diskQuota );
        }

        return result;
    }


    @Override
    public void setQuota( final ContainerId containerId, final ContainerQuota containerQuota ) throws PeerException
    {
        Preconditions.checkNotNull( containerId );
        Preconditions.checkNotNull( containerQuota );

        ContainerQuota quota = ContainerSize.getDefaultContainerQuota( containerQuota.getContainerSize() );
        // CUSTOM value of container size returns null quota
        if ( quota == null )
        {
            quota = ContainerSize.getDefaultContainerQuota( ContainerSize.SMALL );
        }

        if ( containerQuota.getContainerSize() == ContainerSize.CUSTOM || quota.getAll().isEmpty() )
        {
            quota.copyValues( containerQuota );
        }

        try
        {
            ContainerHost containerHost = getContainerHostById( containerId.getId() );

            ResourceHost resourceHost = getResourceHostById( containerHost.getResourceHostId().getId() );

            resourceHost.setContainerQuota( containerHost, quota );

            containerHost.setContainerSize( quota.getContainerSize() );

            resourceHostDataService.update( ( ResourceHostEntity ) resourceHost );
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            throw new PeerException(
                    String.format( "Could not set container quota for %s: %s", containerId.getId(), e.getMessage() ) );
        }
    }


    @Override
    public void alert( AlertEvent alert )
    {
        Preconditions.checkNotNull( alert );

        monitor.addAlert( alert );
    }


    @Override
    public String getHistoricalMetrics( final HostId hostId, final Date startTime, final Date endTime )
            throws PeerException
    {
        Preconditions.checkNotNull( hostId );
        Preconditions.checkNotNull( startTime );
        Preconditions.checkNotNull( endTime );

        try
        {
            Host host = findHost( hostId.getId() );
            return monitor.getHistoricalMetrics( host, startTime, endTime );
        }
        catch ( HostNotFoundException e )
        {
            LOG.error( e.getMessage(), e );
            throw new PeerException( e.getMessage(), e );
        }
    }


    @Override
    public HistoricalMetrics getMetricsSeries( final HostId hostId, final Date startTime, final Date endTime )
            throws PeerException
    {
        Preconditions.checkNotNull( hostId );
        Preconditions.checkNotNull( startTime );
        Preconditions.checkNotNull( endTime );

        try
        {
            Host host = findHost( hostId.getId() );
            return monitor.getMetricsSeries( host, startTime, endTime );
        }
        catch ( HostNotFoundException e )
        {
            LOG.error( e.getMessage(), e );
            throw new PeerException( e.getMessage(), e );
        }
    }


    @Override
    public void addCustomProxy( final CustomProxyConfig proxyConfig ) throws PeerException
    {
        Preconditions.checkNotNull( proxyConfig, "Invalid proxy config" );

        ContainerHost containerHost = getContainerHostById( proxyConfig.getContainerId() );

        try
        {
            getNetworkManager().addCustomProxy( proxyConfig, containerHost );
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            throw new PeerException( String.format( "Error on adding custom reverse proxy: %s", e.getMessage() ) );
        }
    }


    @Override
    public void removeCustomProxy( final CustomProxyConfig proxyConfig ) throws PeerException
    {
        Preconditions.checkNotNull( proxyConfig, "Invalid proxy config" );

        try
        {
            getNetworkManager().removeCustomProxy( proxyConfig.getVlan() );
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            throw new PeerException( String.format( "Error on removing custom reverse proxy: %s", e.getMessage() ) );
        }
    }


    @Override
    public void setContainerHostname( final ContainerId containerId, final String hostname ) throws PeerException
    {
        Preconditions.checkNotNull( containerId, "Invalid container id" );

        ContainerHost containerHost = getContainerHostById( containerId.getId() );

        String newHostname = StringUtil.removeHtmlAndSpecialChars( hostname, true );

        Preconditions.checkArgument( !Strings.isNullOrEmpty( newHostname ), "Invalid hostname" );

        //check if container with new hostname already exists on peer
        try
        {
            getContainerHostByHostName( newHostname );

            throw new PeerException( String.format( "Container with hostname %s already exists", newHostname ) );
        }
        catch ( HostNotFoundException ignore )
        {
            //ignore since all is ok
        }

        try
        {
            ResourceHost resourceHost = getResourceHostById( containerHost.getResourceHostId().getId() );

            resourceHost.setContainerHostname( containerHost, newHostname );
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            throw new PeerException(
                    String.format( "Error setting container %s hostname: %s", containerId.getId(), e.getMessage() ) );
        }
    }


    @Override
    public void setRhHostname( final String rhId, final String hostname ) throws PeerException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( rhId ), "Invalid RH id" );

        ResourceHost resourceHost = getResourceHostById( rhId );

        String newHostname = StringUtil.removeHtmlAndSpecialChars( hostname, true );

        Preconditions.checkArgument( !Strings.isNullOrEmpty( newHostname ), "Invalid hostname" );


        //check if RH with new hostname already exists on peer
        try
        {
            getResourceHostByHostName( newHostname );

            throw new PeerException( String.format( "RH with hostname %s already exists", newHostname ) );
        }
        catch ( HostNotFoundException ignore )
        {
            //ignore since all is ok
        }

        try
        {
            resourceHost.setHostname( newHostname );
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            throw new PeerException(
                    String.format( "Error setting RH %s hostname: %s", resourceHost.getId(), e.getMessage() ) );
        }
    }


    @Override
    public void updateEtcHostsWithNewContainerHostname( final EnvironmentId environmentId, final String oldHostname,
                                                        final String newHostname ) throws PeerException
    {
        Preconditions.checkNotNull( environmentId );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( oldHostname ) );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( newHostname ) );

        Set<Host> hosts = Sets.newHashSet();

        hosts.addAll( findContainersByEnvironmentId( environmentId.getId() ) );

        if ( hosts.isEmpty() )
        {
            return;
        }

        CommandUtil.HostCommandResults results = commandUtil
                .execute( localPeerCommands.getChangeHostnameInEtcHostsCommand( oldHostname, newHostname ), hosts,
                        environmentId.getId() );

        for ( CommandUtil.HostCommandResult result : results.getCommandResults() )
        {
            if ( !result.hasSucceeded() )
            {
                LOG.error( "Failed to update hosts on host {}: {}", result.getHost().getHostname(),
                        result.getFailureReason() );
            }
        }

        if ( results.hasFailures() )
        {
            throw new PeerException( "Failed to update hosts on every host" );
        }
    }


    @Override
    public void updateAuthorizedKeysWithNewContainerHostname( final EnvironmentId environmentId,
                                                              final String oldHostname, final String newHostname,
                                                              final SshEncryptionType sshEncryptionType )
            throws PeerException
    {
        Preconditions.checkNotNull( environmentId );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( oldHostname ) );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( newHostname ) );
        Preconditions.checkNotNull( sshEncryptionType );

        Set<Host> hosts = Sets.newHashSet();

        hosts.addAll( findContainersByEnvironmentId( environmentId.getId() ) );

        if ( hosts.isEmpty() )
        {
            return;
        }

        for ( Host containerHost : hosts )
        {
            if ( containerHost.getHostname().equalsIgnoreCase( newHostname ) )
            {
                try
                {
                    commandUtil.execute( localPeerCommands
                                    .getChangeHostnameInSshPubKeyCommand( oldHostname, newHostname, sshEncryptionType ),
                            containerHost );
                }
                catch ( CommandException e )
                {
                    LOG.error( "Error updating ssh pub key with hostname change: {}", e.getMessage() );
                }

                break;
            }
        }

        CommandUtil.HostCommandResults results = commandUtil
                .execute( localPeerCommands.getChangeHostnameInAuthorizedKeysCommand( oldHostname, newHostname ), hosts,
                        environmentId.getId() );

        for ( CommandUtil.HostCommandResult result : results.getCommandResults() )
        {
            if ( !result.hasSucceeded() )
            {
                LOG.error( "Failed to update authorized keys on host {}: {}", result.getHost().getHostname(),
                        result.getFailureReason() );
            }
        }

        if ( results.hasFailures() )
        {
            throw new PeerException( "Failed to update authorized keys on every host" );
        }
    }


    private NetworkManager getNetworkManager()
    {

        return serviceLocator.getService( NetworkManager.class );
    }


    @Override
    public Set<HostUtil.Task> getTasks()
    {
        return hostUtil.getAllTasks();
    }


    @Override
    public void cancelAllTasks()
    {
        hostUtil.cancelAll();
    }


    @Override
    public HostId getResourceHostIdByContainerId( final ContainerId id ) throws PeerException
    {
        return new HostId( getResourceHostByContainerId( id.getId() ).getId() );
    }


    @Override
    public RegistrationStatus getStatus()
    {
        return RegistrationStatus.APPROVED;
    }


    @Override
    public Set<ContainerHostInfo> getNotRegisteredContainers()
    {
        Set<ContainerHostInfo> containerHostInfos = hostRegistry.getContainerHostsInfo();

        Set<ContainerHost> registeredContainers = getRegisteredContainers();

        for ( Iterator<ContainerHostInfo> iterator = containerHostInfos.iterator(); iterator.hasNext(); )
        {
            final ContainerHostInfo containerHostInfo = iterator.next();

            boolean registered = false;

            for ( ContainerHost registeredContainer : registeredContainers )
            {
                if ( containerHostInfo.getId().equals( registeredContainer.getId() ) )
                {
                    registered = true;

                    break;
                }
            }

            if ( Common.MANAGEMENT_HOSTNAME.equalsIgnoreCase( containerHostInfo.getContainerName() ) || registered )
            {
                iterator.remove();
            }
        }

        return containerHostInfos;
    }


    /**
     * Destroys only not registered container
     *
     * @return - true if container is not registered and destroyed, false otherwise
     */
    @Override
    public boolean destroyNotRegisteredContainer( final String containerId ) throws PeerException
    {
        ContainerHostInfo containerHost = null;

        for ( ContainerHostInfo containerHostInfo : getNotRegisteredContainers() )
        {
            if ( containerHostInfo.getId().equals( containerId ) )
            {
                containerHost = containerHostInfo;

                break;
            }
        }

        if ( containerHost != null )
        {
            try
            {
                commandExecutor.execute( hostRegistry.getResourceHostByContainerHost( containerHost ).getId(),
                        localPeerCommands.getDestroyContainerCommand( containerHost.getContainerName() ) );
            }
            catch ( Exception e )
            {
                throw new PeerException( e );
            }

            return true;
        }

        return false;
    }


    @Override
    public Set<ContainerHost> getRegisteredContainers()
    {
        Set<ContainerHost> result = Sets.newHashSet();

        for ( ResourceHost resourceHost : getResourceHosts() )
        {
            result.addAll( resourceHost.getContainerHosts() );
        }

        return result;
    }


    @Override
    public Set<ContainerHost> getOrphanContainers()
    {
        Set<ContainerHost> result = new HashSet<>();
        Set<String> involvedPeers = getInvolvedPeers();
        final Set<String> unregisteredPeers = getNotRegisteredPeers( involvedPeers );
        for ( ResourceHost resourceHost : getResourceHosts() )
        {
            for ( ContainerHost containerHost : resourceHost.getContainerHosts() )
            {
                if ( unregisteredPeers.contains( containerHost.getInitiatorPeerId() ) && !Common.MANAGEMENT_HOSTNAME
                        .equalsIgnoreCase( containerHost.getContainerName() ) )
                {
                    result.add( containerHost );
                }
            }
        }
        return result;
    }


    private Set<String> getInvolvedPeers()
    {
        final Set<String> result = new HashSet<>();
        for ( ResourceHost resourceHost : getResourceHosts() )
        {
            for ( ContainerHost containerHost : resourceHost.getContainerHosts() )
            {
                result.add( containerHost.getInitiatorPeerId() );
            }
        }
        return result;
    }


    private Set<String> getNotRegisteredPeers( Set<String> peers )
    {
        final Set<String> result = new HashSet<>();
        PeerManager peerManager = getPeerManager();

        for ( String p : peers )
        {
            if ( RegistrationStatus.NOT_REGISTERED == peerManager.getRemoteRegistrationStatus( p ) )
            {
                result.add( p );
            }
        }
        return result;
    }


    protected PeerManager getPeerManager()
    {
        return ServiceLocator.lookup( PeerManager.class );
    }


    @Override
    public void removeOrphanContainers()
    {
        Set<ContainerHost> orphanContainers = getOrphanContainers();

        for ( ContainerHost containerHost : orphanContainers )
        {
            try
            {
                destroyContainer( containerHost.getContainerId() );
            }
            catch ( PeerException e )
            {
                LOG.error( "Error on destroying container", e );
            }
        }
    }


    @Override
    public PeerTemplatesDownloadProgress getTemplateDownloadProgress( final EnvironmentId environmentId )
            throws PeerException
    {
        Preconditions.checkNotNull( environmentId, "Invalid environment id" );

        PeerTemplatesDownloadProgress peerProgress = new PeerTemplatesDownloadProgress( getId() );

        List<ResourceHost> resourceHosts = Lists.newArrayList( getResourceHosts() );

        Collections.sort( resourceHosts, new Comparator<ResourceHost>()
        {
            @Override
            public int compare( final ResourceHost o1, final ResourceHost o2 )
            {
                return o1.getId().compareTo( o2.getId() );
            }
        } );

        for ( ResourceHost resourceHost : resourceHosts )
        {
            RhTemplatesDownloadProgress rhProgress = resourceHost.getTemplateDownloadProgress( environmentId.getId() );

            //add only RH with existing progress
            if ( !rhProgress.getTemplatesDownloadProgresses().isEmpty() )
            {
                peerProgress.addTemplateDownloadProgress( rhProgress );
            }
        }

        return peerProgress;
    }


    @Override
    public PeerTemplatesUploadProgress getTemplateUploadProgress( final String templateName ) throws PeerException
    {
        Preconditions.checkNotNull( templateName, "Invalid template name" );

        PeerTemplatesUploadProgress peerProgress = new PeerTemplatesUploadProgress( getId() );

        List<ResourceHost> resourceHosts = Lists.newArrayList( getResourceHosts() );

        Collections.sort( resourceHosts, new Comparator<ResourceHost>()
        {
            @Override
            public int compare( final ResourceHost o1, final ResourceHost o2 )
            {
                return o1.getId().compareTo( o2.getId() );
            }
        } );

        for ( ResourceHost resourceHost : resourceHosts )
        {
            RhTemplatesUploadProgress rhProgress = resourceHost.getTemplateUploadProgress( templateName );

            //add only RH with existing progress
            if ( !rhProgress.getTemplatesUploadProgresses().isEmpty() )
            {
                peerProgress.addTemplateUploadProgress( rhProgress );
            }
        }

        return peerProgress;
    }


    @Override
    public void exportTemplate( final ContainerId containerId, final String templateName, final String version,
                                final boolean isPrivateTemplate, final String token ) throws PeerException
    {
        Preconditions.checkNotNull( containerId, "Invalid container id" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( version ), "Invalid version" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( token ), "Invalid token" );

        ResourceHost resourceHost = getResourceHostByContainerId( containerId.getId() );

        try
        {
            resourceHost
                    .exportTemplate( containerId.getContainerName(), templateName, version, isPrivateTemplate, token );
        }
        catch ( ResourceHostException e )
        {
            LOG.error( e.getMessage(), e );
            throw new PeerException( String.format( "Error exporting template: %s", e.getMessage() ) );
        }
    }

    //**************************


    @Override
    public boolean equals( final Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( !( o instanceof LocalPeerImpl ) )
        {
            return false;
        }

        final LocalPeerImpl that = ( LocalPeerImpl ) o;

        return getId().equals( that.getId() );
    }


    @Override
    public int hashCode()
    {
        return getId().hashCode();
    }


    @Override
    public String getLinkId()
    {
        return String.format( "%s|%s", getClassPath(), getUniqueIdentifier() );
    }


    @Override
    public String getUniqueIdentifier()
    {
        return getId();
    }


    @Override
    public String getClassPath()
    {
        return this.getClass().getSimpleName();
    }


    @Override
    public String getContext()
    {
        return PermissionObject.PEER_MANAGEMENT.getName();
    }


    @Override
    public String getKeyId()
    {
        return getId();
    }


    public void addListener( LocalPeerEventListener listener )
    {
        if ( listener != null )
        {
            peerEventListeners.add( listener );
        }
    }


    public void removeListener( LocalPeerEventListener listener )
    {
        if ( listener != null )
        {
            peerEventListeners.remove( listener );
        }
    }


    private void removeStaleContainers()
    {
        for ( ResourceHost resourceHost : getResourceHosts() )
        {
            if ( !resourceHost.isConnected() )
            {
                continue;
            }

            //destroy containers that are registered with Console but don't have environments
            Set<ContainerHost> containersWithoutEnvironment = Sets.newHashSet();
            try
            {
                ReservedNetworkResources reservedNetworkResources = getReservedNetworkResources();

                for ( ContainerHost containerHost : resourceHost.getContainerHosts() )
                {
                    if ( reservedNetworkResources.findByEnvironmentId( containerHost.getEnvId() ) == null
                            && !Common.MANAGEMENT_HOSTNAME.equalsIgnoreCase( containerHost.getContainerName() ) )
                    {
                        containersWithoutEnvironment.add( containerHost );
                    }
                }
            }
            catch ( Exception e )
            {
                LOG.error( e.getMessage(), e );
            }

            //destroy container without environments
            for ( ContainerHost containerHost : containersWithoutEnvironment )
            {
                try
                {
                    destroyContainer( containerHost.getContainerId() );
                }
                catch ( PeerException e )
                {
                    LOG.error( e.getMessage(), e );
                }
            }

            try
            {
                Set<ContainerInfo> existingContainers = resourceHost.listExistingContainersInfo();

                //remove stale containers (the ones that are registered with Console but not present on RH)
                for ( ContainerHost containerHost : resourceHost.getContainerHosts() )
                {
                    boolean isContainerEligibleForRemoval = containerHost.getState() == ContainerHostState.UNKNOWN &&
                            ( System.currentTimeMillis() - ( ( ContainerHostEntity ) containerHost )
                                    .getLastHeartbeat() ) > TimeUnit.MINUTES.toMillis( 60 )
                            && !Common.MANAGEMENT_HOSTNAME.equalsIgnoreCase( containerHost.getContainerName() );

                    if ( !isContainerEligibleForRemoval )
                    {
                        continue;
                    }

                    boolean found = false;

                    for ( ContainerInfo containerInfo : existingContainers )
                    {
                        if ( containerHost.getContainerName().equalsIgnoreCase( containerInfo.getName() ) )
                        {
                            found = true;
                            break;
                        }
                    }

                    if ( !found )
                    {
                        LOG.warn( "Removing stale container {}", containerHost.getContainerName() );

                        resourceHost.removeContainerHost( containerHost );

                        notifyPeerEventListeners( containerHost );
                    }
                }


                // destroy dead containers (the ones that exist on RH but not appearing in heartbeats and
                // have a STOPPED state )
                Set<String> deadContainerNames = Sets.newHashSet();
                for ( ContainerInfo containerInfo : existingContainers )
                {
                    if ( !Common.MANAGEMENT_HOSTNAME.equalsIgnoreCase( containerInfo.getName() )
                            && ContainerHostState.STOPPED.equals( containerInfo.getState() ) )
                    {
                        try
                        {
                            hostRegistry.getContainerHostInfoByContainerName( containerInfo.getName() );
                        }
                        catch ( HostDisconnectedException e )
                        {
                            deadContainerNames.add( containerInfo.getName() );
                        }
                    }
                }

                //destroy dead containers
                for ( String deadContainerName : deadContainerNames )
                {
                    //do additional check
                    try
                    {
                        resourceHost.getContainerHostByContainerName( deadContainerName );
                    }
                    catch ( HostNotFoundException e )
                    {
                        try
                        {
                            commandExecutor.execute( resourceHost.getId(),
                                    localPeerCommands.getDestroyContainerCommand( deadContainerName ) );
                        }
                        catch ( CommandException e1 )
                        {
                            LOG.error( e1.getMessage(), e1 );
                        }
                    }
                }
            }
            catch ( ResourceHostException e )
            {
                LOG.error( e.getMessage(), e );
            }


            //destroy lost environments (the ones that are present on RH but not registered with Console)
            Set<Integer> lostEnvironmentsVlans = Sets.newHashSet();
            try
            {

                Set<String> p2pIfNames = resourceHost.getUsedP2pIfaceNames();
                ReservedNetworkResources reservedNetworkResources = getReservedNetworkResources();

                for ( String p2pIfName : p2pIfNames )
                {
                    int vlan = Integer.valueOf( p2pIfName.replace( Common.P2P_INTERFACE_PREFIX, "" ) );
                    NetworkResource networkResource = reservedNetworkResources.findByVlan( vlan );
                    if ( networkResource == null )
                    {
                        lostEnvironmentsVlans.add( vlan );
                    }
                }
            }
            catch ( Exception e )
            {
                LOG.error( e.getMessage(), e );
            }

            for ( Integer vlan : lostEnvironmentsVlans )
            {
                try
                {
                    LOG.warn( "Removing lost environment, vlan = {}", vlan );

                    resourceHost.cleanup( null, vlan );
                }
                catch ( ResourceHostException e )
                {
                    LOG.error( e.getMessage(), e );
                }
            }


            //destroy lost containers (the ones that are present on RH but not registered with Console)
            //a) filter out lost ones
            Set<ContainerHostInfo> lostContainers = Sets.newHashSet();
            try
            {
                ResourceHostInfo resourceHostInfo = hostRegistry.getResourceHostInfoById( resourceHost.getId() );

                //consider only containers that got cached not less than 30 min ago
                if ( System.currentTimeMillis() - ( ( ResourceHostInfoModel ) resourceHostInfo ).getDateCreated()
                        > TimeUnit.MINUTES.toMillis( 60 ) )

                {
                    for ( ContainerHostInfo containerHostInfo : resourceHostInfo.getContainers() )
                    {
                        try
                        {
                            resourceHost.getContainerHostById( containerHostInfo.getId() );
                        }
                        catch ( HostNotFoundException ignore )
                        {
                            if ( !( resourceHost.isManagementHost() && Common.MANAGEMENT_HOSTNAME
                                    .equalsIgnoreCase( containerHostInfo.getContainerName().trim() ) ) )
                            {
                                lostContainers.add( containerHostInfo );
                            }
                        }
                    }
                }
            }
            catch ( Exception e )
            {
                LOG.error( e.getMessage(), e );
            }

            //b) ignore manually created containers
            for ( Iterator<ContainerHostInfo> iterator = lostContainers.iterator(); iterator.hasNext(); )
            {
                ContainerHostInfo lostContainer = iterator.next();

                //filter out containers created by system, not by user
                try
                {
                    //system containers should have both vlan and envId
                    if ( lostContainer.getVlan() == null || lostContainer.getEnvId() == null )
                    {
                        iterator.remove();
                    }
                }
                catch ( Exception e )
                {
                    iterator.remove();
                    LOG.error( e.getMessage(), e );
                }
            }

            //skip cleanup in case an environment workflow is in progress since this can lead to inconsistency
            if ( isContainerCreationInProgress() )
            {
                continue;
            }

            //c) destroy lost containers
            for ( ContainerHostInfo lostContainer : lostContainers )
            {
                try
                {
                    //do additional check here
                    try
                    {
                        resourceHost.getContainerHostById( lostContainer.getId() );
                    }
                    catch ( HostNotFoundException e )
                    {
                        LOG.warn( "Removing lost container {}", lostContainer.getContainerName() );

                        ( ( ResourceHostEntity ) resourceHost ).destroyContainer( lostContainer.getId() );
                    }
                }
                catch ( CommandException e )
                {
                    LOG.error( e.getMessage(), e );
                }
            }


            //cleanup empty environments (the ones with no containers on RH)
            //a) filter out net resources missing on this RH
            Set<NetworkResource> missingNetResources = Sets.newHashSet();
            try
            {
                missingNetResources = getReservedNetworkResources().getNetworkResources();

                for ( Iterator<NetworkResource> iterator = missingNetResources.iterator(); iterator.hasNext(); )
                {
                    final NetworkResourceEntity networkResource = ( NetworkResourceEntity ) iterator.next();

                    boolean found = false;

                    for ( ContainerHost containerHost : resourceHost.getContainerHosts() )
                    {

                        if ( Objects.equals( containerHost.getEnvironmentId().getId(),
                                networkResource.getEnvironmentId() ) )
                        {
                            found = true;
                            break;
                        }
                    }

                    if ( found || System.currentTimeMillis() - networkResource.getDateCreated() < TimeUnit.DAYS
                            .toMillis( 7 ) )
                    {
                        //don't cleanup the found ones and the ones that are created less than 7 days ago
                        iterator.remove();
                    }
                }
            }
            catch ( PeerException e )
            {
                LOG.error( e.getMessage(), e );
            }


            //skip cleanup in case an environment workflow is in progress since this can lead to inconsistency
            if ( isContainerCreationInProgress() )
            {
                continue;
            }

            //b) cleanup empty environments
            for ( NetworkResource networkResource : missingNetResources )
            {
                //do additional check
                if ( resourceHost.getContainerHostsByEnvironmentId( networkResource.getEnvironmentId() ).isEmpty() )
                {
                    try
                    {
                        LOG.warn( "Removing empty environment, vlan = {}", networkResource.getVlan() );

                        resourceHost.cleanup( null, networkResource.getVlan() );
                    }
                    catch ( ResourceHostException e )
                    {
                        LOG.error( e.getMessage(), e );
                    }
                }
            }
        }
    }


    private void notifyPeerEventListeners( final ContainerHost containerHost )
    {
        for ( final LocalPeerEventListener eventListener : peerEventListeners )
        {
            threadPool.execute( new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        eventListener.onContainerDestroyed( containerHost );
                    }
                    catch ( Exception e )
                    {
                        LOG.warn( "Error notifying LocalPeerEventListener: {}", e.getMessage() );
                    }
                }
            } );
        }
    }


    //on heartbeat we may consider RH update as complete
    //because it either arrives right before agent shutdown or upon agent startup
    private void releaseUpdateLock( ResourceHostInfo resourceHostInfo )
    {
        try
        {
            ResourceHostEntity resourceHost = ( ResourceHostEntity ) getResourceHostById( resourceHostInfo.getId() );
            resourceHost.markUpdateAsCompleted();
        }
        catch ( Exception e )
        {
            //ignore
        }
    }
}

