package io.subutai.core.localpeer.impl;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.naming.NamingException;

import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.net.util.SubnetUtils;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;

import io.subutai.common.command.CommandCallback;
import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.CommandUtil;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.dao.DaoManager;
import io.subutai.common.environment.Containers;
import io.subutai.common.environment.CreateEnvironmentContainerGroupRequest;
import io.subutai.common.environment.CreateEnvironmentContainerResponseCollector;
import io.subutai.common.environment.PrepareTemplatesRequest;
import io.subutai.common.environment.PrepareTemplatesResponseCollector;
import io.subutai.common.host.ContainerHostInfo;
import io.subutai.common.host.ContainerHostInfoModel;
import io.subutai.common.host.ContainerHostState;
import io.subutai.common.host.HostArchitecture;
import io.subutai.common.host.HostId;
import io.subutai.common.host.HostInfo;
import io.subutai.common.host.HostInterface;
import io.subutai.common.host.HostInterfaceModel;
import io.subutai.common.host.HostInterfaces;
import io.subutai.common.host.NullHostInterface;
import io.subutai.common.host.ResourceHostInfo;
import io.subutai.common.mdc.SubutaiExecutors;
import io.subutai.common.metric.ProcessResourceUsage;
import io.subutai.common.metric.QuotaAlertValue;
import io.subutai.common.metric.ResourceHostMetrics;
import io.subutai.common.network.DomainLoadBalanceStrategy;
import io.subutai.common.network.Gateway;
import io.subutai.common.network.Gateways;
import io.subutai.common.network.Vni;
import io.subutai.common.network.Vnis;
import io.subutai.common.peer.AlertEvent;
import io.subutai.common.peer.ContainerGateway;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.ContainerId;
import io.subutai.common.peer.EnvironmentId;
import io.subutai.common.peer.Host;
import io.subutai.common.peer.HostNotFoundException;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.Payload;
import io.subutai.common.peer.PeerException;
import io.subutai.common.peer.PeerInfo;
import io.subutai.common.peer.RequestListener;
import io.subutai.common.peer.ResourceHost;
import io.subutai.common.peer.ResourceHostException;
import io.subutai.common.protocol.Disposable;
import io.subutai.common.protocol.P2PConfig;
import io.subutai.common.protocol.P2PConnection;
import io.subutai.common.protocol.P2PConnections;
import io.subutai.common.protocol.P2PCredentials;
import io.subutai.common.protocol.PingDistance;
import io.subutai.common.protocol.PingDistances;
import io.subutai.common.protocol.TemplateKurjun;
import io.subutai.common.protocol.Tunnel;
import io.subutai.common.quota.ContainerQuota;
import io.subutai.common.quota.QuotaException;
import io.subutai.common.resource.HistoricalMetrics;
import io.subutai.common.resource.PeerResources;
import io.subutai.common.security.PublicKeyContainer;
import io.subutai.common.security.crypto.pgp.KeyPair;
import io.subutai.common.security.crypto.pgp.PGPKeyUtil;
import io.subutai.common.security.objects.KeyTrustLevel;
import io.subutai.common.security.objects.Ownership;
import io.subutai.common.security.objects.PermissionObject;
import io.subutai.common.security.objects.SecurityKeyType;
import io.subutai.common.security.relation.RelationLink;
import io.subutai.common.security.relation.RelationLinkDto;
import io.subutai.common.settings.Common;
import io.subutai.common.settings.SystemSettings;
import io.subutai.common.task.CloneRequest;
import io.subutai.common.task.CloneResponse;
import io.subutai.common.task.ImportTemplateRequest;
import io.subutai.common.task.QuotaRequest;
import io.subutai.common.task.Task;
import io.subutai.common.task.TaskCallbackHandler;
import io.subutai.common.util.CollectionUtil;
import io.subutai.common.util.ControlNetworkUtil;
import io.subutai.common.util.ExceptionUtil;
import io.subutai.common.util.P2PUtil;
import io.subutai.common.util.ServiceLocator;
import io.subutai.core.executor.api.CommandExecutor;
import io.subutai.core.hostregistry.api.HostDisconnectedException;
import io.subutai.core.hostregistry.api.HostListener;
import io.subutai.core.hostregistry.api.HostRegistry;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.model.User;
import io.subutai.core.kurjun.api.TemplateManager;
import io.subutai.core.localpeer.impl.command.CommandRequestListener;
import io.subutai.core.localpeer.impl.container.CloneTask;
import io.subutai.core.localpeer.impl.container.CreateEnvironmentContainerGroupRequestListener;
import io.subutai.core.localpeer.impl.container.ImportTask;
import io.subutai.core.localpeer.impl.container.PrepareTemplateRequestListener;
import io.subutai.core.localpeer.impl.container.QuotaTask;
import io.subutai.core.localpeer.impl.dao.ResourceHostDataService;
import io.subutai.core.localpeer.impl.entity.AbstractSubutaiHost;
import io.subutai.core.localpeer.impl.entity.ContainerHostEntity;
import io.subutai.core.localpeer.impl.entity.ResourceHostEntity;
import io.subutai.core.localpeer.impl.tasks.ReserveVniTask;
import io.subutai.core.localpeer.impl.tasks.SetupTunnelsTask;
import io.subutai.core.lxc.quota.api.QuotaManager;
import io.subutai.core.metric.api.Monitor;
import io.subutai.core.metric.api.MonitorException;
import io.subutai.core.network.api.NetworkManager;
import io.subutai.core.network.api.NetworkManagerException;
import io.subutai.core.object.relation.api.RelationManager;
import io.subutai.core.object.relation.api.model.Relation;
import io.subutai.core.object.relation.api.model.RelationInfoMeta;
import io.subutai.core.object.relation.api.model.RelationMeta;
import io.subutai.core.object.relation.api.model.RelationStatus;
import io.subutai.core.registration.api.RegistrationManager;
import io.subutai.core.security.api.SecurityManager;
import io.subutai.core.security.api.crypto.EncryptionTool;
import io.subutai.core.security.api.crypto.KeyManager;


/**
 * Local peer implementation
 */
@PermitAll
public class LocalPeerImpl implements LocalPeer, HostListener, Disposable
{
    private static final Logger LOG = LoggerFactory.getLogger( LocalPeerImpl.class );

    private static final String GATEWAY_INTERFACE_NAME_REGEX = "^gw-(\\d+)$";
    private static final Pattern GATEWAY_INTERFACE_NAME_PATTERN = Pattern.compile( GATEWAY_INTERFACE_NAME_REGEX );

    private DaoManager daoManager;
    private TemplateManager templateRegistry;
    protected Host managementHost;
    protected Set<ResourceHost> resourceHosts = Sets.newHashSet();
    private CommandExecutor commandExecutor;
    private QuotaManager quotaManager;
    private Monitor monitor;
    protected ResourceHostDataService resourceHostDataService;
    private HostRegistry hostRegistry;
    protected CommandUtil commandUtil = new CommandUtil();
    protected ExceptionUtil exceptionUtil = new ExceptionUtil();
    protected Set<RequestListener> requestListeners = Sets.newHashSet();
    protected PeerInfo peerInfo;
    private SecurityManager securityManager;
    protected ServiceLocator serviceLocator = new ServiceLocator();
    private IdentityManager identityManager;
    private RelationManager relationManager;


    protected boolean initialized = false;
    protected ExecutorService singleThreadExecutorService = SubutaiExecutors.newSingleThreadExecutor();
    private TaskManagerImpl taskManager;


    public LocalPeerImpl( DaoManager daoManager, TemplateManager templateRegistry, QuotaManager quotaManager,
                          CommandExecutor commandExecutor, HostRegistry hostRegistry, Monitor monitor,
                          SecurityManager securityManager )
    {
        this.daoManager = daoManager;
        this.templateRegistry = templateRegistry;
        this.quotaManager = quotaManager;
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


    public void init() throws PeerException
    {
        LOG.debug( "********************************************** Initializing peer "
                + "******************************************" );

        initPeerInfo();

        //add command request listener
        addRequestListener( new CommandRequestListener() );
        //add command response listener

        //add create container requests listener
        addRequestListener( new CreateEnvironmentContainerGroupRequestListener( this ) );

        //add prepare templates listener
        addRequestListener( new PrepareTemplateRequestListener( this ) );


        try
        {

            resourceHostDataService = createResourceHostDataService();
            resourceHosts.clear();
            synchronized ( resourceHosts )
            {
                for ( ResourceHost resourceHost : resourceHostDataService.getAll() )
                {
                    resourceHosts.add( resourceHost );
                }
            }

            setResourceHostTransientFields( getResourceHosts() );
        }
        catch ( Exception e )
        {
            throw new LocalPeerInitializationError( "Failed to init Local Peer", e );
        }

        taskManager = new TaskManagerImpl( this );
        initialized = true;
    }


    protected void initPeerInfo()
    {

        peerInfo = new PeerInfo();
        peerInfo.setId( securityManager.getKeyManager().getPeerId() );
        peerInfo.setOwnerId( securityManager.getKeyManager().getPeerOwnerId() );
        peerInfo.setPublicUrl( SystemSettings.getPublicUrl() );
        peerInfo.setPort( SystemSettings.getPublicSecurePort() );
        peerInfo.setName( String.format( "Peer %s on %s", peerInfo.getId(), SystemSettings.getPublicUrl() ) );
    }


    @Override
    public void setPeerInfo( final PeerInfo peerInfo )
    {
        this.peerInfo = peerInfo;
    }


    protected ResourceHostDataService createResourceHostDataService()
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
    }


    private void setResourceHostTransientFields( Set<ResourceHost> resourceHosts )
    {
        for ( ResourceHost resourceHost : resourceHosts )
        {
            ( ( AbstractSubutaiHost ) resourceHost ).setPeer( this );
            final ResourceHostEntity resourceHostEntity = ( ResourceHostEntity ) resourceHost;
            resourceHostEntity.setRegistry( templateRegistry );
            resourceHostEntity.setHostRegistry( hostRegistry );
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
    public PeerInfo getPeerInfo() throws PeerException
    {
        if ( peerInfo == null )
        {
            throw new PeerException( "Peer info unavailable." );
        }
        return peerInfo;
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
            throw new PeerException( "Error getting container state ", e );
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
            throw new PeerException( "Error getting container state ", e );
        }
    }


    @Override
    public void configureHostsInEnvironment( final EnvironmentId environmentId,
                                             final Map<String, String> hostAddresses ) throws PeerException
    {
        Preconditions.checkNotNull( environmentId, "Environment id is null" );
        Preconditions.checkArgument( hostAddresses != null && !hostAddresses.isEmpty(), "Invalid host addresses" );

        Set<Host> hosts = Sets.newHashSet();

        hosts.addAll( findContainersByEnvironmentId( environmentId.getId() ) );

        if ( hosts.isEmpty() )
        {
            return;
        }

        Map<Host, CommandResult> results =
                commandUtil.executeParallelSilent( getAddIpHostToEtcHostsCommand( hostAddresses ), hosts );


        Set<Host> succeededHosts = Sets.newHashSet();
        Set<Host> failedHosts = Sets.newHashSet( hosts );

        for ( Map.Entry<Host, CommandResult> resultEntry : results.entrySet() )
        {
            CommandResult result = resultEntry.getValue();
            Host host = resultEntry.getKey();

            if ( result.hasSucceeded() )
            {
                succeededHosts.add( host );
            }
        }

        failedHosts.removeAll( succeededHosts );

        for ( Host failedHost : failedHosts )
        {
            LOG.error( "Host registration failed on host {}", failedHost.getHostname() );
        }

        if ( !failedHosts.isEmpty() )
        {
            throw new PeerException( "Failed to register all hosts" );
        }
    }


    protected RequestBuilder getAddIpHostToEtcHostsCommand( Map<String, String> hostAddresses )
    {
        StringBuilder cleanHosts = new StringBuilder( "localhost|127.0.0.1|" );
        StringBuilder appendHosts = new StringBuilder();

        for ( Map.Entry<String, String> hostEntry : hostAddresses.entrySet() )
        {
            String hostname = hostEntry.getKey();
            String ip = hostEntry.getValue();
            cleanHosts.append( ip ).append( "|" ).append( hostname ).append( "|" );
            appendHosts.append( "/bin/echo '" ).
                    append( ip ).append( " " ).
                               append( hostname ).append( "." ).append( Common.DEFAULT_DOMAIN_NAME ).
                               append( " " ).append( hostname ).
                               append( "' >> '/etc/hosts'; " );
        }

        if ( cleanHosts.length() > 0 )
        {
            //drop pipe | symbol
            cleanHosts.setLength( cleanHosts.length() - 1 );
            cleanHosts.insert( 0, "egrep -v '" );
            cleanHosts.append( "' /etc/hosts > etc-hosts-cleaned; mv etc-hosts-cleaned /etc/hosts;" );
            appendHosts.insert( 0, cleanHosts );
        }

        appendHosts.append( "/bin/echo '127.0.0.1 localhost " ).append( "' >> '/etc/hosts';" );

        return new RequestBuilder( appendHosts.toString() );
    }


    @Override
    public void configureSshInEnvironment( final EnvironmentId environmentId, final Set<String> sshKeys )
            throws PeerException
    {
        Preconditions.checkNotNull( environmentId, "Environment id is null" );
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( sshKeys ), "Invalid ssh keys" );

        Set<Host> hosts = Sets.newHashSet();

        hosts.addAll( findContainersByEnvironmentId( environmentId.getId() ) );

        if ( hosts.isEmpty() )
        {
            return;
        }

        //add keys in portions, since all can not fit into one command, it fails
        int i = 0;
        StringBuilder keysString = new StringBuilder();
        for ( String key : sshKeys )
        {
            keysString.append( key );
            i++;
            //send next 5 keys
            if ( i % 5 == 0 || i == sshKeys.size() )
            {
                Set<Host> succeededHosts = Sets.newHashSet();
                Set<Host> failedHosts = Sets.newHashSet( hosts );

                Map<Host, CommandResult> results =
                        commandUtil.executeParallelSilent( getAppendSshKeysCommand( keysString.toString() ), hosts );

                keysString.setLength( 0 );

                for ( Map.Entry<Host, CommandResult> resultEntry : results.entrySet() )
                {
                    CommandResult result = resultEntry.getValue();
                    Host host = resultEntry.getKey();

                    if ( result.hasSucceeded() )
                    {
                        succeededHosts.add( host );
                    }
                }

                failedHosts.removeAll( succeededHosts );

                for ( Host failedHost : failedHosts )
                {
                    LOG.error( "Failed to add ssh keys on host {}", failedHost.getHostname() );
                }

                if ( !failedHosts.isEmpty() )
                {
                    throw new PeerException( "Failed to add ssh keys on all hosts" );
                }
            }
        }

        //config ssh
        Set<Host> succeededHosts = Sets.newHashSet();
        Set<Host> failedHosts = Sets.newHashSet( hosts );

        Map<Host, CommandResult> results = commandUtil.executeParallelSilent( getConfigSSHCommand(), hosts );

        for ( Map.Entry<Host, CommandResult> resultEntry : results.entrySet() )
        {
            CommandResult result = resultEntry.getValue();
            Host host = resultEntry.getKey();

            if ( result.hasSucceeded() )
            {
                succeededHosts.add( host );
            }
        }

        failedHosts.removeAll( succeededHosts );

        for ( Host failedHost : failedHosts )
        {
            LOG.error( "Failed to configure ssh on host {}", failedHost.getHostname() );
        }

        if ( !failedHosts.isEmpty() )
        {
            throw new PeerException( "Failed to configure ssh on all hosts" );
        }
    }


    protected RequestBuilder getAppendSshKeysCommand( String keys )
    {
        return new RequestBuilder( String.format( "mkdir -p %1$s && " +
                "chmod 700 %1$s && " +
                "echo '%3$s' >> %2$s && " +
                "chmod 644 %2$s", Common.CONTAINER_SSH_FOLDER, Common.CONTAINER_SSH_FILE, keys ) );
    }


    protected RequestBuilder getConfigSSHCommand()
    {
        return new RequestBuilder( String.format( "echo 'Host *' > %1$s/config && " +
                "echo '    StrictHostKeyChecking no' >> %1$s/config && " +
                "chmod 644 %1$s/config", Common.CONTAINER_SSH_FOLDER ) );
    }


    //TODO this is for basic environment via hub
    //    @RolesAllowed( "Environment-Management|Write" )
    @Override
    public PrepareTemplatesResponseCollector prepareTemplates( final PrepareTemplatesRequest request )
            throws PeerException
    {
        final PrepareTemplatesResponseCollector prepareTemplatesResponse =
                new PrepareTemplatesResponseCollector( getId() );
        for ( String resourceHostId : request.getTemplates().keySet() )
        {
            for ( String templateName : request.getTemplates().get( resourceHostId ) )
            {
                ImportTask task = new ImportTask( new ImportTemplateRequest( resourceHostId, templateName ) );
                prepareTemplatesResponse.addTask( taskManager.schedule( task, prepareTemplatesResponse ) );
            }
        }

        prepareTemplatesResponse.waitResponsesWhileSucceeded();
        return prepareTemplatesResponse;
    }


    //TODO this is for basic environment via hub
    //    @RolesAllowed( "Environment-Management|Write" )
    @Override
    public CreateEnvironmentContainerResponseCollector createEnvironmentContainerGroup(
            final CreateEnvironmentContainerGroupRequest requestGroup ) throws PeerException
    {
        Preconditions.checkNotNull( requestGroup );

        final CreateEnvironmentContainerResponseCollector response =
                new CreateEnvironmentContainerResponseCollector( getId() );
        final TaskCallbackHandler<CloneRequest, CloneResponse> successResultHandler = getCloneSuccessHandler( this );

        final Vni environmentVni = getReservedVnis().findVniByEnvironmentId( requestGroup.getEnvironmentId() );

        if ( environmentVni == null )
        {
            throw new PeerException(
                    String.format( "No reserved vni found for environment %s", requestGroup.getEnvironmentId() ) );
        }

        for ( final CloneRequest request : requestGroup.getRequests() )
        {
            try
            {

                int rhCoresNumber = getResourceHostById( request.getResourceHostId() ).getNumberOfCpuCores();

                CloneTask task = new CloneTask( request, environmentVni.getVlan(), rhCoresNumber );

                task.onSuccess( successResultHandler );

                response.addTask( taskManager.schedule( task, response ) );
            }
            catch ( Exception e )
            {
                LOG.error( e.getMessage(), e );
            }
        }

        response.waitAllResponses();
        return response;
    }


    private TaskCallbackHandler<CloneRequest, CloneResponse> getCloneSuccessHandler( final LocalPeer localPeer )
    {
        return new TaskCallbackHandler<CloneRequest, CloneResponse>()
        {
            @Override
            public void handle( Task task, CloneRequest request, CloneResponse response ) throws Exception
            {
                if ( response == null )
                {
                    throw new IllegalArgumentException( "Task response could not be null." );
                }

                try
                {
                    QuotaTask quotaTask = new QuotaTask( quotaManager,
                            new QuotaRequest( request.getResourceHostId(), request.getHostname(),
                                    request.getContainerSize() ) );

                    taskManager.schedule( quotaTask, null );

                    final HostInterfaces interfaces = new HostInterfaces();
                    interfaces.addHostInterface(
                            new HostInterfaceModel( Common.DEFAULT_CONTAINER_INTERFACE, response.getIp() ) );
                    final String hostId = response.getContainerId();
                    final String localPeerId = localPeer.getId();
                    final HostArchitecture arch = request.getTemplateArch();
                    final String hostname = request.getHostname();
                    ContainerHostEntity containerHostEntity =
                            new ContainerHostEntity( localPeerId, hostId, hostname, arch, interfaces,
                                    request.getContainerName(), request.getTemplateName(), arch.name(),
                                    request.getEnvironmentId(), request.getOwnerId(), request.getInitiatorPeerId(),
                                    request.getContainerSize(), ContainerHostState.RUNNING );

                    registerContainer( request.getResourceHostId(), containerHostEntity );

                    //wait for container
                    boolean isRunning = false;
                    long waitStart = System.currentTimeMillis();
                    while ( !isRunning
                            && System.currentTimeMillis() - waitStart < Common.WAIT_CONTAINER_CONNECTION_SEC * 1000 )
                    {
                        try
                        {
                            isRunning = hostRegistry.getContainerHostInfoById( hostId ).getState()
                                    == ContainerHostState.RUNNING;
                        }
                        catch ( HostDisconnectedException e )
                        {
                            //ignore
                        }
                        if ( !isRunning )
                        {
                            Thread.sleep( 100 );
                        }
                    }
                }
                catch ( Exception e )
                {
                    LOG.error( "Error on registering container.", e );
                    throw new PeerException( "Error on registering container.", e );
                }
            }
        };
    }


    protected void registerContainer( String resourceHostId, ContainerHostEntity containerHostEntity ) throws Exception
    {
        ResourceHost resourceHost = getResourceHostById( resourceHostId );

        resourceHost.addContainerHost( containerHostEntity );

        signContainerKeyWithPEK( containerHostEntity.getId(), containerHostEntity.getEnvironmentId() );

        resourceHostDataService.saveOrUpdate( resourceHost );

        LOG.debug( "New container host registered: " + containerHostEntity.getHostname() );
    }


    private void signContainerKeyWithPEK( String containerId, EnvironmentId envId ) throws PeerException
    {
        String pairId = String.format( "%s-%s", getId(), envId.getId() );
        final PGPSecretKeyRing pekSecKeyRing = securityManager.getKeyManager().getSecretKeyRing( pairId );
        try
        {
            PGPPublicKeyRing containerPub = securityManager.getKeyManager().getPublicKeyRing( containerId );

            PGPPublicKeyRing signedKey = securityManager.getKeyManager().setKeyTrust( pekSecKeyRing, containerPub,
                    KeyTrustLevel.Full.getId() );

            securityManager.getKeyManager().updatePublicKeyRing( signedKey );
        }
        catch ( Exception ex )
        {
            throw new PeerException( ex );
        }
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


    @Override
    public ContainerHost findContainerById( final ContainerId containerId )
    {
        Preconditions.checkNotNull( containerId, "Invalid container id" );
        Preconditions.checkNotNull( containerId.getId(), "Invalid container id" );

        ContainerHost result = null;
        for ( ResourceHost resourceHost : getResourceHosts() )
        {
            try
            {
                result = resourceHost.getContainerHostById( containerId.getId() );
                break;
            }
            catch ( HostNotFoundException ignore )
            {
                // ignore
            }
        }
        return result;
    }


    @Override
    public Set<ContainerHost> findContainersByOwnerId( final String ownerId )
    {
        Preconditions.checkNotNull( ownerId, "Specify valid owner" );


        Set<ContainerHost> result = new HashSet<>();

        for ( ResourceHost resourceHost : getResourceHosts() )
        {
            result.addAll( resourceHost.getContainerHostsByOwnerId( ownerId ) );
        }
        return result;
    }


    @PermitAll
    @Override
    public ContainerHost getContainerHostByName( String hostname ) throws HostNotFoundException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hostname ), "Container hostname shouldn't be null" );

        for ( ResourceHost resourceHost : getResourceHosts() )
        {
            try
            {
                return resourceHost.getContainerHostByName( hostname );
            }
            catch ( HostNotFoundException ignore )
            {
                //ignore
            }
        }

        throw new HostNotFoundException( String.format( "No container host found for name %s", hostname ) );
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
    public ContainerHostInfo getContainerHostInfoById( final String containerHostId ) throws PeerException
    {
        ContainerHost containerHost = getContainerHostById( containerHostId );

        return new ContainerHostInfoModel( containerHost );
    }


    @PermitAll
    @Override
    public ResourceHost getResourceHostByName( String hostname ) throws HostNotFoundException
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
    public ResourceHost getResourceHostByContainerName( final String containerName ) throws HostNotFoundException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( containerName ), "Invalid container name" );

        ContainerHost c = getContainerHostByName( containerName );
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
    public Host bindHost( String id ) throws HostNotFoundException
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
    public ContainerHostEntity bindHost( final ContainerId containerId ) throws HostNotFoundException
    {
        return ( ContainerHostEntity ) bindHost( containerId.getId() );
    }


    //TODO this is for basic environment via hub
    //    @RolesAllowed( "Environment-Management|Update" )
    @Override
    public void startContainer( final ContainerId containerId ) throws PeerException
    {
        Preconditions.checkNotNull( containerId, "Cannot operate on null container id" );

        ContainerHostEntity containerHost = bindHost( containerId );
        ResourceHost resourceHost = containerHost.getParent();
        try
        {
            resourceHost.startContainerHost( containerHost );
        }
        catch ( Exception e )
        {
            throw new PeerException( String.format( "Could not start LXC container [%s]", e.toString() ) );
        }
    }


    //TODO this is for basic environment via hub
    //    @RolesAllowed( "Environment-Management|Update" )
    @Override
    public void stopContainer( final ContainerId containerId ) throws PeerException
    {
        Preconditions.checkNotNull( containerId, "Cannot operate on null container id" );

        ContainerHostEntity containerHost = bindHost( containerId );
        ResourceHost resourceHost = containerHost.getParent();
        try
        {
            resourceHost.stopContainerHost( containerHost );
        }
        catch ( Exception e )
        {
            throw new PeerException( String.format( "Could not stop LXC container [%s]", e.toString() ) );
        }
    }


    //TODO this is for basic environment via hub
    //    @RolesAllowed( "Environment-Management|Delete" )
    @Override
    public void destroyContainer( final ContainerId containerId ) throws PeerException
    {
        Preconditions.checkNotNull( containerId, "Cannot operate on null container id" );

        ContainerHostEntity host = bindHost( containerId );
        ResourceHost resourceHost = host.getParent();

        try
        {
            resourceHost.destroyContainerHost( host );
            quotaManager.removeQuota( containerId );
        }
        catch ( ResourceHostException e )
        {
            String errMsg = String.format( "Could not destroy container [%s]", host.getHostname() );
            LOG.error( errMsg, e );
            throw new PeerException( errMsg, e.toString() );
        }

        resourceHostDataService.update( ( ResourceHostEntity ) resourceHost );
    }


    @RolesAllowed( "Environment-Management|Delete" )
    @Override
    public void removePeerEnvironmentKeyPair( final EnvironmentId environmentId ) throws PeerException
    {
        Preconditions.checkNotNull( environmentId );

        KeyManager keyManager = securityManager.getKeyManager();

        keyManager.removeKeyData( environmentId.getId() );
        keyManager.removeKeyData( getId() + "-" + environmentId.getId() );
    }


    //TODO this is for basic environment via hub
    //    @RolesAllowed( "Environment-Management|Write" )
    @Override
    public void setDefaultGateway( final ContainerGateway gateway ) throws PeerException
    {
        Preconditions.checkNotNull( gateway, "Invalid gateway" );

        try
        {
            commandUtil.execute( new RequestBuilder( String.format( "route add default gw %s %s", gateway.getGateway(),
                    Common.DEFAULT_CONTAINER_INTERFACE ) ), bindHost( gateway.getContainerId() ) );
        }
        catch ( CommandException e )
        {
            throw new PeerException( e );
        }
    }


    @Override
    public boolean isConnected( final HostId hostId )
    {
        Preconditions.checkNotNull( hostId, "Host id null" );

        try
        {
            HostInfo hostInfo = hostRegistry.getHostInfoById( hostId.getId() );
            return hostInfo.getId().equals( hostId.getId() );
        }
        catch ( HostDisconnectedException e )
        {
            return false;
        }
    }


    @Override
    public Host getManagementHost() throws HostNotFoundException
    {
        if ( managementHost == null )
        {
            throw new HostNotFoundException( String.format( "Management host not found on peer %s.", getId() ) );
        }

        return managementHost;
    }


    @Override
    public Set<ResourceHost> getResourceHosts()
    {
        synchronized ( resourceHosts )
        {
            return Sets.newConcurrentHashSet( this.resourceHosts );
        }
    }


    public void addResourceHost( final ResourceHost host )
    {
        Preconditions.checkNotNull( host, "Resource host could not be null." );

        synchronized ( resourceHosts )
        {
            resourceHosts.add( host );
        }
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


    @Override
    public TemplateKurjun getTemplate( final String templateName )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( templateName ), "Invalid template name" );

        return templateRegistry.getTemplate( templateName );
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


    protected <T, V> V sendRequestInternal( final T request, final String recipient, final Class<V> responseType )
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
                    throw new PeerException( e );
                }
            }
        }

        return null;
    }


    @Override
    public void onHeartbeat( final ResourceHostInfo resourceHostInfo, Set<QuotaAlertValue> alerts )
    {
        LOG.debug( "On heartbeat: " + resourceHostInfo.getHostname() );
        if ( initialized )
        {
            ResourceHostEntity host;
            try
            {
                host = ( ResourceHostEntity ) getResourceHostByName( resourceHostInfo.getHostname() );
            }
            catch ( HostNotFoundException e )
            {
                host = new ResourceHostEntity( getId(), resourceHostInfo );
                resourceHostDataService.persist( host );
                addResourceHost( host );
                Set<ResourceHost> a = Sets.newHashSet();
                a.add( host );
                setResourceHostTransientFields( a );
                buildAdminHostRelation( host );
                LOG.debug( String.format( "Resource host %s registered.", resourceHostInfo.getHostname() ) );
            }
            if ( host.updateHostInfo( resourceHostInfo ) )
            {
                resourceHostDataService.update( host );
                LOG.debug( String.format( "Resource host %s updated.", resourceHostInfo.getHostname() ) );
            }
            if ( managementHost == null )
            {
                try
                {
                    final Host managementLxc = findHostByName( "management" );
                    if ( managementLxc instanceof ContainerHostEntity )
                    {
                        managementHost = ( ( ContainerHostEntity ) managementLxc ).getParent();
                        buildAdminHostRelation( managementHost );
                        //todo save flag that exchange happened to db
                        exchangeMhKeysWithRH();
                    }
                }
                catch ( Exception e )
                {
                    //ignore
                }
            }
        }
    }


    private void buildAdminHostRelation(Host host)
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

            if ( "management".equalsIgnoreCase( host.getHostname() ) )
            {
                relationTraits.put( "managementSupervisor", "true" );
            }
            else
            {
                relationTraits.put( "resourceSupervisor", "true" );
                relationTraits.put( "containerManagement", "true" );
            }

            RelationMeta relationMeta =
                    new RelationMeta( peerOwner, peerOwner, host, peerOwner.getSecurityKeyId() );
            Relation relation = relationManager.buildRelation( relationInfoMeta, relationMeta );
            relation.setRelationStatus( RelationStatus.VERIFIED );
            relationManager.saveRelation( relation );
        }
    }


    @Override
    public void exchangeMhKeysWithRH() throws Exception
    {

        RegistrationManager registrationManager = ServiceLocator.getServiceNoCache( RegistrationManager.class );

        String token = registrationManager.generateContainerTTLToken( 30 * 1000L ).getToken();

        final RequestBuilder requestBuilder =
                new RequestBuilder( String.format( "subutai import management -t %s", token ) );

        commandUtil.execute( requestBuilder, getManagementHost() );
    }


    @Override
    public ProcessResourceUsage getProcessResourceUsage( final ContainerId containerId, int pid ) throws PeerException
    {
        Preconditions.checkNotNull( containerId );
        Preconditions.checkArgument( pid > 0, "Process pid must be greater than 0" );

        try
        {
            return monitor.getProcessResourceUsage( containerId, pid );
        }
        catch ( MonitorException e )
        {
            throw new PeerException( e );
        }
    }


    @Override
    public Set<Integer> getCpuSet( final ContainerHost host ) throws PeerException
    {
        Preconditions.checkNotNull( host, "Invalid container host" );

        try
        {
            return quotaManager.getCpuSet( host.getContainerId() );
        }
        catch ( QuotaException e )
        {
            throw new PeerException( e );
        }
    }


    //    @RolesAllowed( "Environment-Management|Update" )
    @Override
    public void setCpuSet( final ContainerHost host, final Set<Integer> cpuSet ) throws PeerException
    {
        Preconditions.checkNotNull( host, "Invalid container host" );
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( cpuSet ), "Empty cpu set" );

        try
        {
            quotaManager.setCpuSet( host.getContainerId(), cpuSet );
        }
        catch ( QuotaException e )
        {
            throw new PeerException( e );
        }
    }


    //networking


    @Override
    public Gateways getGateways() throws PeerException
    {
        Gateways gateways = new Gateways();

        for ( HostInterface iface : getManagementHost().getHostInterfaces().getAll() )
        {
            Matcher matcher = GATEWAY_INTERFACE_NAME_PATTERN.matcher( iface.getName().trim() );
            if ( matcher.find() )
            {
                gateways.add( new Gateway( Integer.parseInt( matcher.group( 1 ) ), iface.getIp() ) );
            }
        }

        return gateways;
    }


    //TODO this is for basic environment via hub
    //    @RolesAllowed( "Environment-Management|Write" )
    @Override
    public Vni reserveVni( final Vni vni ) throws PeerException
    {
        Preconditions.checkNotNull( vni, "Invalid vni" );

        //need to execute sequentially since other parallel executions can take the same VNI
        Future<Vni> future = queueSequentialTask( new ReserveVniTask( getNetworkManager(), vni, this ) );

        try
        {
            return future.get();
        }
        catch ( InterruptedException e )
        {
            throw new PeerException( e );
        }
        catch ( ExecutionException e )
        {
            if ( e.getCause() instanceof PeerException )
            {
                throw ( PeerException ) e.getCause();
            }
            throw new PeerException( "Error reserving VNI", e.getCause() );
        }
    }


    @Override
    public Vnis getReservedVnis() throws PeerException
    {
        try
        {
            return getNetworkManager().getReservedVnis();
        }
        catch ( NetworkManagerException e )
        {
            throw new PeerException( e );
        }
    }


    @Override
    public String getVniDomain( final Long vni ) throws PeerException
    {
        Integer vlan = getReservedVnis().findVlanByVni( vni );

        if ( vlan != null )
        {
            try
            {
                return getNetworkManager().getVlanDomain( vlan );
            }
            catch ( NetworkManagerException e )
            {
                throw new PeerException( String.format( "Error obtaining domain by vlan %d", vlan ), e );
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
        Integer vlan = getReservedVnis().findVlanByVni( vni );

        if ( vlan != null )
        {
            try
            {
                getNetworkManager().removeVlanDomain( vlan );
            }
            catch ( NetworkManagerException e )
            {
                throw new PeerException( String.format( "Error removing domain by vlan %d", vlan ), e );
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
                              final DomainLoadBalanceStrategy domainLoadBalanceStrategy, final String sslCertPath )
            throws PeerException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( domain ) );
        Preconditions.checkNotNull( domainLoadBalanceStrategy );

        Integer vlan = getReservedVnis().findVlanByVni( vni );

        if ( vlan != null )
        {
            try
            {
                getNetworkManager().setVlanDomain( vlan, domain, domainLoadBalanceStrategy, sslCertPath );
            }
            catch ( NetworkManagerException e )
            {
                throw new PeerException( String.format( "Error setting domain by vlan %d", vlan ), e );
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

        Integer vlan = getReservedVnis().findVlanByVni( vni );

        if ( vlan != null )
        {
            try
            {
                return getNetworkManager().isIpInVlanDomain( hostIp, vlan );
            }
            catch ( NetworkManagerException e )
            {
                throw new PeerException( String.format( "Error checking domain by ip %s and vlan %d", hostIp, vlan ),
                        e );
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

        Integer vlan = getReservedVnis().findVlanByVni( vni );

        if ( vlan != null )
        {
            try
            {
                getNetworkManager().addIpToVlanDomain( hostIp, vlan );
            }
            catch ( NetworkManagerException e )
            {
                throw new PeerException( String.format( "Error adding ip %s to domain by vlan %d", hostIp, vlan ), e );
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

        Integer vlan = getReservedVnis().findVlanByVni( vni );

        if ( vlan != null )
        {
            try
            {
                getNetworkManager().removeIpFromVlanDomain( hostIp, vlan );
            }
            catch ( NetworkManagerException e )
            {
                throw new PeerException( String.format( "Error removing ip %s from domain by vlan %d", hostIp, vlan ),
                        e );
            }
        }
        else
        {

            throw new PeerException( String.format( "Vlan for vni %d not found", vni ) );
        }
    }


    //TODO this is for basic environment via hub
    //    @RolesAllowed( "Environment-Management|Update" )
    @Override
    public int setupContainerSsh( final String containerHostId, final int sshIdleTimeout ) throws PeerException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( containerHostId ) );
        Preconditions.checkArgument( sshIdleTimeout > 0 );

        ContainerHost containerHost = getContainerHostById( containerHostId );

        HostInterface hostInterface = containerHost.getInterfaceByName( Common.DEFAULT_CONTAINER_INTERFACE );

        if ( hostInterface instanceof NullHostInterface )
        {
            throw new PeerException( "Container IP not found" );
        }

        try
        {
            return getNetworkManager().setupContainerSsh( hostInterface.getIp(), sshIdleTimeout );
        }
        catch ( NetworkManagerException e )
        {
            throw new PeerException( String.format( "Error setting up ssh for container ip %s", hostInterface.getIp() ),
                    e );
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


    @Override
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
    //TODO this is for basic environment via hub
    //    @RolesAllowed( "Environment-Management|Write" )
    @Override
    public PublicKeyContainer createPeerEnvironmentKeyPair( RelationLinkDto envLink ) throws PeerException
    {
        Preconditions.checkNotNull( envLink );
        //TODO don't generate PEK if already exists, return the existing one!!!
        KeyManager keyManager = securityManager.getKeyManager();
        EncryptionTool encTool = securityManager.getEncryptionTool();
        String pairId = String.format( "%s-%s", getId(), envLink.getUniqueIdentifier() );
        buildPeerEnvRelation( envLink );

        final PGPSecretKeyRing peerSecKeyRing = securityManager.getKeyManager().getSecretKeyRing( null );
        try
        {
            KeyPair keyPair = keyManager.generateKeyPair( pairId, false );

            //******Create PEK *****************************************************************
            PGPSecretKeyRing secRing = PGPKeyUtil.readSecretKeyRing( keyPair.getSecKeyring() );
            PGPPublicKeyRing pubRing = PGPKeyUtil.readPublicKeyRing( keyPair.getPubKeyring() );

            //***************Save Keys *********************************************************
            keyManager.saveSecretKeyRing( pairId, SecurityKeyType.PeerEnvironmentKey.getId(), secRing );
            keyManager.savePublicKeyRing( pairId, SecurityKeyType.PeerEnvironmentKey.getId(), pubRing );

            pubRing =
                    securityManager.getKeyManager().setKeyTrust( peerSecKeyRing, pubRing, KeyTrustLevel.Full.getId() );

            return new PublicKeyContainer( getId(), pubRing.getPublicKey().getFingerprint(),
                    encTool.armorByteArrayToString( pubRing.getEncoded() ) );
        }
        catch ( IOException | PGPException ex )
        {
            throw new PeerException( ex );
        }
    }


    private void buildPeerEnvRelation(final RelationLink envLink)
    {

        // Build relation between LocalPeer and LocalEnvironment/CrossPeerEnvironment.

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

            RelationMeta relationMeta = new RelationMeta( peerOwner, this, envLink, this.getKeyId() );
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
    public void addPeerEnvironmentPubKey( final String keyId, final PGPPublicKeyRing pubRing )
    {
        Preconditions.checkNotNull( keyId );
        Preconditions.checkNotNull( pubRing );

        securityManager.getKeyManager().savePublicKeyRing( keyId, SecurityKeyType.PeerEnvironmentKey.getId(), pubRing );

        // Build relation between LocalPeer => RemotePeer => Environment
        // for message encryption/decryption mechanism described in relation trais
        String [] ids = keyId.split( "_" );
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

            RelationMeta relationMeta = new RelationMeta( this, peerLink, envLink, this.getKeyId() );
            Relation relation = relationManager.buildRelation( relationInfoMeta, relationMeta );
            relation.setRelationStatus( RelationStatus.VERIFIED );
            relationManager.saveRelation( relation );
        }
    }


    @Override
    public HostInterfaces getInterfaces() throws HostNotFoundException
    {
        return getManagementHost().getHostInterfaces();
    }


    //----------- P2P SECTION BEGIN --------------------
    @Override
    public void resetP2PSecretKey( final P2PCredentials p2PCredentials ) throws PeerException
    {

        Preconditions.checkNotNull( p2PCredentials, "Invalid p2p credentials" );

        try
        {
            for ( ResourceHost resourceHost : getResourceHosts() )
            {
                getNetworkManager()
                        .resetP2PSecretKey( resourceHost, p2PCredentials.getP2pHash(), p2PCredentials.getP2pSecretKey(),
                                p2PCredentials.getP2pTtlSeconds() );
            }
        }
        catch ( NetworkManagerException e )
        {
            throw new PeerException( "Error resetting P2P secret key", e );
        }
    }


    @Override
    public String getP2PIP( final String resourceHostId, final String swarmHash ) throws PeerException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( resourceHostId ), "Invalid resource host id" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( swarmHash ), "Invalid p2p swarm hash" );

        try
        {
            String p2pIp = getNetworkManager().getP2PConnections( getResourceHostById( resourceHostId ) )
                                              .findConnectionByHash( swarmHash ).getIp();
            if ( p2pIp == null )
            {
                throw new PeerException( "P2P connection not found" );
            }

            return p2pIp;
        }
        catch ( NetworkManagerException e )
        {
            throw new PeerException( "Error getting p2p connection", e );
        }
    }


    //TODO this is for basic environment via hub
    //    @RolesAllowed( "Environment-Management|Update" )
    @Override
    public String setupP2PConnection( final P2PConfig config ) throws PeerException
    {
        Preconditions.checkNotNull( config, "Invalid p2p config" );

        LOG.debug( String.format( "Adding local peer to P2P swarm: %s %s", config.getHash(), config.getAddress() ) );

        try
        {
            Vni envVni = getNetworkManager().getReservedVnis().findVniByEnvironmentId( config.getEnvironmentId() );

            if ( envVni == null )
            {
                throw new PeerException(
                        String.format( "Reserved vni not found for environment %s", config.getEnvironmentId() ) );
            }


            for ( ResourceHost resourceHost : getResourceHosts() )
            {
                P2PConnections p2PConnections = getNetworkManager().getP2PConnections( resourceHost );

                if ( p2PConnections.findConnectionByHash( P2PUtil.generateHash( envVni.getEnvironmentId() ) ) != null )
                {
                    getNetworkManager().resetP2PSecretKey( resourceHost, config.getHash(), config.getSecretKey(),
                            config.getSecretKeyTtlSec() );
                }
                else
                {
                    //we don't supply p2p IP since it should get assigned dynamically
                    getNetworkManager()
                            .setupP2PConnection( resourceHost, P2PUtil.generateInterfaceName( envVni.getVlan() ), null,
                                    config.getHash(), config.getSecretKey(), config.getSecretKeyTtlSec() );
                }
            }

            //obtain p2p connection on MH-RH
            P2PConnection mhP2pConnection =
                    getNetworkManager().getP2PConnections().findConnectionByHash( config.getHash() );

            if ( mhP2pConnection == null )
            {
                throw new PeerException( "P2P connection on management host not found." );
            }

            return mhP2pConnection.getIp();
        }
        catch ( NetworkManagerException e )
        {
            LOG.error( e.getMessage(), e );
            throw new PeerException( "Failed to setup P2P connection", e );
        }
    }


    public void setupInitialP2PConnection( final P2PConfig config ) throws PeerException
    {
        Preconditions.checkNotNull( config, "Invalid p2p config" );

        LOG.debug( String.format( "Adding local peer MH to P2P swarm: %s %s", config.getHash(), config.getAddress() ) );

        try
        {
            Vni envVni = getNetworkManager().getReservedVnis().findVniByEnvironmentId( config.getEnvironmentId() );

            if ( envVni == null )
            {
                throw new PeerException(
                        String.format( "Reserved vni not found for environment %s", config.getEnvironmentId() ) );
            }


            P2PConnections p2PConnections = getNetworkManager().getP2PConnections();

            if ( p2PConnections.findConnectionByHash( P2PUtil.generateHash( envVni.getEnvironmentId() ) ) != null )
            {
                throw new PeerException( "Initial P2P connection is already setup with this hash" );
            }
            else
            {
                getNetworkManager()
                        .setupP2PConnection( P2PUtil.generateInterfaceName( envVni.getVlan() ), config.getAddress(),
                                config.getHash(), config.getSecretKey(), config.getSecretKeyTtlSec() );
            }
        }
        catch ( NetworkManagerException e )
        {
            throw new PeerException( "Failed to setup P2P connection on MH", e );
        }
    }


    //TODO this is for basic environment via hub
    //    @RolesAllowed( "Environment-Management|Delete" )
    @Override
    public void removeP2PConnection( final String p2pHash ) throws PeerException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( p2pHash ), "Invalid p2p hash" );

        try
        {
            for ( ResourceHost resourceHost : getResourceHosts() )
            {
                getNetworkManager().removeP2PConnection( resourceHost, p2pHash );
            }
        }
        catch ( NetworkManagerException e )
        {
            throw new PeerException( "Failed to remove P2P connection", e );
        }
    }
    //----------- P2P SECTION END --------------------


    //TODO this is for basic environment via hub
    //    @RolesAllowed( "Environment-Management|Delete" )
    @Override
    public void cleanupEnvironment( final EnvironmentId environmentId ) throws PeerException
    {
        Preconditions.checkNotNull( environmentId );

        Vni vni = getReservedVnis().findVniByEnvironmentId( environmentId.getId() );
        if ( vni == null )
        {
            LOG.warn(
                    "Environment VNI not found to cleanup resources hosts. Environment ID: " + environmentId.getId() );
            return;
        }

        for ( ResourceHost resourceHost : getResourceHosts() )
        {
            try
            {
                resourceHost.cleanup( environmentId, vni.getVlan() );
            }
            catch ( ResourceHostException e )
            {
                throw new PeerException( e.getMessage() );
            }
        }
    }


    @Override
    public ResourceHostMetrics getResourceHostMetrics()
    {
        return monitor.getResourceHostMetrics();
    }


    @Override
    public PeerResources getResourceLimits( final String peerId ) throws PeerException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( peerId ) );

        return quotaManager.getResourceLimits( peerId );
    }


    @Override
    public List<TemplateKurjun> getTemplates()
    {
        return templateRegistry.list();
    }


    @Override
    public TemplateKurjun getTemplateByName( final String name )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( name ) );

        return templateRegistry.getTemplate( name );
    }


    @Override
    public ContainerQuota getAvailableQuota( final ContainerId containerId ) throws PeerException
    {
        Preconditions.checkNotNull( containerId );

        try
        {
            ContainerHost containerHost = getContainerHostById( containerId.getId() );
            return quotaManager.getAvailableQuota( containerHost.getContainerId() );
        }
        catch ( QuotaException e )
        {
            throw new PeerException( String.format( "Could not obtain quota for: %s", containerId ) );
        }
    }


    @Override
    public ContainerQuota getQuota( final ContainerId containerId ) throws PeerException
    {
        Preconditions.checkNotNull( containerId );
        try
        {
            ContainerHost containerHost = getContainerHostById( containerId.getId() );
            return quotaManager.getQuota( containerHost.getContainerId() );
        }
        catch ( QuotaException e )
        {
            throw new PeerException( String.format( "Could not obtain quota for: %s.", containerId.getId() ) );
        }
    }


    @Override
    public void setQuota( final ContainerId containerId, final ContainerQuota containerQuota ) throws PeerException
    {
        Preconditions.checkNotNull( containerId );
        Preconditions.checkNotNull( containerQuota );
        try
        {
            ContainerHost containerHost = getContainerHostById( containerId.getId() );
            quotaManager.setQuota( containerHost.getContainerId(), containerQuota );
        }
        catch ( QuotaException e )
        {
            throw new PeerException( String.format( "Could not set quota for: %s", containerId.getId() ) );
        }
    }


    @Override
    public void alert( AlertEvent alert )
    {
        Preconditions.checkNotNull( alert );

        monitor.addAlert( alert );
    }


    @Override
    public HistoricalMetrics getHistoricalMetrics( final String hostname, final Date startTime, final Date endTime )
            throws PeerException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hostname ) );
        Preconditions.checkNotNull( startTime );
        Preconditions.checkNotNull( endTime );

        try
        {
            Host host = findHostByName( hostname );
            return monitor.getHistoricalMetrics( host, startTime, endTime );
        }
        catch ( HostNotFoundException e )
        {
            throw new PeerException( e.getMessage(), e );
        }
    }


    @Override
    public Host findHostByName( final String hostname ) throws HostNotFoundException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hostname ) );


        if ( managementHost != null && getManagementHost().getHostname().equals( hostname ) )
        {
            return managementHost;
        }

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

        throw new HostNotFoundException( "Host by name '" + hostname + "' not found." );
    }


    public <T> Future<T> queueSequentialTask( Callable<T> callable )
    {
        Preconditions.checkNotNull( callable );

        return singleThreadExecutorService.submit( callable );
    }


    protected NetworkManager getNetworkManager() throws PeerException
    {
        try
        {
            return serviceLocator.getService( NetworkManager.class );
        }
        catch ( NamingException e )
        {
            throw new PeerException( e );
        }
    }


    @Override
    public List<Task> getTaskList()
    {
        return taskManager.getAllTasks();
    }


    @Override
    public Task getTask( final Integer id )
    {
        return taskManager.getTask( id );
    }


    @RolesAllowed( "Environment-Management|Write" )
    @Override
    public int setupTunnels( final Map<String, String> peerIps, final String environmentId ) throws PeerException
    {
        Preconditions.checkNotNull( peerIps, "Invalid peer ips set" );
        Preconditions.checkArgument( !peerIps.isEmpty(), "Invalid peer ips set" );
        Preconditions.checkNotNull( environmentId, "Invalid environment id" );

        //need to execute sequentially since other parallel executions can setup the same tunnel
        Future<Integer> future =
                queueSequentialTask( new SetupTunnelsTask( getNetworkManager(), this, environmentId, peerIps ) );

        try
        {
            return future.get();
        }
        catch ( InterruptedException e )
        {
            throw new PeerException( e );
        }
        catch ( ExecutionException e )
        {
            if ( e.getCause() instanceof PeerException )
            {
                throw ( PeerException ) e.getCause();
            }
            throw new PeerException( "Error setting up tunnels", e.getCause() );
        }
    }


    protected Set<Tunnel> listTunnels() throws PeerException
    {
        try
        {
            return getNetworkManager().listTunnels();
        }
        catch ( NetworkManagerException e )
        {
            throw new PeerException( "Error retrieving peer tunnels", e );
        }
    }


    @Override
    public String getExternalIp() throws PeerException
    {
        return getPeerInfo().getIp();
    }


    @Override
    public HostId getResourceHostIdByContainerId( final ContainerId id ) throws PeerException
    {
        return new HostId( getResourceHostByContainerId( id.getId() ).getId() );
    }


    @Override
    public PingDistances getP2PSwarmDistances( final String p2pHash, final Integer maxAddress ) throws PeerException
    {
        PingDistances result = new PingDistances();
        try
        {
            final P2PConnections p2PConnections = getNetworkManager().getP2PConnections();

            final P2PConnection p2PConnection = p2PConnections.findConnectionByHash( p2pHash );

            if ( p2PConnection == null )
            {
                return result;
            }
            String p2pIP = p2PConnection.getIp();
            final SubnetUtils.SubnetInfo info = new SubnetUtils( p2pIP, ControlNetworkUtil.NETWORK_MASK ).getInfo();

            ExecutorService pool = Executors.newCachedThreadPool();
            ExecutorCompletionService<PingDistance> completionService = new ExecutorCompletionService<>( pool );
            int counter = 0;
            for ( int i = 0; i < maxAddress; i++ )
            {
                if ( !p2PConnection.getIp().equals( info.getAllAddresses()[i] ) )
                {
                    completionService
                            .submit( new PingDistanceTask( p2PConnection.getIp(), info.getAllAddresses()[i] ) );
                    counter++;
                }
            }

            pool.shutdown();

            while ( counter-- > 0 )
            {
                try
                {
                    Future<PingDistance> d = completionService.take();
                    result.add( d.get() );
                }
                catch ( ExecutionException | InterruptedException e )
                {
                    // ignore
                }
            }
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
            throw new PeerException( e.getMessage() );
        }
        return result;
    }


    private class PingDistanceTask implements Callable<PingDistance>
    {
        private final String sourceIp;
        private final String targetIp;


        public PingDistanceTask( final String sourceIp, final String targetIp )
        {
            this.sourceIp = sourceIp;
            this.targetIp = targetIp;
        }


        @Override
        public PingDistance call() throws Exception
        {
            try
            {
                return getNetworkManager().getPingDistance( getManagementHost(), sourceIp, targetIp );
            }
            catch ( Exception e )
            {
                return new PingDistance( sourceIp, targetIp, null, null, null, null );
            }
        }
    }


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
        return PermissionObject.PeerManagement.getName();
    }


    @Override
    public String getKeyId()
    {
        return getId();
    }
}

